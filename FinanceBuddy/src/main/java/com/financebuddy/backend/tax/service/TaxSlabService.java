package com.financebuddy.backend.tax.service;

import com.financebuddy.backend.tax.dto.TaxRegime;
import com.financebuddy.backend.tax.model.TaxSlab;

import java.util.List;

public interface TaxSlabService {

    List<TaxSlab> getSlabs(TaxRegime regime, Integer age);
}
