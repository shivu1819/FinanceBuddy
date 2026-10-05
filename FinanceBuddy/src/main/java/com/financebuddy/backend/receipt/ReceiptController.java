package com.financebuddy.backend.receipt;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;

    private final ReceiptOcrService receiptOcrService;

    @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReceiptOcrResult> scanReceipt(
            @RequestPart("file") MultipartFile file
    ) {
        validateFile(file);
        return ResponseEntity.status(HttpStatus.OK).body(receiptOcrService.scan(toBytes(file)));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ReceiptValidationException("Receipt image is required.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ReceiptValidationException("Receipt image must not exceed 5 MB.");
        }

        String contentType = file.getContentType();
        if (!MediaType.IMAGE_JPEG_VALUE.equalsIgnoreCase(contentType)
                && !"image/jpg".equalsIgnoreCase(contentType)
                && !MediaType.IMAGE_PNG_VALUE.equalsIgnoreCase(contentType)
                && !"image/webp".equalsIgnoreCase(contentType)) {
            throw new ReceiptValidationException("Only JPEG, PNG, and WEBP receipt images are supported.");
        }
    }

    private byte[] toBytes(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new ReceiptValidationException("The uploaded file is not a readable image.");
            }
            return bytes;
        } catch (ReceiptValidationException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new ReceiptValidationException("The uploaded receipt could not be read.");
        } catch (Exception exception) {
            throw new ReceiptValidationException("The uploaded receipt could not be read.");
        }
    }
}
