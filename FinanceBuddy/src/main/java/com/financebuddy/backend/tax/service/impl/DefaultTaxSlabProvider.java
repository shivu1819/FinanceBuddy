package com.financebuddy.backend.tax.service.impl;

import com.financebuddy.backend.tax.dto.TaxRegime;
import com.financebuddy.backend.tax.model.TaxSlab;
import com.financebuddy.backend.tax.service.TaxSlabProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DefaultTaxSlabProvider implements TaxSlabProvider {

    private static final BigDecimal OLD_BASIC_EXEMPTION = new BigDecimal("250000.00");
    private static final BigDecimal OLD_SENIOR_EXEMPTION = new BigDecimal("300000.00");
    private static final BigDecimal OLD_SUPER_SENIOR_EXEMPTION = new BigDecimal("500000.00");

    /**
     * Returns isolated slab data. Future Finance Act changes should update this provider data only.
     *
     * @param regime selected tax regime
     * @param age taxpayer age
     * @return ordered slab list
     */
    @Override
    public List<TaxSlab> getSlabs(TaxRegime regime, Integer age) {
        if (regime == TaxRegime.NEW) {
            return newRegimeSlabs();
        }

        return oldRegimeSlabs(age);
    }

    private List<TaxSlab> newRegimeSlabs() {
        return List.of(
                slab(TaxRegime.NEW, "0.00", "400000.00", "0.00"),
                slab(TaxRegime.NEW, "400000.00", "800000.00", "0.05"),
                slab(TaxRegime.NEW, "800000.00", "1200000.00", "0.10"),
                slab(TaxRegime.NEW, "1200000.00", "1600000.00", "0.15"),
                slab(TaxRegime.NEW, "1600000.00", "2000000.00", "0.20"),
                slab(TaxRegime.NEW, "2000000.00", "2400000.00", "0.25"),
                slab(TaxRegime.NEW, "2400000.00", null, "0.30")
        );
    }

    private List<TaxSlab> oldRegimeSlabs(Integer age) {
        BigDecimal exemptionLimit = getOldRegimeExemptionLimit(age);

        return List.of(
                new TaxSlab(TaxRegime.OLD, BigDecimal.ZERO, exemptionLimit, BigDecimal.ZERO),
                new TaxSlab(TaxRegime.OLD, exemptionLimit, new BigDecimal("500000.00"), new BigDecimal("0.05")),
                slab(TaxRegime.OLD, "500000.00", "1000000.00", "0.20"),
                slab(TaxRegime.OLD, "1000000.00", null, "0.30")
        );
    }

    private BigDecimal getOldRegimeExemptionLimit(Integer age) {
        if (age == null || age < 60) {
            return OLD_BASIC_EXEMPTION;
        }

        if (age < 80) {
            return OLD_SENIOR_EXEMPTION;
        }

        return OLD_SUPER_SENIOR_EXEMPTION;
    }

    private TaxSlab slab(TaxRegime regime, String lowerLimit, String upperLimit, String rate) {
        return new TaxSlab(
                regime,
                new BigDecimal(lowerLimit),
                upperLimit == null ? null : new BigDecimal(upperLimit),
                new BigDecimal(rate)
        );
    }
}
