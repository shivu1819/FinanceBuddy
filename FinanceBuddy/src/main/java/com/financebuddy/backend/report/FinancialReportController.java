package com.financebuddy.backend.report;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.YearMonth;

@RestController @RequestMapping("/api/reports") @RequiredArgsConstructor
public class FinancialReportController {
    private final FinancialReportService service;
    @GetMapping("/monthly") public FinancialReportResponse monthly(@RequestParam(required=false) YearMonth month){return service.getMonthly(month==null?YearMonth.now():month);}
    @GetMapping(value="/monthly/export", produces="text/csv") public ResponseEntity<String> export(@RequestParam(required=false) YearMonth month){return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=finance-report.csv").body(service.exportMonthly(month==null?YearMonth.now():month));}
}
