package com.digtp.scm.portbalance.style;

import java.util.EnumMap;
import java.util.Map;

public class StylePalette {

    private final Map<StyleToken, String> styles;

    public StylePalette(Map<StyleToken, String> styles) {
        this.styles = new EnumMap<>(styles);
    }

    public static StylePalette portBalance() {
        Map<StyleToken, String> styles = new EnumMap<>(StyleToken.class);
        styles.put(StyleToken.BASE, "background-color:#FFFFFF;color:#1F2937;");
        styles.put(StyleToken.MONTH_BREAK, "background-color:#E5E7EB;color:#4B5563;font-weight:600;");
        styles.put(StyleToken.TODAY_ROW, "border-top:2px solid #2563EB;border-bottom:2px solid #2563EB;");
        styles.put(StyleToken.NEGATIVE_STOCK, "color:#DC2626;background-color:#FEE2E2;font-weight:600;");
        styles.put(StyleToken.METRIC_IN, "color:#2563EB;");
        styles.put(StyleToken.METRIC_OUT, "color:#7C3AED;");
        styles.put(StyleToken.METRIC_STOCK, "color:#111827;");
        styles.put(StyleToken.METRIC_VESSEL, "color:#0F766E;");
        styles.put(StyleToken.METRIC_LAYCAN, "color:#6B7280;");
        styles.put(StyleToken.METRIC_TOTAL_OUT, "color:#111827;");
        styles.put(StyleToken.MULTI_VESSEL, "background-color:#F3E8FF;color:#7C3AED;font-weight:600;");
        return new StylePalette(styles);
    }

    public String get(StyleToken token) {
        return styles.get(token);
    }
}
