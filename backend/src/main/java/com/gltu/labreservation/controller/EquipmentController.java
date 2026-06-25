package com.gltu.labreservation.controller;

import com.gltu.labreservation.common.ApiResponse;
import com.gltu.labreservation.entity.Equipment;
import com.gltu.labreservation.service.EquipmentService;
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
@RequestMapping("/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping
    public ApiResponse<List<Equipment>> list() {
        return ApiResponse.success(equipmentService.list());
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Equipment equipment) {
        equipmentService.save(equipment);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Equipment equipment) {
        equipment.setId(id);
        equipmentService.updateById(equipment);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        equipmentService.removeById(id);
        return ApiResponse.success();
    }
}

