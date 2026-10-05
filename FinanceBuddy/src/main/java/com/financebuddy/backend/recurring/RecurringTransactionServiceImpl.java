package com.financebuddy.backend.recurring;

import com.financebuddy.backend.entity.*;
import com.financebuddy.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.List;

@Service @RequiredArgsConstructor
public class RecurringTransactionServiceImpl implements RecurringTransactionService {
    private final RecurringTransactionRepository repository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BankAccountRepository bankAccountRepository;

    @Override @Transactional(readOnly = true)
    public List<RecurringTransactionResponse> getAll() { User user = currentUser(); return repository.findByUserId(user.getId()).stream().map(this::map).toList(); }

    @Override @Transactional
    public RecurringTransactionResponse create(RecurringTransactionRequest request) { User user = currentUser(); return map(repository.save(build(request, user, new RecurringTransaction()))); }

    @Override @Transactional
    public RecurringTransactionResponse update(Long id, RecurringTransactionRequest request) { User user = currentUser(); return map(repository.save(build(request, user, owned(id, user)))); }

    @Override @Transactional
    public RecurringTransactionResponse setActive(Long id, boolean active) { User user = currentUser(); RecurringTransaction item = owned(id, user); item.setActive(active); return map(repository.save(item)); }

    @Override @Transactional
    public void delete(Long id) { User user = currentUser(); repository.delete(owned(id, user)); }

    private RecurringTransaction build(RecurringTransactionRequest request, User user, RecurringTransaction item) {
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must not precede start date.");
        Category category = categoryRepository.findByIdAndUserId(request.getCategoryId(), user.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found."));
        if (!request.getTransactionType().equals(category.getType())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category type must match transaction type.");
        BankAccount account = request.getBankAccountId() == null
                ? bankAccountRepository.findByUserIdAndActiveTrue(user.getId()).stream().findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Create an active bank account before adding a recurring transaction."))
                : bankAccountRepository.findByIdAndUserId(request.getBankAccountId(), user.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank account not found."));
        LocalDate next = item.getNextRunDate() == null ? request.getStartDate() : item.getNextRunDate();
        item.setUser(user); item.setAmount(request.getAmount()); item.setTransactionType(request.getTransactionType()); item.setCategory(category); item.setBankAccount(account); item.setDescription(request.getDescription()); item.setFrequency(request.getFrequency()); item.setStartDate(request.getStartDate()); item.setEndDate(request.getEndDate()); item.setNextRunDate(next); item.setActive(request.getActive() == null || request.getActive());
        return item;
    }
    private RecurringTransaction owned(Long id, User user) { return repository.findByIdAndUserId(id, user.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurring transaction not found.")); }
    private User currentUser() { var auth = SecurityContextHolder.getContext().getAuthentication(); if (auth == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required."); return userRepository.findByEmail(auth.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found.")); }
    private RecurringTransactionResponse map(RecurringTransaction item) { return RecurringTransactionResponse.builder().id(item.getId()).amount(item.getAmount()).transactionType(item.getTransactionType()).categoryId(item.getCategory().getId()).categoryName(item.getCategory().getName()).bankAccountId(item.getBankAccount() == null ? null : item.getBankAccount().getId()).description(item.getDescription()).frequency(item.getFrequency()).startDate(item.getStartDate()).endDate(item.getEndDate()).nextRunDate(item.getNextRunDate()).active(item.getActive()).build(); }
}
