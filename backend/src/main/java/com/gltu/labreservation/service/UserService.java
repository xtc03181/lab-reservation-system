package com.gltu.labreservation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gltu.labreservation.entity.User;

public interface UserService extends IService<User> {

    void create(User user);

    void updateUser(Long id, User user);

    void changePassword(Long id, String oldPassword, String newPassword);
}
