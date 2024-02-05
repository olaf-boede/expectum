package de.cleanitworks.expectum.core.resource;

import org.apache.commons.lang3.StringUtils;

public class TextNodeQuoteWorkaround {

    private static final String QUOTE = "\"";

    // TODO: Strings are delivered with extra quotes. Mapper adjustment or different api usage needed?
    public static String unquote(String nodeString) {
        return StringUtils.startsWith(nodeString, QUOTE) && StringUtils.endsWith(nodeString, QUOTE)
                ? StringUtils.removeEnd(StringUtils.removeStart(nodeString, QUOTE), QUOTE)
                : nodeString;
    }
}