package com.financebuddy.backend.receipt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReceiptControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReceiptOcrService receiptOcrService;

    @Test
    void unauthenticatedUploadIsRejected() throws Exception {
        mockMvc.perform(multipart("/api/receipts/scan")
                        .file(pngFile()))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"));
    }

    @Test
    @WithMockUser(username = "receipt-user@example.com")
    void missingFileIsRejected() throws Exception {
        mockMvc.perform(multipart("/api/receipts/scan"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Receipt image is required."));
    }

    @Test
    @WithMockUser(username = "receipt-user@example.com")
    void emptyFileIsRejected() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "receipt.png", MediaType.IMAGE_PNG_VALUE, new byte[0]
        );

        mockMvc.perform(multipart("/api/receipts/scan").file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Receipt image is required."));
    }

    @Test
    @WithMockUser(username = "receipt-user@example.com")
    void unsupportedFileTypeIsRejected() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile(
                "file", "receipt.pdf", MediaType.APPLICATION_PDF_VALUE, "not a pdf".getBytes()
        );

        mockMvc.perform(multipart("/api/receipts/scan").file(pdf))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Only JPEG, PNG, and WEBP receipt images are supported."
                ));
    }

    @Test
    @WithMockUser(username = "receipt-user@example.com")
    void oversizedFileIsRejected() throws Exception {
        MockMultipartFile oversized = new MockMultipartFile(
                "file", "receipt.png", MediaType.IMAGE_PNG_VALUE,
                new byte[(5 * 1024 * 1024) + 1]
        );

        mockMvc.perform(multipart("/api/receipts/scan").file(oversized))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Receipt image must not exceed 5 MB."));
    }

    @Test
    @WithMockUser(username = "receipt-user@example.com")
    void invalidImageContentIsRejected() throws Exception {
        MockMultipartFile invalidImage = new MockMultipartFile(
                "file", "receipt.png", MediaType.IMAGE_PNG_VALUE, "not an image".getBytes()
        );

        mockMvc.perform(multipart("/api/receipts/scan").file(invalidImage))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The uploaded file is not a readable image."));
    }

    @Test
    @WithMockUser(username = "receipt-user@example.com")
    void validImageReturnsStructuredOcrResult() throws Exception {
        ReceiptOcrResult result = new ReceiptOcrResult(
                "Cafe Example",
                LocalDate.of(2026, 9, 11),
                new BigDecimal("245.00"),
                new BigDecimal("12.25"),
                "INR",
                "Food",
                "Cafe Example\nTotal ₹245.00",
                0.75
        );
        when(receiptOcrService.scan(any())).thenReturn(result);

        mockMvc.perform(multipart("/api/receipts/scan")
                        .file(pngFile())
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantName").value("Cafe Example"))
                .andExpect(jsonPath("$.transactionDate").value("2026-09-11"))
                .andExpect(jsonPath("$.totalAmount").value(245.00))
                .andExpect(jsonPath("$.suggestedCategory").value("Food"))
                .andExpect(jsonPath("$.confidence").value(0.75));

        verify(receiptOcrService).scan(any());
    }

    @Test
    @WithMockUser(username = "receipt-user@example.com")
    void jpegImageIsAccepted() throws Exception {
        when(receiptOcrService.scan(any())).thenReturn(new ReceiptOcrResult(
                "JPEG Store",
                LocalDate.of(2026, 9, 14),
                new BigDecimal("99.00"),
                null,
                "INR",
                "Shopping",
                "JPEG Store\nTotal 99.00",
                0.5
        ));

        mockMvc.perform(multipart("/api/receipts/scan").file(jpegFile()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantName").value("JPEG Store"))
                .andExpect(jsonPath("$.totalAmount").value(99.00));
    }

    @Test
    @WithMockUser(username = "receipt-user@example.com")
    void ocrFailureReturnsCleanError() throws Exception {
        when(receiptOcrService.scan(any()))
                .thenThrow(new ReceiptOcrException("Receipt OCR processing failed."));

        mockMvc.perform(multipart("/api/receipts/scan").file(pngFile()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Receipt OCR processing failed."));
    }

    private MockMultipartFile pngFile() {
        try {
            BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            return new MockMultipartFile(
                    "file", "receipt.png", MediaType.IMAGE_PNG_VALUE, output.toByteArray()
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create test PNG", exception);
        }
    }

    private MockMultipartFile jpegFile() {
        try {
            BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", output);
            return new MockMultipartFile(
                    "file", "receipt.jpg", MediaType.IMAGE_JPEG_VALUE, output.toByteArray()
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create test JPEG", exception);
        }
    }
}
