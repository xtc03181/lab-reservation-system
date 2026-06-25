package com.gltu.labreservation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gltu.labreservation.common.ApiResponse;
import com.gltu.labreservation.entity.Notice;
import com.gltu.labreservation.entity.User;
import com.gltu.labreservation.mapper.UserMapper;
import com.gltu.labreservation.service.NoticeService;
import com.gltu.labreservation.service.OperationLogService;
import java.util.List;
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
@RequestMapping("/notices")
public class NoticeController {

    private final NoticeService noticeService;
    private final OperationLogService operationLogService;
    private final UserMapper userMapper;

    public NoticeController(NoticeService noticeService,
                            OperationLogService operationLogService,
                            UserMapper userMapper) {
        this.noticeService = noticeService;
        this.operationLogService = operationLogService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ApiResponse<List<Notice>> list(@RequestHeader("X-User-Role") String role) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<Notice>()
                .orderByDesc(Notice::getStatus)
                .orderByDesc(Notice::getCreateTime);
        if (!"ADMIN".equalsIgnoreCase(role)) {
            wrapper.in(Notice::getStatus, 1, 2);
        }
        return ApiResponse.success(noticeService.list(wrapper));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestHeader("X-User-Id") Long userId, @RequestBody Notice notice) {
        notice.setPublisherId(userId);
        notice.setStatus(1);
        noticeService.save(notice);
        record(userId, "公告管理", "发布公告", notice.getTitle());
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id,
                                    @RequestHeader("X-User-Id") Long userId,
                                    @RequestBody Notice notice) {
        notice.setId(id);
        notice.setPublisherId(null);
        noticeService.updateById(notice);
        record(userId, "公告管理", "编辑公告", "公告ID：" + id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/pin")
    public ApiResponse<Void> pin(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        updateStatus(id, 2);
        record(userId, "公告管理", "置顶公告", "公告ID：" + id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/normal")
    public ApiResponse<Void> normal(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        updateStatus(id, 1);
        record(userId, "公告管理", "上架公告", "公告ID：" + id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/offline")
    public ApiResponse<Void> offline(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        updateStatus(id, 0);
        record(userId, "公告管理", "下架公告", "公告ID：" + id);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        noticeService.removeById(id);
        record(userId, "公告管理", "删除公告", "公告ID：" + id);
        return ApiResponse.success();
    }

    private void updateStatus(Long id, Integer status) {
        Notice notice = new Notice();
        notice.setId(id);
        notice.setStatus(status);
        noticeService.updateById(notice);
    }

    private void record(Long userId, String moduleName, String operation, String detail) {
        User user = userMapper.selectById(userId);
        String username = user == null ? String.valueOf(userId) : user.getUsername();
        operationLogService.record(userId, username, moduleName, operation, detail);
    }
}
