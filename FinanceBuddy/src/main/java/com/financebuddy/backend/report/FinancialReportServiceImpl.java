package com.financebuddy.backend.report;

import com.financebuddy.backend.budget.*;
import com.financebuddy.backend.entity.*;
import com.financebuddy.backend.goal.Goal;
import com.financebuddy.backend.goal.GoalRepository;
import com.financebuddy.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.*;
import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor
public class FinancialReportServiceImpl implements FinancialReportService {
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    @Override @Transactional(readOnly = true)
    public FinancialReportResponse getMonthly(YearMonth month) {
        User user=current(); List<Transaction> transactions=transactionRepository.findByUserIdAndTransactionDateBetween(user.getId(),month.atDay(1),month.atEndOfMonth());
        BigDecimal income=sum(transactions,"INCOME"), expenses=sum(transactions,"EXPENSE"); Map<String,BigDecimal> categories=new HashMap<>();
        transactions.stream().filter(t -> "EXPENSE".equals(t.getTransactionType())).forEach(t -> categories.merge(t.getCategory().getName(),t.getAmount(),BigDecimal::add));
        BigDecimal limit=BigDecimal.ZERO, spent=BigDecimal.ZERO; for(Budget b:budgetRepository.findAllByUserAndBudgetMonth(user,month)){limit=limit.add(b.getMonthlyLimit()); spent=spent.add(transactions.stream().filter(t -> "EXPENSE".equals(t.getTransactionType()) && (b.getCategory()==null || b.getCategory().getId().equals(t.getCategory().getId()))).map(Transaction::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add));}
        List<FinancialReportResponse.GoalReport> goals=goalRepository.findByUserOrderByCreatedAtDesc(user).stream().map(g -> new FinancialReportResponse.GoalReport(g.getTitle(),g.getTargetAmount(),g.getSavedAmount(),progress(g))).toList();
        return FinancialReportResponse.builder().month(month).income(income).expenses(expenses).savings(income.subtract(expenses)).budgetLimit(limit).budgetSpent(spent).categories(categories.entrySet().stream().sorted(Map.Entry.<String,BigDecimal>comparingByValue().reversed()).map(e -> new FinancialReportResponse.CategoryReport(e.getKey(),e.getValue())).toList()).goals(goals).build();
    }
    @Override @Transactional(readOnly = true) public String exportMonthly(YearMonth month){FinancialReportResponse r=getMonthly(month); StringBuilder csv=new StringBuilder("section,name,amount\n").append("summary,income,").append(r.getIncome()).append("\nsummary,expenses,").append(r.getExpenses()).append("\nsummary,savings,").append(r.getSavings()).append("\n"); r.getCategories().forEach(c -> csv.append("category,\"").append(c.category().replace("\"","\"\"")).append("\",").append(c.amount()).append("\n")); return csv.toString();}
    private BigDecimal sum(List<Transaction> list,String type){return list.stream().filter(t -> type.equals(t.getTransactionType())).map(Transaction::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);}
    private int progress(Goal g){return g.getTargetAmount().signum()==0?0:Math.min(100,g.getSavedAmount().multiply(BigDecimal.valueOf(100)).divide(g.getTargetAmount(),0,RoundingMode.HALF_UP).intValue());}
    private User current(){var a=SecurityContextHolder.getContext().getAuthentication();if(a==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Authentication is required.");return userRepository.findByEmail(a.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Authenticated user not found."));}
}
