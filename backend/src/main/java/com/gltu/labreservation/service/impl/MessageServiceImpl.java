package com.gltu.labreservation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gltu.labreservation.entity.Message;
import com.gltu.labreservation.entity.User;
import com.gltu.labreservation.mapper.MessageMapper;
import com.gltu.labreservation.mapper.UserMapper;
import com.gltu.labreservation.service.MessageService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final UserMapper userMapper;

    public MessageServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void send(Long receiverId, String title, String content, String type) {
        if (receiverId == null) {
            return;
        }
        Message message = new Message();
        message.setReceiverId(receiverId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setReadStatus(0);
        save(message);
    }

    @Override
    public void sendToRoles(List<String> roles, String title, String content, String type) {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .in(User::getRole, roles)
                .eq(User::getStatus, 1));
        users.forEach(user -> send(user.getId(), title, content, type));
    }
}
