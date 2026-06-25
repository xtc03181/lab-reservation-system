package com.gltu.labreservation.service;

import com.gltu.labreservation.dto.LoginRequest;
import com.gltu.labreservation.dto.LoginResponse;
import com.gltu.labreservation.dto.ResetPasswordRequest;
import com.gltu.labreservation.dto.SendResetCodeRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void sendResetCode(SendResetCodeRequest request);

    void resetPassword(ResetPasswordRequest request);
}
