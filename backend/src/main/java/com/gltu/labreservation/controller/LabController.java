package com.gltu.labreservation.controller;

import com.gltu.labreservation.common.ApiResponse;
import com.gltu.labreservation.entity.Lab;
import com.gltu.labreservation.service.LabService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/labs")
public class LabController {

    private final LabService labService;

    public LabController(LabService labService) {
        this.labService = labService;
    }

    @GetMapping
    public ApiResponse<List<Lab>> list() {
        return ApiResponse.success(labService.list());
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Lab lab) {
        labService.save(lab);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Lab lab) {
        lab.setId(id);
        labService.updateById(lab);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        labService.removeById(id);
        return ApiResponse.success();
    }
}

