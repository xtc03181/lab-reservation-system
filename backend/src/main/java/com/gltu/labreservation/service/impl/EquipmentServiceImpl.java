package com.gltu.labreservation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gltu.labreservation.entity.Equipment;
import com.gltu.labreservation.mapper.EquipmentMapper;
import com.gltu.labreservation.service.EquipmentService;
import org.springframework.stereotype.Service;

@Service
public class EquipmentServiceImpl extends ServiceImpl<EquipmentMapper, Equipment> implements EquipmentService {
}

