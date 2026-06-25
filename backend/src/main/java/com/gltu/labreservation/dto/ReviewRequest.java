package com.gltu.labreservation.dto;

import jakarta.validation.constraints.NotBlank;

public class ReviewRequest {

    private Long reviewerId;

    @NotBlank
    private String status;

    private String reviewOpinion;

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReviewOpinion() {
        return reviewOpinion;
    }

    public void setReviewOpinion(String reviewOpinion) {
        this.reviewOpinion = reviewOpinion;
    }
}
