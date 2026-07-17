package com.financebuddy.backend.goal;

import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public GoalResponse createGoal(GoalRequest request) {
        User user = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        Goal goal = Goal.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .targetAmount(request.getTargetAmount())
                .savedAmount(BigDecimal.ZERO)
                .targetDate(request.getTargetDate())
                .status(GoalStatus.IN_PROGRESS)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return mapToResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalResponse> getGoals() {
        User user = getCurrentUser();
        return goalRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GoalResponse getGoalById(Long id) {
        User user = getCurrentUser();
        return mapToResponse(getOwnedGoal(id, user));
    }

    @Override
    @Transactional
    public GoalResponse updateGoal(Long id, GoalRequest request) {
        User user = getCurrentUser();
        Goal goal = getOwnedGoal(id, user);

        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        goal.setUpdatedAt(LocalDateTime.now());
        updateGoalStatus(goal);

        return mapToResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public GoalResponse addMoney(Long id, GoalSavingsRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero.");
        }

        User user = getCurrentUser();
        Goal goal = getOwnedGoal(id, user);

        goal.setSavedAmount(goal.getSavedAmount().add(request.getAmount()));
        goal.setUpdatedAt(LocalDateTime.now());
        updateGoalStatus(goal);

        return mapToResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public void deleteGoal(Long id) {
        User user = getCurrentUser();
        Goal goal = getOwnedGoal(id, user);
        goalRepository.delete(goal);
    }

    private Goal getOwnedGoal(Long id, User user) {
        return goalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> {
                    if (goalRepository.existsById(id)) {
                        return new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
                    }
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Goal not found.");
                });
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found."
                ));
    }

    private GoalResponse mapToResponse(Goal goal) {
        updateGoalStatus(goal);
        BigDecimal remainingAmount = goal.getTargetAmount().subtract(goal.getSavedAmount());
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        return GoalResponse.builder()
                .id(goal.getId())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .targetAmount(goal.getTargetAmount())
                .savedAmount(goal.getSavedAmount())
                .remainingAmount(remainingAmount)
                .progressPercentage(calculateProgressPercentage(goal.getSavedAmount(), goal.getTargetAmount()))
                .targetDate(goal.getTargetDate())
                .status(goal.getStatus())
                .build();
    }

    private void updateGoalStatus(Goal goal) {
        if (goal.getSavedAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
            return;
        }

        goal.setStatus(GoalStatus.IN_PROGRESS);
    }

    private Integer calculateProgressPercentage(BigDecimal savedAmount, BigDecimal targetAmount) {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }

        int percentage = savedAmount.multiply(BigDecimal.valueOf(100))
                .divide(targetAmount, 0, RoundingMode.HALF_UP)
                .intValue();

        return Math.min(percentage, 100);
    }
}
