package com.gltu.labreservation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gltu.labreservation.common.ApiResponse;
import com.gltu.labreservation.entity.Message;
import com.gltu.labreservation.service.MessageService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ApiResponse<List<Message>> list(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(messageService.list(new LambdaQueryWrapper<Message>()
                .eq(Message::getReceiverId, userId)
                .orderByAsc(Message::getReadStatus)
                .orderByDesc(Message::getCreateTime)));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(messageService.count(new LambdaQueryWrapper<Message>()
                .eq(Message::getReceiverId, userId)
                .eq(Message::getReadStatus, 0)));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> read(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        Message message = messageService.getById(id);
        if (message != null && userId.equals(message.getReceiverId())) {
            message.setReadStatus(1);
            messageService.updateById(message);
        }
        return ApiResponse.success();
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> readAll(@RequestHeader("X-User-Id") Long userId) {
        Message message = new Message();
        message.setReadStatus(1);
        messageService.update(message, new LambdaQueryWrapper<Message>()
                .eq(Message::getReceiverId, userId)
                .eq(Message::getReadStatus, 0));
        return ApiResponse.success();
    }
}
