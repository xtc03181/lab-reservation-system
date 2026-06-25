package com.gltu.labreservation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gltu.labreservation.common.BizException;
import com.gltu.labreservation.dto.ReviewRequest;
import com.gltu.labreservation.entity.Equipment;
import com.gltu.labreservation.entity.EquipmentBorrow;
import com.gltu.labreservation.mapper.EquipmentBorrowMapper;
import com.gltu.labreservation.mapper.EquipmentMapper;
import com.gltu.labreservation.mapper.UserMapper;
import com.gltu.labreservation.service.EquipmentBorrowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class EquipmentBorrowServiceImpl extends ServiceImpl<EquipmentBorrowMapper, EquipmentBorrow>
        implements EquipmentBorrowService {

    private final EquipmentMapper equipmentMapper;
    private final UserMapper userMapper;

    public EquipmentBorrowServiceImpl(EquipmentMapper equipmentMapper, UserMapper userMapper) {
        this.equipmentMapper = equipmentMapper;
        this.userMapper = userMapper;
    }

    @Override
    public void create(EquipmentBorrow borrow) {
        if (borrow.getUserId() == null || userMapper.selectById(borrow.getUserId()) == null) {
            throw new BizException("申请人不存在，请重新登录后再提交");
        }
        Equipment equipment = borrow.getEquipmentId() == null ? null : equipmentMapper.selectById(borrow.getEquipmentId());
        if (equipment == null) {
            throw new BizException("设备不存在，请选择已有设备");
        }
        if (borrow.getBorrowCount() == null || borrow.getBorrowCount() <= 0) {
            throw new BizException("借用数量必须大于 0");
        }
        if (equipment.getAvailableCount() != null && borrow.getBorrowCount() > equipment.getAvailableCount()) {
            throw new BizException("借用数量不能超过当前可借数量");
        }
        if (borrow.getBorrowTime() == null || borrow.getReturnTime() == null) {
            throw new BizException("请选择借用时间和预计归还时间");
        }
        if (!borrow.getReturnTime().isAfter(borrow.getBorrowTime())) {
            throw new BizException("预计归还时间必须晚于借用时间");
        }
        if (!StringUtils.hasText(borrow.getPurpose())) {
            throw new BizException("请填写借用用途");
        }
        borrow.setStatus("PENDING");
        save(borrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(Long id, ReviewRequest request) {
        if (!"APPROVED".equals(request.getStatus())
                && !"REJECTED".equals(request.getStatus())
                && !"RETURNED".equals(request.getStatus())) {
            throw new BizException("设备借用处理状态不正确");
        }
        EquipmentBorrow borrow = getById(id);
        if (borrow == null) {
            throw new BizException("设备借用申请不存在");
        }

        Equipment equipment = equipmentMapper.selectById(borrow.getEquipmentId());
        if (equipment == null) {
            throw new BizException("设备不存在，无法审核");
        }

        if ("APPROVED".equals(request.getStatus())) {
            if (!"PENDING".equals(borrow.getStatus())) {
                throw new BizException("只能通过待审核的借用申请");
            }
            if (equipment.getAvailableCount() == null || equipment.getAvailableCount() < borrow.getBorrowCount()) {
                throw new BizException("设备可借数量不足，无法通过申请");
            }
            equipment.setAvailableCount(equipment.getAvailableCount() - borrow.getBorrowCount());
            equipmentMapper.updateById(equipment);
        } else if ("RETURNED".equals(request.getStatus())) {
            if (!"APPROVED".equals(borrow.getStatus())) {
                throw new BizException("只有已通过的借用记录才能归还");
            }
            equipment.setAvailableCount(equipment.getAvailableCount() + borrow.getBorrowCount());
            equipmentMapper.updateById(equipment);
        } else if (!"PENDING".equals(borrow.getStatus())) {
            throw new BizException("只能驳回待审核的借用申请");
        }

        borrow.setStatus(request.getStatus());
        borrow.setReviewerId(request.getReviewerId());
        borrow.setReviewOpinion(request.getReviewOpinion());
        updateById(borrow);
    }
}
