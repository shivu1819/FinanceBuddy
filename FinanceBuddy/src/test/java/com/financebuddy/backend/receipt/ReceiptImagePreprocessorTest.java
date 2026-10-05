package com.financebuddy.backend.receipt;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceiptImagePreprocessorTest {

    @Test
    void createsEnhancedAndThresholdedUpscaledVariants() {
        BufferedImage source = new BufferedImage(600, 300, BufferedImage.TYPE_INT_RGB);

        List<BufferedImage> variants = ReceiptImagePreprocessor.prepare(source);

        assertEquals(2, variants.size());
        assertTrue(variants.get(0).getHeight() >= 1600);
        assertEquals(variants.get(0).getWidth(), variants.get(1).getWidth());
        assertEquals(variants.get(0).getHeight(), variants.get(1).getHeight());
    }
}
