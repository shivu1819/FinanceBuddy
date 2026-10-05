package com.financebuddy.backend.receipt;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@Slf4j
public class TesseractReceiptOcrService implements ReceiptOcrService {

    private final String configuredTessdataPath;
    private volatile String resolvedTessdataPath;

    public TesseractReceiptOcrService(
            @Value("${receipt.ocr.tessdata-path:}") String configuredTessdataPath
    ) {
        this.configuredTessdataPath = configuredTessdataPath;
    }

    @Override
    public ReceiptOcrResult scan(byte[] imageBytes) {
        BufferedImage image = readImage(imageBytes);
        String tessdataPath = resolveTessdataPath();
        ReceiptOcrResult bestResult = null;
        TesseractException lastTesseractFailure = null;

        List<BufferedImage> preparedImages = ReceiptImagePreprocessor.prepare(image);
        for (int index = 0; index < preparedImages.size(); index++) {
            BufferedImage preparedImage = preparedImages.get(index);
            try {
                Tesseract tesseract = new Tesseract();
                tesseract.setDatapath(tessdataPath);
                tesseract.setLanguage("eng");
                tesseract.setPageSegMode(index == 0 ? 6 : 11);
                String rawText = tesseract.doOCR(preparedImage);
                if (rawText == null || rawText.isBlank()) {
                    continue;
                }
                ReceiptOcrResult candidate = ReceiptOcrParser.parse(rawText);
                if (bestResult == null || candidate.overallConfidence() > bestResult.overallConfidence()) {
                    bestResult = candidate;
                }
            } catch (TesseractException exception) {
                lastTesseractFailure = exception;
            } catch (UnsatisfiedLinkError | NoClassDefFoundError exception) {
                throw new ReceiptOcrException(
                        "Receipt OCR is unavailable because the local Tesseract runtime could not be loaded.",
                        exception
                );
            } catch (ReceiptOcrException exception) {
                throw exception;
            } catch (RuntimeException exception) {
                throw new ReceiptOcrException(
                        "Receipt OCR could not process this image. Try a clearer, well-lit receipt image.",
                        exception
                );
            } catch (LinkageError exception) {
                throw new ReceiptOcrException(
                        "Receipt OCR is unavailable right now. Please try again or review the receipt manually.",
                        exception
                );
            }
        }

        if (bestResult != null) {
            return bestResult;
        }
        if (lastTesseractFailure != null) {
            throw new ReceiptOcrException(
                    "Receipt OCR could not load or process the English language data. "
                            + "Verify that eng.traineddata is available and try again.",
                    lastTesseractFailure
            );
        }
        log.info("Receipt OCR returned no readable text; returning an editable empty result.");
        return emptyResult();
    }

    private String resolveTessdataPath() {
        String cachedPath = resolvedTessdataPath;
        if (cachedPath != null) {
            return cachedPath;
        }

        String externalPath = configuredTessdataPath == null || configuredTessdataPath.isBlank()
                ? System.getenv("TESSDATA_PREFIX")
                : configuredTessdataPath;
        if (externalPath != null && !externalPath.isBlank()) {
            Path path = Path.of(externalPath).toAbsolutePath().normalize();
            if (Files.isRegularFile(path.resolve("eng.traineddata"))) {
                resolvedTessdataPath = path.toString();
                return resolvedTessdataPath;
            }
            throw new ReceiptOcrException(
                    "Receipt OCR is configured with a tessdata directory that does not contain eng.traineddata."
            );
        }

        ClassPathResource bundledLanguage = new ClassPathResource("tessdata/eng.traineddata");
        if (!bundledLanguage.exists()) {
            throw new ReceiptOcrException(
                    "Receipt OCR language data is unavailable. Add eng.traineddata to the configured tessdata directory."
            );
        }

        try {
            Path temporaryDirectory = Files.createTempDirectory("financebuddy-tessdata-");
            Path languageFile = temporaryDirectory.resolve("eng.traineddata");
            try (InputStream inputStream = bundledLanguage.getInputStream()) {
                Files.copy(inputStream, languageFile, StandardCopyOption.REPLACE_EXISTING);
            }
            resolvedTessdataPath = temporaryDirectory.toString();
            return resolvedTessdataPath;
        } catch (IOException exception) {
            throw new ReceiptOcrException("Receipt OCR language data could not be prepared.", exception);
            }
        }

    private ReceiptOcrResult emptyResult() {
        return new ReceiptOcrResult(null, null, null, null, null, null, "", 0.0);
    }

    private BufferedImage readImage(byte[] imageBytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new ReceiptValidationException("The uploaded file is not a readable image.");
            }
            return image;
        } catch (IOException exception) {
            throw new ReceiptValidationException("The uploaded image could not be read.");
        }
    }
}
