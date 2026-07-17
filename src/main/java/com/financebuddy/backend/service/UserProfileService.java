package com.financebuddy.backend.service;

import com.financebuddy.backend.dto.UserProfileRequest;
import com.financebuddy.backend.dto.UserProfileResponse;

public interface UserProfileService {

    UserProfileResponse getProfile(Long userId);

    UserProfileResponse updateProfile(Long userId, UserProfileRequest request);
}
