package com.financebuddy.backend.receipt;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.util.List;

final class ReceiptImagePreprocessor {

    private static final int TARGET_MIN_DIMENSION = 1600;
    private static final int MAX_DIMENSION = 3000;

    private ReceiptImagePreprocessor() {
    }

    static List<BufferedImage> prepare(BufferedImage source) {
        BufferedImage oriented = orientWideReceipt(source);
        BufferedImage resized = resize(oriented);
        BufferedImage enhanced = sharpen(enhanceContrast(toGrayscale(resized)));
        return List.of(enhanced, adaptiveThreshold(enhanced));
    }

    private static BufferedImage orientWideReceipt(BufferedImage source) {
        if (source.getWidth() <= source.getHeight() * 1.4) {
            return source;
        }

        int width = source.getHeight();
        int height = source.getWidth();
        BufferedImage rotated = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rotated.createGraphics();
        graphics.translate(width, 0);
        graphics.rotate(Math.PI / 2);
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return rotated;
    }

    private static BufferedImage resize(BufferedImage source) {
        double scale = Math.min(
                (double) MAX_DIMENSION / Math.max(source.getWidth(), source.getHeight()),
                Math.max(1.0, (double) TARGET_MIN_DIMENSION / Math.min(source.getWidth(), source.getHeight()))
        );
        if (scale <= 1.0) {
            return source;
        }

        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return resized;
    }

    private static BufferedImage toGrayscale(BufferedImage source) {
        BufferedImage grayscale = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = grayscale.createGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return grayscale;
    }

    private static BufferedImage enhanceContrast(BufferedImage source) {
        RescaleOp contrast = new RescaleOp(1.35f, -35f, null);
        return contrast.filter(source, null);
    }

    private static BufferedImage sharpen(BufferedImage source) {
        float[] kernel = {
                0f, -0.8f, 0f,
                -0.8f, 4.2f, -0.8f,
                0f, -0.8f, 0f
        };
        return new ConvolveOp(new Kernel(3, 3, kernel), ConvolveOp.EDGE_NO_OP, null).filter(source, null);
    }

    private static BufferedImage adaptiveThreshold(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int radius = 8;
        long[][] integral = new long[height + 1][width + 1];
        for (int y = 1; y <= height; y++) {
            long rowSum = 0;
            for (int x = 1; x <= width; x++) {
                rowSum += source.getRaster().getSample(x - 1, y - 1, 0);
                integral[y][x] = integral[y - 1][x] + rowSum;
            }
        }
        BufferedImage thresholded = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int left = Math.max(0, x - radius);
                int top = Math.max(0, y - radius);
                int right = Math.min(width - 1, x + radius);
                int bottom = Math.min(height - 1, y + radius);
                long sum = integral[bottom + 1][right + 1] - integral[top][right + 1]
                        - integral[bottom + 1][left] + integral[top][left];
                int count = (right - left + 1) * (bottom - top + 1);
                int localThreshold = (int) (sum / count) - 10;
                int value = source.getRaster().getSample(x, y, 0) > localThreshold ? 255 : 0;
                thresholded.getRaster().setSample(x, y, 0, value);
            }
        }
        return thresholded;
    }
}
