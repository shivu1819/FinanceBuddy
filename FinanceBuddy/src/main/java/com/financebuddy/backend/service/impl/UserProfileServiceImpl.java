package com.financebuddy.backend.service.impl;

import com.financebuddy.backend.dto.UserProfileRequest;
import com.financebuddy.backend.dto.UserProfileResponse;
import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.entity.UserProfile;
import com.financebuddy.backend.repository.UserProfileRepository;
import com.financebuddy.backend.repository.UserRepository;
import com.financebuddy.backend.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public UserProfileResponse getProfile(Long userId) {
        UserProfile profile = findOrCreateProfile(userId);
        return mapToResponse(profile);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserProfileRequest request) {
        UserProfile profile = findOrCreateProfile(userId);

        profile.setFullName(request.getFullName());
        profile.setAge(request.getAge());
        profile.setCountry(request.getCountry());
        profile.setOccupation(request.getOccupation());
        profile.setMonthlyIncome(request.getMonthlyIncome());
        profile.setCurrencyCode(request.getCurrency());

        User user = profile.getUser();
        user.setFullName(request.getFullName());
        userRepository.save(user);

        return mapToResponse(userProfileRepository.save(profile));
    }

    private UserProfile findOrCreateProfile(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found"));

                    UserProfile profile = UserProfile.builder()
                            .user(user)
                            .fullName(user.getFullName())
                            .currencyCode("INR")
                            .build();

                    return userProfileRepository.save(profile);
                });
    }

    private UserProfileResponse mapToResponse(UserProfile profile) {
        return UserProfileResponse.builder()
                .fullName(profile.getFullName())
                .age(profile.getAge())
                .country(profile.getCountry())
                .occupation(profile.getOccupation())
                .monthlyIncome(profile.getMonthlyIncome())
                .currency(profile.getCurrencyCode())
                .build();
    }
}
