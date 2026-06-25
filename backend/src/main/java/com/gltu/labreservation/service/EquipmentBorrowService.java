package com.gltu.labreservation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gltu.labreservation.dto.ReviewRequest;
import com.gltu.labreservation.entity.EquipmentBorrow;

public interface EquipmentBorrowService extends IService<EquipmentBorrow> {

    void create(EquipmentBorrow borrow);

    void review(Long id, ReviewRequest request);
}
