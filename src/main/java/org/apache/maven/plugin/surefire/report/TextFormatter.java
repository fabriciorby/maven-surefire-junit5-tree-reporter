package org.apache.maven.plugin.surefire.report;

import static org.apache.maven.surefire.shared.lang3.StringUtils.abbreviate;
import static org.apache.maven.surefire.shared.lang3.StringUtils.normalizeSpace;

public class TextFormatter {

    public static final int MAX_WIDTH = 180;
    public static final String ABBREV_MARKER = "...";

    public static String abbreviateName(String text) {
        return abbreviate(normalizeSpace(text), ABBREV_MARKER, MAX_WIDTH);
    }
}
