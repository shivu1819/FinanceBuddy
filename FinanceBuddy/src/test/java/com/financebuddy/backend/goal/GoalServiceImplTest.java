package com.financebuddy.backend.goal;

import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalServiceImplTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GoalServiceImpl goalService;

    private User user;

    @BeforeEach
    void setUpAuthentication() {
        user = User.builder().id(11L).email("goal@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null)
        );
        lenient().when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void contributionCannotExceedRemainingAmount() {
        Goal goal = goal(new BigDecimal("1000.00"), new BigDecimal("800.00"), GoalStatus.IN_PROGRESS);
        when(goalRepository.findForUpdateByIdAndUser(goal.getId(), user)).thenReturn(Optional.of(goal));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> goalService.addMoney(
                        goal.getId(),
                        GoalSavingsRequest.builder().amount(new BigDecimal("200.01")).build()
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(goalRepository, never()).save(any());
    }

    @Test
    void completedGoalRejectsFurtherContributions() {
        Goal goal = goal(new BigDecimal("1000.00"), new BigDecimal("1000.00"), GoalStatus.COMPLETED);
        when(goalRepository.findForUpdateByIdAndUser(goal.getId(), user)).thenReturn(Optional.of(goal));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> goalService.addMoney(
                        goal.getId(),
                        GoalSavingsRequest.builder().amount(BigDecimal.ONE).build()
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(goalRepository, never()).save(any());
    }

    @Test
    void exactRemainingContributionCompletesGoalAtOneHundredPercent() {
        Goal goal = goal(new BigDecimal("1000.00"), new BigDecimal("800.00"), GoalStatus.IN_PROGRESS);
        when(goalRepository.findForUpdateByIdAndUser(goal.getId(), user)).thenReturn(Optional.of(goal));
        when(goalRepository.save(goal)).thenReturn(goal);

        GoalResponse response = goalService.addMoney(
                goal.getId(),
                GoalSavingsRequest.builder().amount(new BigDecimal("200.00")).build()
        );

        assertEquals(0, response.getRemainingAmount().compareTo(BigDecimal.ZERO));
        assertEquals(100, response.getProgressPercentage());
        assertEquals(GoalStatus.COMPLETED, response.getStatus());
    }

    @Test
    void pastTargetDateIsRejected() {
        GoalRequest request = GoalRequest.builder()
                .title("Past goal")
                .targetAmount(new BigDecimal("100.00"))
                .targetDate(LocalDate.now().minusDays(1))
                .build();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> goalService.createGoal(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(goalRepository, never()).save(any());
    }

    @Test
    void zeroAndNegativeContributionsAreRejected() {
        assertThrows(
                ResponseStatusException.class,
                () -> goalService.addMoney(
                        31L,
                        GoalSavingsRequest.builder().amount(BigDecimal.ZERO).build()
                )
        );
        assertThrows(
                ResponseStatusException.class,
                () -> goalService.addMoney(
                        31L,
                        GoalSavingsRequest.builder().amount(new BigDecimal("-1.00")).build()
                )
        );
        verify(goalRepository, never()).findForUpdateByIdAndUser(any(), any());
    }

    private Goal goal(BigDecimal target, BigDecimal saved, GoalStatus status) {
        return Goal.builder()
                .id(31L)
                .user(user)
                .title("Emergency fund")
                .targetAmount(target)
                .savedAmount(saved)
                .targetDate(LocalDate.now().plusMonths(2))
                .status(status)
                .build();
    }
}
