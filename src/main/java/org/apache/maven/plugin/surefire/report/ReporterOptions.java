package org.apache.maven.plugin.surefire.report;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReporterOptions {
    private static final Pattern DURATION = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(ms|s)?", Pattern.CASE_INSENSITIVE);

    /** Value of {@link #getSlowTestThresholdMillis()} when the slow test marker is off. */
    public static final long SLOW_TEST_THRESHOLD_DISABLED = -1L;

    private final boolean printStacktraceOnError;
    private final boolean printStacktraceOnFailure;
    private final boolean printStderrOnError;
    private final boolean printStderrOnFailure;
    private final boolean printStderrOnSuccess;
    private final boolean printStdoutOnError;
    private final boolean printStdoutOnFailure;
    private final boolean printStdoutOnSuccess;
    private final boolean hideResultsOnSuccess;
    private final Theme theme;
    private final boolean usePhrasedClassNameInRunning;
    private final boolean usePhrasedClassNameInTestCaseSummary;
    private final boolean printBlankLineBetweenTests;
    private final boolean printTestCounts;
    private final boolean collapseSuccessfulClasses;
    private final boolean hideSuccessfulNestedClasses;
    private final long slowTestThresholdMillis;
    private final boolean printFailureSummary;
    private final int maxFailuresInSummary;

    private ReporterOptions(Builder builder) {
        this.printStacktraceOnError = builder.printStacktraceOnError;
        this.printStacktraceOnFailure = builder.printStacktraceOnFailure;
        this.printStderrOnError = builder.printStderrOnError;
        this.printStderrOnFailure = builder.printStderrOnFailure;
        this.printStderrOnSuccess = builder.printStderrOnSuccess;
        this.printStdoutOnError = builder.printStdoutOnError;
        this.printStdoutOnFailure = builder.printStdoutOnFailure;
        this.printStdoutOnSuccess = builder.printStdoutOnSuccess;
        this.hideResultsOnSuccess = builder.hideResultsOnSuccess;
        this.usePhrasedClassNameInRunning = builder.usePhrasedClassNameInRunning;
        this.usePhrasedClassNameInTestCaseSummary = builder.usePhrasedClassNameInTestCaseSummary;
        this.theme = builder.theme != null ? builder.theme : Theme.ASCII;
        this.printBlankLineBetweenTests = builder.printBlankLineBetweenTests;
        this.printTestCounts = builder.printTestCounts;
        this.collapseSuccessfulClasses = builder.collapseSuccessfulClasses;
        this.hideSuccessfulNestedClasses = builder.hideSuccessfulNestedClasses;
        this.slowTestThresholdMillis = parseSlowTestThreshold(builder.slowTestThreshold);
        this.printFailureSummary = builder.printFailureSummary;
        this.maxFailuresInSummary = builder.maxFailuresInSummary;
    }

    /**
     * Parses {@code 500ms}, {@code 2s}, {@code 1.5s} or a bare number of milliseconds.
     *
     * @return the milliseconds, or {@link #SLOW_TEST_THRESHOLD_DISABLED} for a null or blank value
     * @throws IllegalArgumentException when the value is not a positive duration
     */
    public static long parseSlowTestThreshold(String value) {
        if (value == null || value.trim().isEmpty()) {
            return SLOW_TEST_THRESHOLD_DISABLED;
        }
        Matcher matcher = DURATION.matcher(value.trim());
        if (!matcher.matches()) {
            throw invalidThreshold(value);
        }
        double amount = Double.parseDouble(matcher.group(1));
        boolean seconds = "s".equalsIgnoreCase(matcher.group(2));
        long millis = Math.round(seconds ? amount * 1000 : amount);
        if (millis <= 0) {
            throw invalidThreshold(value);
        }
        return millis;
    }

    private static IllegalArgumentException invalidThreshold(String value) {
        return new IllegalArgumentException("Invalid slowTestThreshold '" + value
                + "', expected a positive duration like 500ms, 2s, or a number of milliseconds");
    }

    public static Builder builder() {
        return new Builder();
    }

    public Theme getTheme() {
        return theme;
    }

    public boolean isPrintStacktraceOnError() {
        return printStacktraceOnError;
    }

    public boolean isPrintStacktraceOnFailure() {
        return printStacktraceOnFailure;
    }

    public boolean isPrintStderrOnError() {
        return printStderrOnError;
    }

    public boolean isPrintStderrOnFailure() {
        return printStderrOnFailure;
    }

    public boolean isPrintStderrOnSuccess() {
        return printStderrOnSuccess;
    }

    public boolean isPrintStdoutOnError() {
        return printStdoutOnError;
    }

    public boolean isPrintStdoutOnFailure() {
        return printStdoutOnFailure;
    }

    public boolean isPrintStdoutOnSuccess() {
        return printStdoutOnSuccess;
    }

    public boolean isHideResultsOnSuccess() {
        return hideResultsOnSuccess;
    }

    public boolean isUsePhrasedClassNameInRunning() {
        return usePhrasedClassNameInRunning;
    }

    public boolean isUsePhrasedClassNameInTestCaseSummary() {
        return usePhrasedClassNameInTestCaseSummary;
    }

    public boolean isPrintBlankLineBetweenTests() {
        return printBlankLineBetweenTests;
    }

    public boolean isPrintTestCounts() {
        return printTestCounts;
    }

    public boolean isCollapseSuccessfulClasses() {
        return collapseSuccessfulClasses;
    }

    public boolean isHideSuccessfulNestedClasses() {
        return hideSuccessfulNestedClasses;
    }

    /**
     * @return the elapsed time, in milliseconds, from which a test is marked as slow, or
     *         {@link #SLOW_TEST_THRESHOLD_DISABLED}
     */
    public long getSlowTestThresholdMillis() {
        return slowTestThresholdMillis;
    }

    public boolean isPrintFailureSummary() {
        return printFailureSummary;
    }

    public int getMaxFailuresInSummary() {
        return maxFailuresInSummary;
    }

    public static final class Builder {
        private boolean printStacktraceOnError;
        private boolean printStacktraceOnFailure;
        private boolean printStderrOnError;
        private boolean printStderrOnFailure;
        private boolean printStderrOnSuccess;
        private boolean printStdoutOnError;
        private boolean printStdoutOnFailure;
        private boolean printStdoutOnSuccess;
        private boolean hideResultsOnSuccess;
        private Theme theme;
        private boolean usePhrasedClassNameInRunning;
        private boolean usePhrasedClassNameInTestCaseSummary;
        private boolean printBlankLineBetweenTests;
        private boolean printTestCounts;
        private boolean collapseSuccessfulClasses;
        private boolean hideSuccessfulNestedClasses;
        private String slowTestThreshold;
        private boolean printFailureSummary;
        private int maxFailuresInSummary = 50;

        private Builder() {
        }

        public ReporterOptions build() {
            return new ReporterOptions(this);
        }

        public Builder printStacktraceOnError(boolean printStacktraceOnError) {
            this.printStacktraceOnError = printStacktraceOnError;
            return this;
        }

        public Builder printStacktraceOnFailure(boolean printStacktraceOnFailure) {
            this.printStacktraceOnFailure = printStacktraceOnFailure;
            return this;
        }

        public Builder printStderrOnError(boolean printStderrOnError) {
            this.printStderrOnError = printStderrOnError;
            return this;
        }

        public Builder printStderrOnFailure(boolean printStderrOnFailure) {
            this.printStderrOnFailure = printStderrOnFailure;
            return this;
        }

        public Builder printStderrOnSuccess(boolean printStderrOnSuccess) {
            this.printStderrOnSuccess = printStderrOnSuccess;
            return this;
        }

        public Builder printStdoutOnError(boolean printStdoutOnError) {
            this.printStdoutOnError = printStdoutOnError;
            return this;
        }

        public Builder printStdoutOnFailure(boolean printStdoutOnFailure) {
            this.printStdoutOnFailure = printStdoutOnFailure;
            return this;
        }

        public Builder printStdoutOnSuccess(boolean printStdoutOnSuccess) {
            this.printStdoutOnSuccess = printStdoutOnSuccess;
            return this;
        }

        public Builder hideResultsOnSuccess(boolean hideResultsOnSuccess) {
            this.hideResultsOnSuccess = hideResultsOnSuccess;
            return this;
        }

        public Builder theme(Theme theme) {
            this.theme = theme;
            return this;
        }

        public Builder usePhrasedClassNameInRunning(boolean usePhrasedClassNameInRunning) {
            this.usePhrasedClassNameInRunning = usePhrasedClassNameInRunning;
            return this;
        }

        public Builder usePhrasedClassNameInTestCaseSummary(boolean usePhrasedClassNameInTestCaseSummary) {
            this.usePhrasedClassNameInTestCaseSummary = usePhrasedClassNameInTestCaseSummary;
            return this;
        }

        public Builder printBlankLineBetweenTests(boolean printBlankLineBetweenTests) {
            this.printBlankLineBetweenTests = printBlankLineBetweenTests;
            return this;
        }

        public Builder printTestCounts(boolean printTestCounts) {
            this.printTestCounts = printTestCounts;
            return this;
        }

        public Builder collapseSuccessfulClasses(boolean collapseSuccessfulClasses) {
            this.collapseSuccessfulClasses = collapseSuccessfulClasses;
            return this;
        }

        public Builder hideSuccessfulNestedClasses(boolean hideSuccessfulNestedClasses) {
            this.hideSuccessfulNestedClasses = hideSuccessfulNestedClasses;
            return this;
        }

        public Builder slowTestThreshold(String slowTestThreshold) {
            this.slowTestThreshold = slowTestThreshold;
            return this;
        }

        public Builder printFailureSummary(boolean printFailureSummary) {
            this.printFailureSummary = printFailureSummary;
            return this;
        }

        public Builder maxFailuresInSummary(int maxFailuresInSummary) {
            this.maxFailuresInSummary = maxFailuresInSummary;
            return this;
        }
    }
}
