package com.gltu.labreservation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gltu.labreservation.entity.Message;
import java.util.List;

public interface MessageService extends IService<Message> {

    void send(Long receiverId, String title, String content, String type);

    void sendToRoles(List<String> roles, String title, String content, String type);
}
