package com.financebuddy.backend.investment;

import java.util.List;

public interface InvestmentService {

    InvestmentResponse createInvestment(InvestmentRequest request);

    List<InvestmentResponse> getAllInvestments();

    InvestmentResponse getInvestmentById(Long id);

    InvestmentResponse updateInvestment(Long id, InvestmentRequest request);

    void deleteInvestment(Long id);
}
