package com.financebuddy.backend.receipt;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TesseractReceiptOcrServiceTest {
    @Test
    void unavailableLocalOcrReturnsEditableEmptyResult() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);

        ReceiptOcrResult result = new TesseractReceiptOcrService("").scan(output.toByteArray());

        assertNotNull(result);
        assertNull(result.merchantName());
        assertNull(result.totalAmount());
        assertNull(result.transactionDate());
    }

    @Test
    void invalidConfiguredTessdataDirectoryReturnsClearConfigurationError() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        Path emptyDirectory = Files.createTempDirectory("financebuddy-empty-tessdata-");

        ReceiptOcrException exception = assertThrows(
                ReceiptOcrException.class,
                () -> new TesseractReceiptOcrService(emptyDirectory.toString()).scan(output.toByteArray())
        );

        assertTrue(exception.getMessage().contains("eng.traineddata"));
    }

    @Test
    void bundledEnglishModelExtractsTextFromReceiptLikeImage() throws Exception {
        BufferedImage image = new BufferedImage(1200, 1800, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 72));
        graphics.drawString("FRESH CAFE", 120, 250);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 58));
        graphics.drawString("Date: 14/09/2026", 120, 450);
        graphics.drawString("Coffee x2        100.00", 120, 650);
        graphics.drawString("Grand Total      118.00", 120, 850);
        graphics.dispose();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", output);
        ReceiptOcrResult result = new TesseractReceiptOcrService("").scan(output.toByteArray());

        assertTrue(result.rawText().toLowerCase().contains("fresh"));
        assertTrue(result.rawText().toLowerCase().contains("total"));
    }
}
