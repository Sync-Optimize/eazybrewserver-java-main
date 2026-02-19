package com.eazybrew.vend.service;

import com.eazybrew.vend.dto.request.*;
import com.eazybrew.vend.dto.response.JwtResponse;

public interface AuthService {
    String signup(SignupRequest signupRequest);
    JwtResponse signIn(LoginRequest loginRequest);
    // New: reset a user’s initial password
    void resetPassword(ResetPasswordRequest resetRequest);

    /**
     * Initiate forgot password flow by sending reset link to user's email
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * Reset password using a token from forgot password email
     */
    void resetForgotPassword(ResetForgotPasswordRequest request);
}
