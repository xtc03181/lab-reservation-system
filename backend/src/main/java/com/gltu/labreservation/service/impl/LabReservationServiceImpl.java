package com.gltu.labreservation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gltu.labreservation.common.BizException;
import com.gltu.labreservation.dto.ReviewRequest;
import com.gltu.labreservation.entity.Lab;
import com.gltu.labreservation.entity.LabReservation;
import com.gltu.labreservation.entity.ReservationRule;
import com.gltu.labreservation.mapper.LabMapper;
import com.gltu.labreservation.mapper.LabReservationMapper;
import com.gltu.labreservation.mapper.ReservationRuleMapper;
import com.gltu.labreservation.mapper.UserMapper;
import com.gltu.labreservation.service.LabReservationService;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LabReservationServiceImpl extends ServiceImpl<LabReservationMapper, LabReservation>
        implements LabReservationService {

    private final LabMapper labMapper;
    private final UserMapper userMapper;
    private final ReservationRuleMapper reservationRuleMapper;

    public LabReservationServiceImpl(LabMapper labMapper, UserMapper userMapper, ReservationRuleMapper reservationRuleMapper) {
        this.labMapper = labMapper;
        this.userMapper = userMapper;
        this.reservationRuleMapper = reservationRuleMapper;
    }

    @Override
    public void create(LabReservation reservation) {
        validateBasic(null, reservation);
        assertNoConflict(null, reservation);
        reservation.setStatus("PENDING");
        save(reservation);
    }

    @Override
    public void updateReservation(Long id, Long userId, LabReservation reservation) {
        LabReservation current = getById(id);
        if (current == null) {
            throw new BizException("预约申请不存在");
        }
        if (!current.getUserId().equals(userId)) {
            throw new BizException("只能修改自己的预约申请");
        }
        if (!"PENDING".equals(current.getStatus())) {
            throw new BizException("只能修改待审核的预约申请");
        }

        reservation.setId(id);
        reservation.setUserId(userId);
        validateBasic(id, reservation);
        assertNoConflict(id, reservation);

        current.setLabId(reservation.getLabId());
        current.setStartTime(reservation.getStartTime());
        current.setEndTime(reservation.getEndTime());
        current.setPurpose(reservation.getPurpose());
        updateById(current);
    }

    @Override
    public void cancel(Long id, Long userId) {
        LabReservation current = getById(id);
        if (current == null) {
            throw new BizException("预约申请不存在");
        }
        if (!current.getUserId().equals(userId)) {
            throw new BizException("只能取消自己的预约申请");
        }
        if (!"PENDING".equals(current.getStatus())) {
            throw new BizException("只能取消待审核的预约申请");
        }
        current.setStatus("CANCELED");
        updateById(current);
    }

    @Override
    public void review(Long id, ReviewRequest request) {
        if (!"APPROVED".equals(request.getStatus()) && !"REJECTED".equals(request.getStatus())) {
            throw new BizException("预约审核状态不正确");
        }
        LabReservation reservation = getById(id);
        if (reservation == null) {
            throw new BizException("预约申请不存在");
        }
        if (!"PENDING".equals(reservation.getStatus())) {
            throw new BizException("只能审核待审核的预约申请");
        }
        reservation.setStatus(request.getStatus());
        reservation.setReviewerId(request.getReviewerId());
        reservation.setReviewOpinion(request.getReviewOpinion());
        updateById(reservation);
    }

    private void validateBasic(Long currentId, LabReservation reservation) {
        if (reservation.getUserId() == null || userMapper.selectById(reservation.getUserId()) == null) {
            throw new BizException("申请人不存在，请重新登录后再提交");
        }
        Lab lab = reservation.getLabId() == null ? null : labMapper.selectById(reservation.getLabId());
        if (lab == null) {
            throw new BizException("实验室不存在，请选择已有实验室");
        }
        if (Integer.valueOf(0).equals(lab.getStatus())) {
            throw new BizException("该实验室已停用，不能预约");
        }
        if (reservation.getStartTime() == null || reservation.getEndTime() == null) {
            throw new BizException("请选择预约开始时间和结束时间");
        }
        if (!reservation.getEndTime().isAfter(reservation.getStartTime())) {
            throw new BizException("结束时间必须晚于开始时间");
        }
        if (!StringUtils.hasText(reservation.getPurpose())) {
            throw new BizException("请填写预约用途");
        }
        validateLabOpenTime(lab, reservation);
        validateRule(currentId, reservation);
    }

    private void validateLabOpenTime(Lab lab, LabReservation reservation) {
        LocalDate startDate = reservation.getStartTime().toLocalDate();
        LocalDate endDate = reservation.getEndTime().toLocalDate();
        if (!startDate.equals(endDate)) {
            throw new BizException("预约时间不能跨天，请选择同一天内的时间段");
        }

        String openDays = StringUtils.hasText(lab.getOpenDays()) ? lab.getOpenDays() : "1,2,3,4,5";
        Set<Integer> daySet = Arrays.stream(openDays.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(Integer::valueOf)
                .collect(Collectors.toSet());
        int dayValue = reservation.getStartTime().getDayOfWeek().getValue();
        if (!daySet.contains(dayValue)) {
            throw new BizException("该实验室当天不开放，请选择开放日期预约");
        }

        LocalTime openStart = lab.getOpenStartTime() == null ? LocalTime.of(8, 0) : lab.getOpenStartTime();
        LocalTime openEnd = lab.getOpenEndTime() == null ? LocalTime.of(18, 0) : lab.getOpenEndTime();
        if (!openEnd.isAfter(openStart)) {
            throw new BizException("实验室开放时间配置不正确，请联系管理员检查");
        }

        LocalTime start = reservation.getStartTime().toLocalTime();
        LocalTime end = reservation.getEndTime().toLocalTime();
        if (start.isBefore(openStart) || end.isAfter(openEnd)) {
            throw new BizException("预约时间必须在实验室开放时间内：" + formatTime(openStart) + "-" + formatTime(openEnd));
        }
    }

    private String formatTime(LocalTime time) {
        return time.toString().substring(0, 5);
    }

    private void validateRule(Long currentId, LabReservation reservation) {
        ReservationRule rule = activeRule();
        LocalDateTime now = LocalDateTime.now();
        if (reservation.getStartTime().isBefore(now)) {
            throw new BizException("不能预约过去的时间");
        }
        if (rule.getMaxAdvanceDays() != null
                && reservation.getStartTime().isAfter(now.plusDays(rule.getMaxAdvanceDays()))) {
            throw new BizException("最多只能提前 " + rule.getMaxAdvanceDays() + " 天预约");
        }
        if (rule.getMaxDurationHours() != null
                && Duration.between(reservation.getStartTime(), reservation.getEndTime()).toMinutes()
                > rule.getMaxDurationHours() * 60L) {
            throw new BizException("单次预约最长不能超过 " + rule.getMaxDurationHours() + " 小时");
        }
        DayOfWeek dayOfWeek = reservation.getStartTime().getDayOfWeek();
        if (Integer.valueOf(0).equals(rule.getAllowWeekend())
                && (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY)) {
            throw new BizException("当前预约规则不允许周末预约");
        }
        if (rule.getDailyLimit() != null && rule.getDailyLimit() > 0) {
            LocalDateTime dayStart = reservation.getStartTime().toLocalDate().atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);
            LambdaQueryWrapper<LabReservation> wrapper = new LambdaQueryWrapper<LabReservation>()
                    .eq(LabReservation::getUserId, reservation.getUserId())
                    .in(LabReservation::getStatus, "PENDING", "APPROVED")
                    .ge(LabReservation::getStartTime, dayStart)
                    .lt(LabReservation::getStartTime, dayEnd);
            if (currentId != null) {
                wrapper.ne(LabReservation::getId, currentId);
            }
            if (count(wrapper) >= rule.getDailyLimit()) {
                throw new BizException("每天最多只能预约 " + rule.getDailyLimit() + " 次");
            }
        }
    }

    private ReservationRule activeRule() {
        ReservationRule rule = reservationRuleMapper.selectOne(new LambdaQueryWrapper<ReservationRule>()
                .eq(ReservationRule::getStatus, 1)
                .orderByDesc(ReservationRule::getId)
                .last("LIMIT 1"));
        if (rule != null) {
            return rule;
        }
        ReservationRule defaultRule = new ReservationRule();
        defaultRule.setMaxAdvanceDays(7);
        defaultRule.setMaxDurationHours(4);
        defaultRule.setDailyLimit(2);
        defaultRule.setAllowWeekend(1);
        return defaultRule;
    }

    private void assertNoConflict(Long currentId, LabReservation reservation) {
        LambdaQueryWrapper<LabReservation> wrapper = new LambdaQueryWrapper<LabReservation>()
                .eq(LabReservation::getLabId, reservation.getLabId())
                .in(LabReservation::getStatus, "PENDING", "APPROVED")
                .lt(LabReservation::getStartTime, reservation.getEndTime())
                .gt(LabReservation::getEndTime, reservation.getStartTime());
        if (currentId != null) {
            wrapper.ne(LabReservation::getId, currentId);
        }
        long conflicts = count(wrapper);
        if (conflicts > 0) {
            throw new BizException("该时间段实验室已有预约，请更换时间");
        }
    }
}
