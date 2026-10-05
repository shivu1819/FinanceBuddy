package com.financebuddy.backend.investment;

import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvestmentServiceImpl implements InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public InvestmentResponse createInvestment(InvestmentRequest request) {
        Investment investment = Investment.builder()
                .user(getCurrentUser())
                .investmentName(request.getInvestmentName())
                .investmentType(request.getInvestmentType())
                .investedAmount(request.getInvestedAmount())
                .currentValue(request.getCurrentValue())
                .investmentDate(request.getInvestmentDate())
                .expectedReturn(request.getExpectedReturn())
                .riskLevel(request.getRiskLevel())
                .notes(request.getNotes())
                .build();

        return mapToResponse(investmentRepository.save(investment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestmentResponse> getAllInvestments() {
        User user = getCurrentUser();
        return investmentRepository.findByUserOrderByInvestmentDateDescCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InvestmentResponse getInvestmentById(Long id) {
        return mapToResponse(getOwnedInvestment(id, getCurrentUser()));
    }

    @Override
    @Transactional
    public InvestmentResponse updateInvestment(Long id, InvestmentRequest request) {
        Investment investment = getOwnedInvestmentForUpdate(id, getCurrentUser());
        investment.setInvestmentName(request.getInvestmentName());
        investment.setInvestmentType(request.getInvestmentType());
        investment.setInvestedAmount(request.getInvestedAmount());
        investment.setCurrentValue(request.getCurrentValue());
        investment.setInvestmentDate(request.getInvestmentDate());
        investment.setExpectedReturn(request.getExpectedReturn());
        investment.setRiskLevel(request.getRiskLevel());
        investment.setNotes(request.getNotes());
        investment.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(investmentRepository.save(investment));
    }

    @Override
    @Transactional
    public void deleteInvestment(Long id) {
        investmentRepository.delete(getOwnedInvestment(id, getCurrentUser()));
    }

    private Investment getOwnedInvestment(Long id, User user) {
        return investmentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Investment not found."));
    }

    private Investment getOwnedInvestmentForUpdate(Long id, User user) {
        return investmentRepository.findForUpdateByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Investment not found."));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found."
                ));
    }

    private InvestmentResponse mapToResponse(Investment investment) {
        BigDecimal gainLoss = investment.getCurrentValue().subtract(investment.getInvestedAmount());

        return InvestmentResponse.builder()
                .id(investment.getId())
                .investmentName(investment.getInvestmentName())
                .investmentType(investment.getInvestmentType())
                .investedAmount(investment.getInvestedAmount())
                .currentValue(investment.getCurrentValue())
                .gainLoss(gainLoss)
                .investmentDate(investment.getInvestmentDate())
                .expectedReturn(investment.getExpectedReturn())
                .riskLevel(investment.getRiskLevel())
                .notes(investment.getNotes())
                .createdAt(investment.getCreatedAt())
                .updatedAt(investment.getUpdatedAt())
                .build();
    }
}
