package com.financebuddy.backend.report;

import java.time.YearMonth;

public interface FinancialReportService {
    FinancialReportResponse getMonthly(YearMonth month);
    String exportMonthly(YearMonth month);
}
