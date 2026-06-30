package com.gltu.labreservation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gltu.labreservation.common.BizException;
import com.gltu.labreservation.common.PasswordUtil;
import com.gltu.labreservation.common.TokenStore;
import com.gltu.labreservation.dto.LoginRequest;
import com.gltu.labreservation.dto.LoginResponse;
import com.gltu.labreservation.dto.ResetPasswordRequest;
import com.gltu.labreservation.dto.SendResetCodeRequest;
import com.gltu.labreservation.entity.PasswordResetCode;
import com.gltu.labreservation.entity.User;
import com.gltu.labreservation.mapper.PasswordResetCodeMapper;
import com.gltu.labreservation.mapper.UserMapper;
import com.gltu.labreservation.service.AuthService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int EXPIRE_MINUTES = 5;
    private final UserMapper userMapper;
    private final PasswordResetCodeMapper resetCodeMapper;
    private final TokenStore tokenStore;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final String mailFrom;
    private final String mailPassword;

    public AuthServiceImpl(UserMapper userMapper,
                           PasswordResetCodeMapper resetCodeMapper,
                           TokenStore tokenStore,
                           ObjectProvider<JavaMailSender> mailSenderProvider,
                           ObjectProvider<StringRedisTemplate> redisTemplateProvider,
                           @Value("${spring.mail.username:}") String mailFrom,
                           @Value("${spring.mail.password:}") String mailPassword) {
        this.userMapper = userMapper;
        this.resetCodeMapper = resetCodeMapper;
        this.tokenStore = tokenStore;
        this.mailSenderProvider = mailSenderProvider;
        this.redisTemplateProvider = redisTemplateProvider;
        this.mailFrom = mailFrom;
        this.mailPassword = mailPassword;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .eq(User::getStatus, 1));
        if (user == null || !PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        if (PasswordUtil.needsUpgrade(user.getPassword())) {
            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setPassword(PasswordUtil.hash(request.getPassword()));
            userMapper.updateById(updateUser);
        }
        user.setPassword(null);
        return new LoginResponse(tokenStore.issue(user), user);
    }

    @Override
    public void sendResetCode(SendResetCodeRequest request) {
        User user = findResetUser(request.getUsername(), request.getEmail());
        enforceResetCodeRateLimit(user);
        PasswordResetCode latest = resetCodeMapper.selectOne(new LambdaQueryWrapper<PasswordResetCode>()
                .eq(PasswordResetCode::getAccount, user.getUsername())
                .eq(PasswordResetCode::getReceiver, user.getEmail())
                .eq(PasswordResetCode::getUsed, 0)
                .orderByDesc(PasswordResetCode::getCreateTime)
                .last("LIMIT 1"));
        if (latest != null && latest.getCreateTime() != null
                && latest.getCreateTime().isAfter(LocalDateTime.now().minusSeconds(60))) {
            throw new BizException("验证码发送太频繁，请稍后再试");
        }

        String code = String.valueOf(100000 + new Random().nextInt(900000));
        PasswordResetCode resetCode = new PasswordResetCode();
        resetCode.setAccount(user.getUsername());
        resetCode.setReceiver(user.getEmail());
        resetCode.setCode(code);
        resetCode.setExpireTime(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES));
        resetCode.setUsed(0);
        resetCodeMapper.insert(resetCode);
        sendMail(user.getEmail(), code);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = findResetUser(request.getUsername(), request.getEmail());
        if (!StringUtils.hasText(request.getCode())) {
            throw new BizException("请输入邮箱验证码");
        }
        if (!PasswordUtil.isStrongEnough(request.getNewPassword())) {
            throw new BizException(PasswordUtil.strengthMessage());
        }
        if (PasswordUtil.matches(request.getNewPassword(), user.getPassword())) {
            throw new BizException("新密码不能与旧密码相同");
        }

        PasswordResetCode resetCode = resetCodeMapper.selectOne(new LambdaQueryWrapper<PasswordResetCode>()
                .eq(PasswordResetCode::getAccount, user.getUsername())
                .eq(PasswordResetCode::getReceiver, user.getEmail())
                .eq(PasswordResetCode::getCode, request.getCode())
                .eq(PasswordResetCode::getUsed, 0)
                .orderByDesc(PasswordResetCode::getCreateTime)
                .last("LIMIT 1"));
        if (resetCode == null) {
            throw new BizException("验证码不正确");
        }
        if (resetCode.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BizException("验证码已过期，请重新获取");
        }

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setPassword(PasswordUtil.hash(request.getNewPassword()));
        userMapper.updateById(updateUser);

        resetCode.setUsed(1);
        resetCodeMapper.updateById(resetCode);
    }

    private void enforceResetCodeRateLimit(User user) {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return;
        }
        String key = "lab-reservation:reset-code:limit:" + user.getId();
        try {
            Boolean allowed = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(60));
            if (Boolean.FALSE.equals(allowed)) {
                throw new BizException("验证码发送太频繁，请 60 秒后再试");
            }
        } catch (RedisConnectionFailureException | RedisSystemException exception) {
            System.out.println("Redis reset code rate limit unavailable, fallback to database check: " + exception.getMessage());
        }
    }

    private User findResetUser(String username, String email) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(email)) {
            throw new BizException("请输入账号和邮箱");
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getEmail, email)
                .eq(User::getStatus, 1));
        if (user == null) {
            throw new BizException("账号或邮箱不匹配");
        }
        return user;
    }

    private void sendMail(String email, String code) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || !StringUtils.hasText(mailFrom) || !StringUtils.hasText(mailPassword)) {
            System.out.println("Password reset code for " + email + ": " + code);
            throw new BizException("邮件服务未配置成功，请检查 IDEA 环境变量 MAIL_USERNAME 和 MAIL_PASSWORD；验证码已打印到后端控制台");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("实验室预约系统密码重置验证码");
        message.setText("您的密码重置验证码是：" + code + "，5 分钟内有效。如非本人操作，请忽略此邮件。");
        try {
            mailSender.send(message);
            System.out.println("Password reset code email sent to " + email + " from " + mailFrom);
        } catch (MailException exception) {
            System.out.println("Password reset code for " + email + ": " + code);
            throw new BizException("邮件发送失败，请检查邮箱授权码、SMTP 服务和网络；验证码已打印到后端控制台：" + exception.getMessage());
        }
    }
}
