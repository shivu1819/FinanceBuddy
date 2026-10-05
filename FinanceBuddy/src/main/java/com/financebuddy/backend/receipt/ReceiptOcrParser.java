package com.financebuddy.backend.receipt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ReceiptOcrParser {

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(?i)(?<!\\d)(" +
                    "\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4}" +
                    "|" +
                    "\\d{4}[./-]\\d{1,2}[./-]\\d{1,2}" +
                    "|" +
                    "\\d{1,2}\\s+(?:jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec)[a-z]*,?\\s+\\d{4}" +
                    "|" +
                    "(?:jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec)[a-z]*\\s+\\d{1,2},?\\s+\\d{4}" +
                    ")(?!\\d)"
    );

    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(?i)(?<!\\d)" +
                    "(\\d{1,2}[:.]\\d{2}(?::\\d{2})?\\s*(?:AM|PM)?)" +
                    "\\b"
    );

    private static final Pattern NUMERIC_TOKEN_PATTERN = Pattern.compile(
            "(?<!\\d)(" +
                    "(?:[0-9OoIiLlSsBbGg]{1,3}(?:,[0-9OoIiLlSsBbGg]{3})+|" +
                    "[0-9OoIiLlSsBbGg]+)" +
                    "(?:\\.[0-9OoIiLlSsBbGg]{1,2})?" +
                    ")(?![A-Za-z0-9])"
    );

    private static final Pattern GSTIN_PATTERN =
            Pattern.compile("(?i)\\b[0-9A-Z]{15}\\b");

    private static final Set<String> NOISE_WORDS = Set.of(
            "receipt",
            "invoice",
            "tax invoice",
            "description",
            "item",
            "qty",
            "quantity",
            "price",
            "amount",
            "dine-in",
            "table",
            "guest",
            "ref no",
            "reference",
            "bill no",
            "bill number",
            "invoice no",
            "invoice number",
            "address",
            "pincode",
            "thank you",
            "gstin",
            "gst",
            "cgst",
            "sgst",
            "igst",
            "vat",
            "tin",
            "tel",
            "telephone",
            "phone",
            "mobile",
            "date",
            "time",
            "payment",
            "cash",
            "card",
            "upi",
            "subtotal",
            "sub total",
            "total",
            "tax"
    );

    private ReceiptOcrParser() {
    }

    static ReceiptOcrResult parse(String rawText) {

        String normalizedText = normalize(rawText);

        List<String> lines = normalizedText.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();

        String merchant = merchantName(lines);

        LocalDate date = transactionDate(normalizedText);

        LocalTime time = transactionTime(normalizedText);

        BigDecimal total = findBestTotal(normalizedText);

        BigDecimal subtotal = findBestSubtotal(normalizedText);

        TaxBreakdown taxBreakdown = taxBreakdown(normalizedText);

        BigDecimal tax = taxBreakdown.total();

        String currency = currency(normalizedText);

        String paymentMethod = paymentMethod(normalizedText);

        List<ReceiptLineItem> items = lineItems(lines);

        String category = suggestedCategory(
                normalizedText,
                merchant,
                items
        );

        Validation validation = validateFinancials(
                subtotal,
                tax,
                total,
                items
        );

        double merchantConfidence =
                merchant == null ? 0.0 : 0.90;

        double dateConfidence =
                date == null ? 0.0 : 0.92;

        double totalConfidence =
                total == null
                        ? 0.0
                        : totalConfidence(normalizedText);

        double taxConfidence =
                tax == null ? 0.0 : 0.86;

        double itemConfidence =
                items.isEmpty()
                        ? 0.0
                        : items.stream()
                        .mapToDouble(ReceiptLineItem::confidence)
                        .average()
                        .orElse(0.0);

        double subtotalConfidence =
                subtotal == null ? 0.0 : 0.90;

        double categoryConfidence =
                category == null
                        ? 0.0
                        : category.equals("Other")
                          ? 0.45
                          : 0.88;

        double overallConfidence = overallConfidence(
                merchantConfidence,
                dateConfidence,
                totalConfidence,
                taxConfidence,
                itemConfidence,
                validation.consistent
        );

        return new ReceiptOcrResult(
                merchant,
                date,
                total,
                tax,
                currency,
                category,
                normalizedText,
                overallConfidence,
                time,
                subtotal,
                paymentMethod,
                items,
                merchantConfidence,
                dateConfidence,
                totalConfidence,
                taxConfidence,
                itemConfidence,
                overallConfidence,
                validation.consistent,
                validation.message,
                taxBreakdown.cgst(),
                taxBreakdown.sgst(),
                taxBreakdown.igst(),
                subtotalConfidence,
                categoryConfidence
        );
    }

    private static String normalize(String rawText) {

        if (rawText == null) {
            return "";
        }

        return rawText
                .replace('\u00A0', ' ')
                .replace("â‚¹", "₹")
                .replace("Â£", "£")
                .replace("â‚¬", "€")
                .replace("â€“", "-")
                .replace("â€”", "-")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\r\\n?", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private static String merchantName(List<String> lines) {

        for (int index = 0; index < Math.min(lines.size(), 8); index++) {

            String line = lines.get(index)
                    .replaceAll("^[*#|:.\\- ]+", "")
                    .trim();

            if (line.length() < 3 ||
                    line.length() > 100) {
                continue;
            }

            String lower =
                    line.toLowerCase(Locale.ROOT);

            if (DATE_PATTERN.matcher(line).find()) {
                continue;
            }

            if (line.matches("[\\d .:/+\\-]+")) {
                continue;
            }

            if (lower.matches(".*\\b(?:invoice|receipt|tax invoice)\\b.*")) {
                continue;
            }

            if (lower.matches(
                    ".*\\b(?:date|time|payment|total|amount|tax|subtotal)\\b.*"
            )) {
                continue;
            }

            if (lower.matches(
                    ".*\\b(?:phone|mobile|tel|telephone|email)\\b.*"
            )) {
                continue;
            }

            if (lower.matches(".*\\d{8,}.*")) {
                continue;
            }

            return line;
        }

        return null;
    }

    private static LocalDate transactionDate(String text) {

        Matcher matcher =
                DATE_PATTERN.matcher(text);

        while (matcher.find()) {

            LocalDate date =
                    parseDate(matcher.group(1));

            if (date != null) {
                return date;
            }
        }

        return null;
    }

    private static LocalDate parseDate(String value) {

        if (value == null) {
            return null;
        }

        String normalized = value
                .trim()
                .replace(",", "")
                .replace(".", "-")
                .replace("/", "-");

        int currentYear = Year.now().getValue();

        List<DateTimeFormatter> formatters =
                new ArrayList<>();

        if (normalized.matches(
                "\\d{4}-\\d{1,2}-\\d{1,2}"
        )) {

            formatters.add(
                    DateTimeFormatter
                            .ofPattern("uuuu-M-d")
                            .withResolverStyle(
                                    ResolverStyle.STRICT
                            )
            );

        } else if (normalized.matches(
                "\\d{1,2}-\\d{1,2}-\\d{4}"
        )) {

            formatters.add(
                    DateTimeFormatter
                            .ofPattern("d-M-uuuu")
                            .withResolverStyle(
                                    ResolverStyle.STRICT
                            )
            );

        } else if (normalized.matches(
                "\\d{1,2}-\\d{1,2}-\\d{2}"
        )) {

            formatters.add(
                    DateTimeFormatter
                            .ofPattern("d-M-yy")
                            .withResolverStyle(
                                    ResolverStyle.SMART
                            )
            );

        } else {

            String monthText =
                    normalized
                            .replaceAll(
                                    "(?i)^sept ",
                                    "Sep "
                            )
                            .replaceAll(
                                    "(?i)^september ",
                                    "Sep "
                            );

            formatters.add(
                    DateTimeFormatter
                            .ofPattern(
                                    "d MMM uuuu",
                                    Locale.ENGLISH
                            )
                            .withResolverStyle(
                                    ResolverStyle.SMART
                            )
            );

            formatters.add(
                    DateTimeFormatter
                            .ofPattern(
                                    "MMM d uuuu",
                                    Locale.ENGLISH
                            )
                            .withResolverStyle(
                                    ResolverStyle.SMART
                            )
            );

            normalized = monthText;
        }

        for (DateTimeFormatter formatter : formatters) {

            try {

                LocalDate date =
                        LocalDate.parse(
                                normalized,
                                formatter
                        );

                if (date.getYear() >= 2000 &&
                        date.getYear() <= currentYear + 1) {

                    return date;
                }

            } catch (DateTimeParseException ignored) {
                // Try next format.
            }
        }

        return null;
    }

    private static LocalTime transactionTime(
            String text
    ) {

        Matcher matcher =
                TIME_PATTERN.matcher(text);

        if (!matcher.find()) {
            return null;
        }

        String value =
                matcher.group(1)
                        .toUpperCase(Locale.ROOT)
                        .replace(".", ":")
                        .replaceAll("\\s+", "");

        if (value.endsWith("AM") ||
                value.endsWith("PM")) {

            try {

                if (value.matches(
                        "\\d{1,2}:\\d{2}AM|\\d{1,2}:\\d{2}PM"
                )) {

                    return LocalTime.parse(
                            value,
                            DateTimeFormatter.ofPattern(
                                    "h:mma",
                                    Locale.ENGLISH
                            )
                    );
                }

                return LocalTime.parse(
                        value,
                        DateTimeFormatter.ofPattern(
                                "h:mm:ssa",
                                Locale.ENGLISH
                        )
                );

            } catch (DateTimeParseException ignored) {
                return null;
            }
        }

        try {

            if (value.matches("\\d{1,2}:\\d{2}")) {

                return LocalTime.parse(
                        value,
                        DateTimeFormatter.ofPattern("H:mm")
                );
            }

            return LocalTime.parse(
                    value,
                    DateTimeFormatter.ofPattern("H:mm:ss")
            );

        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static BigDecimal findBestTotal(
            String text
    ) {

        String[] labels = {
                "grand total",
                "amount payable",
                "amount due",
                "net total",
                "total amount",
                "total"
        };

        for (String label : labels) {

            Pattern pattern =
                    Pattern.compile(
                            "(?i)\\b" +
                                    Pattern.quote(label) +
                                    "\\b\\s*[:\\-]?\\s*" +
                                    "(?:AED|DHS|INR|USD|EUR|GBP|₹|\\$|€|£)?\\s*" +
                                    "([0-9OoIiLlSsBbGg]{1,3}" +
                                    "(?:,[0-9OoIiLlSsBbGg]{3})*" +
                                    "(?:\\.[0-9OoIiLlSsBbGg]{1,2})?" +
                                    "|[0-9OoIiLlSsBbGg]+" +
                                    "(?:\\.[0-9OoIiLlSsBbGg]{1,2})?)"
                    );

            Matcher matcher =
                    pattern.matcher(text);

            BigDecimal best = null;

            while (matcher.find()) {

                BigDecimal amount =
                        parseAmount(matcher.group(1));

                if (isPlausibleAmount(amount)) {
                    best = amount;
                }
            }

            if (best != null) {
                return best;
            }
        }

        return null;
    }

    private static BigDecimal findBestSubtotal(
            String text
    ) {

        String[] labels = {
                "subtotal",
                "sub total",
                "net amount",
                "ticket total",
                "item total"
        };

        for (String label : labels) {

            Pattern pattern =
                    Pattern.compile(
                            "(?i)\\b" +
                                    Pattern.quote(label) +
                                    "\\b\\s*[:\\-]?\\s*" +
                                    "(?:AED|DHS|INR|USD|EUR|GBP|₹|\\$|€|£)?\\s*" +
                                    "([0-9OoIiLlSsBbGg]{1,3}" +
                                    "(?:,[0-9OoIiLlSsBbGg]{3})*" +
                                    "(?:\\.[0-9OoIiLlSsBbGg]{1,2})?" +
                                    "|[0-9OoIiLlSsBbGg]+" +
                                    "(?:\\.[0-9OoIiLlSsBbGg]{1,2})?)"
                    );

            Matcher matcher =
                    pattern.matcher(text);

            BigDecimal best = null;

            while (matcher.find()) {

                BigDecimal amount =
                        parseAmount(matcher.group(1));

                if (isPlausibleAmount(amount)) {
                    best = amount;
                }
            }

            if (best != null) {
                return best;
            }
        }

        return null;
    }

    private static TaxBreakdown taxBreakdown(
            String text
    ) {

        BigDecimal cgst = null;
        BigDecimal sgst = null;
        BigDecimal igst = null;
        BigDecimal otherTax = null;

        for (String line : text.lines().toList()) {

            String lower =
                    line.toLowerCase(Locale.ROOT);

            boolean taxLine =
                    lower.contains("cgst") ||
                            lower.contains("sgst") ||
                            lower.contains("igst") ||
                            lower.contains("vat") ||
                            lower.matches(
                                    ".*\\btax\\b.*"
                            );

            if (!taxLine) {
                continue;
            }

            BigDecimal amount =
                    extractTaxAmount(line);

            if (amount == null) {
                continue;
            }

            if (lower.contains("cgst")) {

                cgst = add(cgst, amount);

            } else if (lower.contains("sgst")) {

                sgst = add(sgst, amount);

            } else if (lower.contains("igst")) {

                igst = add(igst, amount);

            } else {

                otherTax = add(
                        otherTax,
                        amount
                );
            }
        }

        BigDecimal total =
                add(
                        add(
                                add(cgst, sgst),
                                igst
                        ),
                        otherTax
                );

        return new TaxBreakdown(
                total,
                cgst,
                sgst,
                igst
        );
    }

    private static BigDecimal extractTaxAmount(
            String line
    ) {

        /*
         * Prefer an amount after the tax percentage.
         *
         * Example:
         * VAT 5% AED 1,500.00
         *
         * We want 1500, NOT 5.
         */

        Pattern afterPercent =
                Pattern.compile(
                        "(?i)\\b(?:gst|cgst|sgst|igst|vat|tax)" +
                                "\\b.*?\\d+(?:\\.\\d+)?\\s*%" +
                                "\\s*(?:AED|DHS|INR|USD|EUR|GBP|₹|\\$|€|£)?\\s*" +
                                "([0-9OoIiLlSsBbGg]{1,3}" +
                                "(?:,[0-9OoIiLlSsBbGg]{3})*" +
                                "(?:\\.[0-9OoIiLlSsBbGg]{1,2})?" +
                                "|[0-9OoIiLlSsBbGg]+" +
                                "(?:\\.[0-9OoIiLlSsBbGg]{1,2})?)"
                );

        Matcher percentMatcher =
                afterPercent.matcher(line);

        if (percentMatcher.find()) {

            BigDecimal amount =
                    parseAmount(
                            percentMatcher.group(1)
                    );

            if (isPlausibleAmount(amount)) {
                return amount;
            }
        }

        /*
         * Otherwise take the last monetary-looking value
         * on the tax line.
         */

        Matcher matcher =
                NUMERIC_TOKEN_PATTERN.matcher(line);

        BigDecimal last = null;

        while (matcher.find()) {

            String after =
                    line.substring(
                            matcher.end()
                    ).stripLeading();

            if (after.startsWith("%")) {
                continue;
            }

            BigDecimal amount =
                    parseAmount(
                            matcher.group(1)
                    );

            if (isPlausibleAmount(amount)) {
                last = amount;
            }
        }

        return last;
    }

    private static BigDecimal parseAmount(
            String value
    ) {

        if (value == null ||
                value.isBlank()) {
            return null;
        }

        try {

            String normalized =
                    value
                            .replace('O', '0')
                            .replace('o', '0')
                            .replace('I', '1')
                            .replace('i', '1')
                            .replace('L', '1')
                            .replace('l', '1')
                            .replace('S', '5')
                            .replace('s', '5')
                            .replace('B', '8')
                            .replace('b', '8')
                            .replace('G', '6')
                            .replace('g', '6')
                            .replace(",", "")
                            .replace(" ", "");

            return new BigDecimal(normalized)
                    .setScale(
                            2,
                            RoundingMode.HALF_UP
                    );

        } catch (NumberFormatException exception) {

            return null;
        }
    }

    private static BigDecimal add(
            BigDecimal left,
            BigDecimal right
    ) {

        if (left == null) {
            return right;
        }

        if (right == null) {
            return left;
        }

        return left
                .add(right)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private static String currency(
            String text
    ) {

        String normalized =
                text.toLowerCase(Locale.ROOT);

        if (normalized.contains("aed") ||
                normalized.contains("dhs") ||
                normalized.contains("dirham") ||
                normalized.contains("د.إ")) {

            return "AED";
        }

        if (normalized.contains("₹") ||
                normalized.contains("inr") ||
                normalized.contains("rs.") ||
                normalized.contains("rs ")) {

            return "INR";
        }

        if (normalized.contains("$") ||
                normalized.contains("usd")) {

            return "USD";
        }

        if (normalized.contains("€") ||
                normalized.contains("eur")) {

            return "EUR";
        }

        if (normalized.contains("£") ||
                normalized.contains("gbp")) {

            return "GBP";
        }

        return null;
    }

    private static String paymentMethod(
            String text
    ) {

        String normalized =
                text.toLowerCase(Locale.ROOT);

        if (normalized.matches(
                "(?s).*\\b(?:upi|gpay|google pay|phonepe|paytm|bhim)\\b.*"
        )) {
            return "UPI";
        }

        if (normalized.matches(
                "(?s).*\\b(?:visa|mastercard|credit card|debit card|card)\\b.*"
        )) {
            return "CARD";
        }

        if (normalized.matches(
                "(?s).*\\b(?:cash|tender)\\b.*"
        )) {
            return "CASH";
        }

        return null;
    }

    private static List<ReceiptLineItem> lineItems(
            List<String> lines
    ) {

        List<ReceiptLineItem> items =
                new ArrayList<>();

        Set<String> seen =
                new HashSet<>();

        boolean tableStarted = false;

        for (String line : lines) {

            String lower =
                    line.toLowerCase(Locale.ROOT);

            if (lower.matches(
                    ".*\\b(?:name|item|description)\\b.*" +
                            "\\b(?:qty|quantity|rate|amt|amount|price)\\b.*"
            )) {

                tableStarted = true;
                continue;
            }

            if (!tableStarted) {
                continue;
            }

            if (line.length() < 3) {
                continue;
            }

            if (isMetadataLine(line)) {
                continue;
            }

            if (lower.matches(
                    ".*\\b(?:subtotal|sub total|ticket total|" +
                            "grand total|total|tax|payment|cash|card|upi|paid)\\b.*"
            )) {
                continue;
            }

            List<NumberToken> numbers =
                    numericTokens(line);

            if (numbers.size() < 2) {
                continue;
            }

            NumberToken amountToken =
                    numbers.get(
                            numbers.size() - 1
                    );

            NumberToken rateToken =
                    numbers.size() >= 3
                            ? numbers.get(
                            numbers.size() - 2
                    )
                            : null;

            NumberToken quantityToken =
                    numbers.size() >= 3
                            ? numbers.get(
                            numbers.size() - 3
                    )
                            : numbers.get(
                            numbers.size() - 2
                    );

            BigDecimal quantity =
                    parseAmount(
                            quantityToken.value()
                    );

            BigDecimal lineTotal =
                    parseAmount(
                            amountToken.value()
                    );

            if (!isPlausibleQuantity(quantity) ||
                    !isPlausibleAmount(lineTotal)) {
                continue;
            }

            BigDecimal rate;

            if (rateToken == null) {

                rate =
                        lineTotal.divide(
                                quantity,
                                2,
                                RoundingMode.HALF_UP
                        );

            } else {

                rate =
                        parseAmount(
                                rateToken.value()
                        );
            }

            if (!isPlausibleAmount(rate)) {
                continue;
            }

            if (rateToken != null) {

                BigDecimal calculated =
                        rate.multiply(quantity);

                BigDecimal difference =
                        calculated
                                .subtract(lineTotal)
                                .abs();

                if (difference.compareTo(
                        new BigDecimal("0.10")
                ) > 0) {
                    continue;
                }
            }

            int nameEnd =
                    quantityToken.start();

            if (nameEnd <= 0 ||
                    nameEnd > line.length()) {
                continue;
            }

            String name =
                    line.substring(
                                    0,
                                    nameEnd
                            )
                            .replaceAll(
                                    "(?i)\\b(?:qty|quantity|rate|amt|amount|price|x)\\b",
                                    ""
                            )
                            .replaceAll(
                                    "[|:.-]+$",
                                    ""
                            )
                            .trim();

            if (name.length() < 2 ||
                    !name.matches(".*[A-Za-z].*") ||
                    name.matches(".*\\d{5,}.*")) {
                continue;
            }

            String key =
                    name.toLowerCase(Locale.ROOT);

            if (!seen.add(key)) {
                continue;
            }

            items.add(
                    new ReceiptLineItem(
                            name,
                            quantity,
                            rate,
                            lineTotal,
                            rateToken == null
                                    ? 0.70
                                    : 0.85
                    )
            );
        }

        return items;
    }

    private static boolean isMetadataLine(
            String line
    ) {

        String normalized =
                line.toLowerCase(Locale.ROOT)
                        .replaceAll(
                                "[^a-z0-9]+",
                                " "
                        )
                        .trim();

        String compact =
                line.replaceAll(
                        "\\s",
                        ""
                );

        if (GSTIN_PATTERN.matcher(
                compact
        ).find()) {
            return true;
        }

        if (normalized.matches(
                ".*\\b(?:gstin|gst|cgst|sgst|igst|vat|tin|" +
                        "tel|telephone|phone|mobile|date|time|" +
                        "table|guest|ref no|reference|bill no|" +
                        "invoice|address|pincode|thank you|" +
                        "total|tax|subtotal|payment|cash|card|upi)\\b.*"
        )) {
            return true;
        }

        /*
         * Long numeric strings are normally invoice,
         * phone, reference, GST, or transaction numbers.
         */
        return compact.replaceAll(
                "\\D",
                ""
        ).length() >= 10;
    }

    private static List<NumberToken> numericTokens(
            String line
    ) {

        List<NumberToken> tokens =
                new ArrayList<>();

        Matcher matcher =
                NUMERIC_TOKEN_PATTERN.matcher(line);

        while (matcher.find()) {

            String after =
                    line.substring(
                            matcher.end()
                    ).stripLeading();

            if (after.startsWith("%")) {
                continue;
            }

            tokens.add(
                    new NumberToken(
                            matcher.group(1),
                            matcher.start()
                    )
            );
        }

        return tokens;
    }

    private static boolean isPlausibleQuantity(
            BigDecimal value
    ) {

        return value != null &&
                value.signum() > 0 &&
                value.compareTo(
                        new BigDecimal("1000")
                ) <= 0;
    }

    private static boolean isPlausibleAmount(
            BigDecimal value
    ) {

        return value != null &&
                value.signum() >= 0 &&
                value.compareTo(
                        new BigDecimal("10000000")
                ) <= 0;
    }

    private static String suggestedCategory(
            String text,
            String merchant,
            List<ReceiptLineItem> items
    ) {

        String normalized =
                (
                        text +
                                " " +
                                (
                                        merchant == null
                                                ? ""
                                                : merchant
                                )
                ).toLowerCase(
                        Locale.ROOT
                );

        if (containsAny(
                normalized,
                "restaurant",
                "cafe",
                "coffee",
                "food",
                "dining",
                "swiggy",
                "zomato",
                "idli",
                "dosa",
                "tiffin",
                "mess",
                "bakery",
                "canteen"
        )) {
            return "Food";
        }

        if (containsAny(
                normalized,
                "grocery",
                "supermarket",
                "vegetable",
                "milk",
                "walmart",
                "dmart"
        )) {
            return "Grocery";
        }

        if (containsAny(
                normalized,
                "amazon",
                "shopping",
                "mall",
                "clothing",
                "fashion",
                "flipkart"
        )) {
            return "Shopping";
        }

        if (containsAny(
                normalized,
                "flight",
                "hotel",
                "travel",
                "uber",
                "ola",
                "fuel",
                "petrol",
                "diesel",
                "transport"
        )) {
            return "Transport";
        }

        if (containsAny(
                normalized,
                "pharmacy",
                "hospital",
                "medical",
                "health",
                "apollo"
        )) {
            return "Medical";
        }

        if (containsAny(
                normalized,
                "electricity",
                "water",
                "internet",
                "utility",
                "airtel",
                "jio"
        )) {
            return "Utilities";
        }

        if (containsAny(
                normalized,
                "movie",
                "cinema",
                "concert",
                "entertainment"
        )) {
            return "Entertainment";
        }

        if (containsAny(
                normalized,
                "school",
                "course",
                "education",
                "college"
        )) {
            return "Education";
        }

        return items.isEmpty()
                ? null
                : "Other";
    }

    private static boolean containsAny(
            String value,
            String... candidates
    ) {

        for (String candidate : candidates) {

            if (value.contains(candidate)) {
                return true;
            }
        }

        return false;
    }

    private static double totalConfidence(
            String text
    ) {

        if (text.matches(
                "(?is).*\\b(?:grand\\s+total|" +
                        "amount\\s+(?:payable|due)|" +
                        "net\\s+total|" +
                        "total\\s+amount)\\b.*"
        )) {
            return 0.96;
        }

        if (text.matches(
                "(?is).*\\btotal\\b.*"
        )) {
            return 0.88;
        }

        return 0.70;
    }

    private static Validation validateFinancials(
            BigDecimal subtotal,
            BigDecimal tax,
            BigDecimal total,
            List<ReceiptLineItem> items
    ) {

        if (total != null &&
                total.signum() <= 0) {

            return new Validation(
                    false,
                    "Total amount must be positive."
            );
        }

        if (subtotal != null &&
                tax != null &&
                total != null) {

            BigDecimal difference =
                    subtotal
                            .add(tax)
                            .subtract(total)
                            .abs();

            if (difference.compareTo(
                    new BigDecimal("0.10")
            ) > 0) {

                return new Validation(
                        false,
                        "Subtotal and tax do not closely match the detected total."
                );
            }
        }

        if (!items.isEmpty() &&
                subtotal != null) {

            BigDecimal itemTotal =
                    items.stream()
                            .map(
                                    ReceiptLineItem::lineTotal
                            )
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            if (itemTotal
                    .subtract(subtotal)
                    .abs()
                    .compareTo(
                            new BigDecimal("0.10")
                    ) > 0) {

                return new Validation(
                        false,
                        "Detected line items do not closely match the subtotal."
                );
            }
        }

        if (subtotal != null ||
                tax != null ||
                total != null) {

            return new Validation(
                    true,
                    null
            );
        }

        return new Validation(
                null,
                "No financial values could be confidently detected."
        );
    }

    private static double overallConfidence(
            double merchant,
            double date,
            double total,
            double tax,
            double items,
            Boolean consistent
    ) {

        /*
         * Total and date are more important than line items
         * for a receipt scanner.
         */
        double score =
                merchant * 0.15 +
                        date * 0.15 +
                        total * 0.40 +
                        tax * 0.10 +
                        items * 0.20;

        if (Boolean.TRUE.equals(consistent)) {
            score += 0.05;
        }

        if (Boolean.FALSE.equals(consistent)) {
            score -= 0.15;
        }

        return Math.max(
                0.0,
                Math.min(
                        0.98,
                        Math.round(score * 100.0) / 100.0
                )
        );
    }

    private record Validation(
            Boolean consistent,
            String message
    ) {
    }

    private record TaxBreakdown(
            BigDecimal total,
            BigDecimal cgst,
            BigDecimal sgst,
            BigDecimal igst
    ) {
    }

    private record NumberToken(
            String value,
            int start
    ) {
    }
}