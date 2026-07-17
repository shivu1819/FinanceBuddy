package com.financebuddy.backend.service.impl;

import com.financebuddy.backend.dto.AuthenticationResponse;
import com.financebuddy.backend.dto.LoginRequest;
import com.financebuddy.backend.dto.RegisterRequest;
import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.entity.UserProfile;
import com.financebuddy.backend.repository.UserProfileRepository;
import com.financebuddy.backend.repository.UserRepository;
import com.financebuddy.backend.security.JwtService;
import com.financebuddy.backend.security.UserPrincipal;
import com.financebuddy.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .enabled(true)
                .emailVerified(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .build();

        User savedUser = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(savedUser)
                .fullName(savedUser.getFullName())
                .build();
        userProfileRepository.save(profile);

        return buildAuthenticationResponse(savedUser, null);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticationResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = findUserByEmail(request.getEmail());

        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);

        return buildAuthenticationResponse(user, accessToken);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private AuthenticationResponse buildAuthenticationResponse(User user, String accessToken) {
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .emailVerified(user.getEmailVerified())
                .build();
    }
}
