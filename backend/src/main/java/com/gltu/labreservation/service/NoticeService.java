package com.gltu.labreservation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gltu.labreservation.entity.Notice;
import java.util.List;

public interface NoticeService extends IService<Notice> {

    List<Notice> listWithCache(String role);

    void clearNoticeCache();
}
