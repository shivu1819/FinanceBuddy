package com.financebuddy.backend.billsplit;

import java.util.List;

public interface BillSplitService {

    BillSplitResponse createBillSplit(BillSplitRequest request);

    List<BillSplitResponse> getAllBillSplits();

    BillSplitResponse getBillSplitById(Long id);

    BillSplitResponse updateBillSplit(Long id, BillSplitRequest request);

    void deleteBillSplit(Long id);
}
