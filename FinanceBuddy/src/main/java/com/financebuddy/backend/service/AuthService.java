package com.financebuddy.backend.service;

import com.financebuddy.backend.dto.AuthenticationResponse;
import com.financebuddy.backend.dto.LoginRequest;
import com.financebuddy.backend.dto.RegisterRequest;

public interface AuthService {

    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse login(LoginRequest request);
}
