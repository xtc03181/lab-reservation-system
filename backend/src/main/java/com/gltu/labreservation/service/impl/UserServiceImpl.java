package com.gltu.labreservation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gltu.labreservation.common.BizException;
import com.gltu.labreservation.common.PasswordUtil;
import com.gltu.labreservation.entity.User;
import com.gltu.labreservation.mapper.UserMapper;
import com.gltu.labreservation.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public void create(User user) {
        validateUser(user, true);
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(PasswordUtil.hash("Abc12345!"));
        } else if (!user.getPassword().startsWith("SHA256:")) {
            validatePassword(user.getPassword());
            user.setPassword(PasswordUtil.hash(user.getPassword()));
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        save(user);
    }

    @Override
    public void updateUser(Long id, User user) {
        validateUser(user, false);
        user.setId(id);
        if (StringUtils.hasText(user.getPassword()) && !user.getPassword().startsWith("SHA256:")) {
            validatePassword(user.getPassword());
            user.setPassword(PasswordUtil.hash(user.getPassword()));
        } else if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(null);
        }
        updateById(user);
    }

    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = getById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (!StringUtils.hasText(oldPassword) || !PasswordUtil.matches(oldPassword, user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        validatePassword(newPassword);
        if (PasswordUtil.matches(newPassword, user.getPassword())) {
            throw new BizException("新密码不能与旧密码相同");
        }
        User updateUser = new User();
        updateUser.setId(id);
        updateUser.setPassword(PasswordUtil.hash(newPassword));
        updateById(updateUser);
    }

    private void validateUser(User user, boolean creating) {
        if (creating && !StringUtils.hasText(user.getUsername())) {
            throw new BizException("账号不能为空");
        }
        if (!StringUtils.hasText(user.getRole())) {
            throw new BizException("请选择用户角色");
        }
        if (!"ADMIN".equals(user.getRole()) && !"TEACHER".equals(user.getRole()) && !"STUDENT".equals(user.getRole())) {
            throw new BizException("用户角色不正确");
        }
    }

    private void validatePassword(String password) {
        if (!PasswordUtil.isStrongEnough(password)) {
            throw new BizException(PasswordUtil.strengthMessage());
        }
    }
}
