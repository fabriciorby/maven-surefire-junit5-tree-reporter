package org.apache.maven.plugin.surefire.report;

public class ReporterOptions {
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
    }
}
