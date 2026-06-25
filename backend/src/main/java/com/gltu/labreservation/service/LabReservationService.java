package com.gltu.labreservation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gltu.labreservation.dto.ReviewRequest;
import com.gltu.labreservation.entity.LabReservation;

public interface LabReservationService extends IService<LabReservation> {

    void create(LabReservation reservation);

    void updateReservation(Long id, Long userId, LabReservation reservation);

    void cancel(Long id, Long userId);

    void review(Long id, ReviewRequest request);
}
