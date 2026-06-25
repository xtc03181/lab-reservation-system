package com.gltu.labreservation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gltu.labreservation.common.BizException;
import com.gltu.labreservation.dto.ReviewRequest;
import com.gltu.labreservation.entity.LabReservation;
import com.gltu.labreservation.mapper.LabMapper;
import com.gltu.labreservation.mapper.LabReservationMapper;
import com.gltu.labreservation.mapper.UserMapper;
import com.gltu.labreservation.service.LabReservationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LabReservationServiceImpl extends ServiceImpl<LabReservationMapper, LabReservation>
        implements LabReservationService {

    private final LabMapper labMapper;
    private final UserMapper userMapper;

    public LabReservationServiceImpl(LabMapper labMapper, UserMapper userMapper) {
        this.labMapper = labMapper;
        this.userMapper = userMapper;
    }

    @Override
    public void create(LabReservation reservation) {
        validateBasic(reservation);
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
        validateBasic(reservation);
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

    private void validateBasic(LabReservation reservation) {
        if (reservation.getUserId() == null || userMapper.selectById(reservation.getUserId()) == null) {
            throw new BizException("申请人不存在，请重新登录后再提交");
        }
        if (reservation.getLabId() == null || labMapper.selectById(reservation.getLabId()) == null) {
            throw new BizException("实验室不存在，请选择已有实验室");
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
