package com.financebuddy.backend.recurring;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api/recurring-transactions") @RequiredArgsConstructor
public class RecurringTransactionController {
    private final RecurringTransactionService service;
    @GetMapping public List<RecurringTransactionResponse> getAll() { return service.getAll(); }
    @PostMapping public ResponseEntity<RecurringTransactionResponse> create(@Valid @RequestBody RecurringTransactionRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request)); }
    @PutMapping("/{id}") public RecurringTransactionResponse update(@PathVariable Long id, @Valid @RequestBody RecurringTransactionRequest request) { return service.update(id, request); }
    @PatchMapping("/{id}/active") public RecurringTransactionResponse setActive(@PathVariable Long id, @RequestParam boolean active) { return service.setActive(id, active); }
    @DeleteMapping("/{id}") public Map<String,String> delete(@PathVariable Long id) { service.delete(id); return Map.of("message", "Recurring transaction deleted successfully."); }
}
