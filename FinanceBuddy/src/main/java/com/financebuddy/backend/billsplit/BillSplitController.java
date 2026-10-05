package com.financebuddy.backend.billsplit;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bill-splits")
@RequiredArgsConstructor
public class BillSplitController {

    private final BillSplitService billSplitService;

    @PostMapping
    public ResponseEntity<BillSplitResponse> createBillSplit(@Valid @RequestBody BillSplitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billSplitService.createBillSplit(request));
    }

    @GetMapping
    public ResponseEntity<List<BillSplitResponse>> getAllBillSplits() {
        return ResponseEntity.ok(billSplitService.getAllBillSplits());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillSplitResponse> getBillSplitById(@PathVariable Long id) {
        return ResponseEntity.ok(billSplitService.getBillSplitById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BillSplitResponse> updateBillSplit(
            @PathVariable Long id,
            @Valid @RequestBody BillSplitRequest request
    ) {
        return ResponseEntity.ok(billSplitService.updateBillSplit(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteBillSplit(@PathVariable Long id) {
        billSplitService.deleteBillSplit(id);
        return ResponseEntity.ok(Map.of("message", "Bill split deleted successfully."));
    }
}
