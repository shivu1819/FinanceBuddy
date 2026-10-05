package com.financebuddy.backend.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-insights")
@RequiredArgsConstructor
public class AIInsightsController {
    private final AIInsightsService insightsService;

    @GetMapping
    public ResponseEntity<AIInsightResponse> getInsights() {
        return ResponseEntity.ok(insightsService.getInsights());
    }
}
