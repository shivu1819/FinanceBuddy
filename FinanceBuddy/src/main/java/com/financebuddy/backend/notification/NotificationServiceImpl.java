package com.financebuddy.backend.notification;

import com.financebuddy.backend.entity.Notification;
import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.NotificationRepository;
import com.financebuddy.backend.repository.UserRepository;
import com.financebuddy.backend.budget.Budget;
import com.financebuddy.backend.budget.BudgetRepository;
import com.financebuddy.backend.entity.RecurringTransaction;
import com.financebuddy.backend.goal.Goal;
import com.financebuddy.backend.goal.GoalRepository;
import com.financebuddy.backend.repository.RecurringTransactionRepository;
import com.financebuddy.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.time.LocalDate;
import java.time.YearMonth;
import java.math.BigDecimal;

@Service @RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository repository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final GoalRepository goalRepository;
    private final RecurringTransactionRepository recurringTransactionRepository;
    @Override @Transactional public List<NotificationResponse> getAll() { User user=current(); refresh(user); return repository.findByUserId(user.getId()).stream().sorted((a,b) -> b.getCreatedAt().compareTo(a.getCreatedAt())).map(this::map).toList(); }
    @Override @Transactional(readOnly = true) public long unreadCount() { return repository.findByUserIdAndReadStatusFalse(current().getId()).size(); }
    @Override @Transactional public NotificationResponse markRead(Long id) { User user=current(); Notification n=repository.findById(id).filter(item -> item.getUser().getId().equals(user.getId())).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Notification not found.")); n.setReadStatus(true); return map(repository.save(n)); }
    @Override @Transactional public void markAllRead() { repository.findByUserIdAndReadStatusFalse(current().getId()).forEach(n -> n.setReadStatus(true)); }
    private User current(){var auth=SecurityContextHolder.getContext().getAuthentication(); if(auth==null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Authentication is required."); return userRepository.findByEmail(auth.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Authenticated user not found."));}
    private NotificationResponse map(Notification n){return NotificationResponse.builder().id(n.getId()).title(n.getTitle()).message(n.getMessage()).notificationType(n.getNotificationType()).readStatus(n.getReadStatus()).createdAt(n.getCreatedAt()).build();}
    private void refresh(User user) {
        YearMonth month=YearMonth.now();
        for(Budget budget: budgetRepository.findAllByUserAndBudgetMonth(user,month)) {
            BigDecimal spent=transactionRepository.sumAmountByUserIdAndTransactionTypeAndDateBetween(user.getId(),"EXPENSE",month.atDay(1),month.atEndOfMonth());
            int percent=budget.getMonthlyLimit().signum()==0?0:spent.multiply(BigDecimal.valueOf(100)).divide(budget.getMonthlyLimit(),0,java.math.RoundingMode.HALF_UP).intValue();
            if(percent>=80) add(user,"BUDGET", "Budget " + (percent>=100?"exceeded":"nearing limit"), "Your budget is " + percent + "% used.");
        }
        for(Goal goal: goalRepository.findByUserOrderByCreatedAtDesc(user)) if(goal.getTargetAmount().signum()>0 && goal.getSavedAmount().multiply(BigDecimal.valueOf(100)).divide(goal.getTargetAmount(),0,java.math.RoundingMode.HALF_UP).intValue()>=80) add(user,"GOAL",goal.getTitle()+" goal progress","Your goal is close to completion.");
        for(RecurringTransaction item: recurringTransactionRepository.findByUserIdAndActiveTrue(user.getId())) if(!item.getNextRunDate().isAfter(LocalDate.now().plusDays(3))) add(user,"RECURRING", "Recurring transaction reminder", "A " + item.getFrequency().toLowerCase() + " transaction is due on " + item.getNextRunDate() + ".");
    }
    private void add(User user,String type,String title,String message){if(!repository.existsByUserIdAndNotificationTypeAndTitle(user.getId(),type,title)) repository.save(Notification.builder().user(user).notificationType(type).title(title).message(message).readStatus(false).build());}
}
