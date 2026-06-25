package com.gltu.labreservation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gltu.labreservation.entity.Lab;
import com.gltu.labreservation.mapper.LabMapper;
import com.gltu.labreservation.service.LabService;
import org.springframework.stereotype.Service;

@Service
public class LabServiceImpl extends ServiceImpl<LabMapper, Lab> implements LabService {
}

