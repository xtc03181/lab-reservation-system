package com.gltu.labreservation.controller;

import com.gltu.labreservation.common.ApiResponse;
import com.gltu.labreservation.entity.User;
import com.gltu.labreservation.service.OperationLogService;
import com.gltu.labreservation.service.UserService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final OperationLogService operationLogService;

    public UserController(UserService userService, OperationLogService operationLogService) {
        this.userService = userService;
        this.operationLogService = operationLogService;
    }

    @GetMapping
    public ApiResponse<List<User>> list() {
        List<User> users = userService.list();
        users.forEach(user -> user.setPassword(null));
        return ApiResponse.success(users);
    }

    @GetMapping("/me")
    public ApiResponse<User> me(@RequestHeader("X-User-Id") Long userId) {
        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return ApiResponse.success(user);
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestHeader("X-User-Id") Long operatorId, @RequestBody User user) {
        userService.create(user);
        record(operatorId, "用户管理", "新增用户", "账号：" + user.getUsername());
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id,
                                    @RequestHeader("X-User-Id") Long operatorId,
                                    @RequestBody User user) {
        userService.updateUser(id, user);
        record(operatorId, "用户管理", "修改用户", "用户ID：" + id);
        return ApiResponse.success();
    }

    @PutMapping("/me")
    public ApiResponse<Void> updateMe(@RequestHeader("X-User-Id") Long userId, @RequestBody User user) {
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setRealName(user.getRealName());
        updateUser.setPhone(user.getPhone());
        updateUser.setEmail(user.getEmail());
        userService.updateById(updateUser);
        record(userId, "个人中心", "修改个人资料", "更新姓名或电话");
        return ApiResponse.success();
    }

    @PutMapping("/me/password")
    public ApiResponse<Void> changePassword(@RequestHeader("X-User-Id") Long userId,
                                            @RequestBody Map<String, String> request) {
        userService.changePassword(userId, request.get("oldPassword"), request.get("newPassword"));
        record(userId, "个人中心", "修改密码", "用户修改自己的登录密码");
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @RequestHeader("X-User-Id") Long operatorId) {
        userService.removeById(id);
        record(operatorId, "用户管理", "删除用户", "用户ID：" + id);
        return ApiResponse.success();
    }

    private void record(Long userId, String moduleName, String operation, String detail) {
        User user = userService.getById(userId);
        String username = user == null ? String.valueOf(userId) : user.getUsername();
        operationLogService.record(userId, username, moduleName, operation, detail);
    }
}
