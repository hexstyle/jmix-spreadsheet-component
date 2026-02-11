package com.digtp.scm.portbalance.style;

import com.digtp.scm.portbalance.columns.PortBalanceMetric;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PortBalanceStyleEngine {

    private final List<StyleRule> rules;

    public PortBalanceStyleEngine() {
        this.rules = defaultRules();
    }

    public PortBalanceStyleEngine(List<StyleRule> rules) {
        this.rules = new ArrayList<>(rules);
    }

    public List<StyleToken> resolveTokens(CellContext context) {
        Set<StyleToken> tokens = new java.util.LinkedHashSet<>();
        for (StyleRule rule : rules) {
            if (rule.matches(context)) {
                tokens.add(rule.token());
            }
        }
        return List.copyOf(tokens);
    }

    public String resolveStyle(CellContext context, StylePalette palette) {
        return resolveTokens(context).stream()
                .map(palette::get)
                .filter(Objects::nonNull)
                .collect(Collectors.joining());
    }

    private List<StyleRule> defaultRules() {
        List<StyleRule> rules = new ArrayList<>();
        rules.add(new StyleRule(ctx -> true, StyleToken.BASE));
        rules.add(new StyleRule(CellContext::isMonthBreak, StyleToken.MONTH_BREAK));
        rules.add(new StyleRule(CellContext::isTodayRow, StyleToken.TODAY_ROW));

        rules.add(new StyleRule(ctx -> ctx.getMetric() == PortBalanceMetric.IN, StyleToken.METRIC_IN));
        rules.add(new StyleRule(ctx -> ctx.getMetric() == PortBalanceMetric.OUT, StyleToken.METRIC_OUT));
        rules.add(new StyleRule(ctx -> ctx.getMetric() == PortBalanceMetric.STOCK, StyleToken.METRIC_STOCK));
        rules.add(new StyleRule(ctx -> ctx.getMetric() == PortBalanceMetric.VESSEL, StyleToken.METRIC_VESSEL));
        rules.add(new StyleRule(ctx -> ctx.getMetric() == PortBalanceMetric.LAYCAN, StyleToken.METRIC_LAYCAN));
        rules.add(new StyleRule(ctx -> ctx.getMetric() == PortBalanceMetric.TOTAL_OUT, StyleToken.METRIC_TOTAL_OUT));

        rules.add(new StyleRule(CellContext::isMultiVessel, StyleToken.MULTI_VESSEL));
        rules.add(new StyleRule(CellContext::isNegativeStock, StyleToken.NEGATIVE_STOCK));
        return rules;
    }
}
