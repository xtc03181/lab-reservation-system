package com.gltu.labreservation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gltu.labreservation.entity.Notice;
import com.gltu.labreservation.mapper.NoticeMapper;
import com.gltu.labreservation.service.NoticeService;
import org.springframework.stereotype.Service;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {
}

