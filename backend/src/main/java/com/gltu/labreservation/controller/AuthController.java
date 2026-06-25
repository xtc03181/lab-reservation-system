package com.gltu.labreservation.controller;

import com.gltu.labreservation.common.ApiResponse;
import com.gltu.labreservation.dto.LoginRequest;
import com.gltu.labreservation.dto.LoginResponse;
import com.gltu.labreservation.dto.ResetPasswordRequest;
import com.gltu.labreservation.dto.SendResetCodeRequest;
import com.gltu.labreservation.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/send-reset-code")
    public ApiResponse<Void> sendResetCode(@RequestBody SendResetCodeRequest request) {
        authService.sendResetCode(request);
        return ApiResponse.success();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success();
    }
}
