package com.financebuddy.backend.tax.service;

import com.financebuddy.backend.tax.dto.TaxRegime;
import com.financebuddy.backend.tax.model.TaxSlab;

import java.util.List;

public interface TaxSlabProvider {

    /**
     * Returns configured slabs for the selected regime and taxpayer age.
     *
     * @param regime selected tax regime
     * @param age taxpayer age, used only where regime rules require age-based slabs
     * @return ordered slab configuration
     */
    List<TaxSlab> getSlabs(TaxRegime regime, Integer age);
}
