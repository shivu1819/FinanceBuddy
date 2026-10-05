package com.financebuddy.backend.goal;

import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.UserRepository;
import com.financebuddy.backend.util.FinancialCalculationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        validateTargetDate(request.getTargetDate());
        BigDecimal savedAmount = request.getSavedAmount() == null
                ? BigDecimal.ZERO
                : request.getSavedAmount();
        if (savedAmount.compareTo(request.getTargetAmount()) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Saved amount cannot exceed the target amount."
            );
        }
        User user = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        Goal goal = Goal.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .targetAmount(request.getTargetAmount())
                .savedAmount(savedAmount)
                .targetDate(request.getTargetDate())
                .status(savedAmount.compareTo(request.getTargetAmount()) >= 0
                        ? GoalStatus.COMPLETED
                        : GoalStatus.IN_PROGRESS)
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
        validateTargetDate(request.getTargetDate());
        User user = getCurrentUser();
        Goal goal = getOwnedGoalForUpdate(id, user);

        if (request.getTargetAmount().compareTo(goal.getSavedAmount()) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Target amount cannot be less than the amount already saved."
            );
        }

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
        if (request == null || request.getAmount() == null
                || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero.");
        }

        User user = getCurrentUser();
        Goal goal = getOwnedGoalForUpdate(id, user);

        if (goal.getStatus() == GoalStatus.COMPLETED
                || goal.getSavedAmount().compareTo(goal.getTargetAmount()) >= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Completed goal cannot accept additional contributions."
            );
        }

        BigDecimal remainingAmount = goal.getTargetAmount().subtract(goal.getSavedAmount());
        if (request.getAmount().compareTo(remainingAmount) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Contribution cannot exceed the remaining goal amount."
            );
        }

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Goal not found."));
    }

    private Goal getOwnedGoalForUpdate(Long id, User user) {
        return goalRepository.findForUpdateByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Goal not found."));
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
        BigDecimal remainingAmount = FinancialCalculationUtils.nonNegative(
                goal.getTargetAmount().subtract(goal.getSavedAmount())
        );
        GoalStatus responseStatus = goal.getSavedAmount().compareTo(goal.getTargetAmount()) >= 0
                ? GoalStatus.COMPLETED
                : GoalStatus.IN_PROGRESS;

        return GoalResponse.builder()
                .id(goal.getId())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .targetAmount(goal.getTargetAmount())
                .savedAmount(goal.getSavedAmount())
                .remainingAmount(remainingAmount)
                .progressPercentage(FinancialCalculationUtils.calculateGoalProgress(
                        goal.getSavedAmount(),
                        goal.getTargetAmount()
                ))
                .targetDate(goal.getTargetDate())
                .status(responseStatus)
                .build();
    }

    private void updateGoalStatus(Goal goal) {
        if (goal.getSavedAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
            return;
        }

        goal.setStatus(GoalStatus.IN_PROGRESS);
    }

    private void validateTargetDate(LocalDate targetDate) {
        if (targetDate != null && !targetDate.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target date must be in the future.");
        }
    }
}
