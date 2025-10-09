package org.apache.maven.plugin.surefire.extensions.junit5;

import org.apache.maven.plugin.surefire.loader.SurefireClassLoaderModifier;
import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;
import org.apache.maven.plugin.surefire.report.ConsoleTreeReporter;
import org.apache.maven.plugin.surefire.report.ReporterOptions;
import org.apache.maven.plugin.surefire.report.TestSetStats;
import org.apache.maven.plugin.surefire.report.Theme;
import org.apache.maven.plugin.surefire.report.WrappedReportEntry;
import org.apache.maven.surefire.extensions.StatelessTestsetInfoConsoleReportEventListener;

/**
 * Extension of {@link JUnit5StatelessTestsetInfoReporter file and console
 * reporter of test-set} for JUnit5.
 *
 * @author <a href="mailto:fabriciorby@hotmail.com">Fabrício Yamamoto
 *         (fabriciorby)</a>
 */
public class JUnit5StatelessTestsetInfoTreeReporter extends JUnit5StatelessTestsetInfoReporter {
    private boolean printStacktraceOnError;
    private boolean printStacktraceOnFailure;
    private boolean printStderrOnError;
    private boolean printStderrOnFailure;
    private boolean printStderrOnSuccess;
    private boolean printStdoutOnError;
    private boolean printStdoutOnFailure;
    private boolean printStdoutOnSuccess;
    private boolean hideResultsOnSuccess;
    private boolean printBlankLineBetweenTests;
    private Theme theme = Theme.ASCII;

    @Override
    public Object clone(ClassLoader target) {
        try {
            new SurefireClassLoaderModifier(target).addThisToSurefireClassLoader();

            Object clone = super.clone(target);

            Class<?> cls = clone.getClass();

            Class<?> themeClass = target.loadClass(Theme.class.getName());
            @SuppressWarnings({ "rawtypes", "unchecked" })
            Object clonedTheme = Enum.valueOf((Class) themeClass, getTheme().name());

            cls.getMethod("setPrintStacktraceOnError", boolean.class).invoke(clone, isPrintStacktraceOnError());
            cls.getMethod("setPrintStacktraceOnFailure", boolean.class).invoke(clone, isPrintStacktraceOnFailure());
            cls.getMethod("setPrintStderrOnError", boolean.class).invoke(clone, isPrintStderrOnError());
            cls.getMethod("setPrintStderrOnFailure", boolean.class).invoke(clone, isPrintStderrOnFailure());
            cls.getMethod("setPrintStderrOnSuccess", boolean.class).invoke(clone, isPrintStderrOnSuccess());
            cls.getMethod("setPrintStdoutOnError", boolean.class).invoke(clone, isPrintStdoutOnError());
            cls.getMethod("setPrintStdoutOnFailure", boolean.class).invoke(clone, isPrintStdoutOnFailure());
            cls.getMethod("setPrintStdoutOnSuccess", boolean.class).invoke(clone, isPrintStdoutOnSuccess());
            cls.getMethod("setHideResultsOnSuccess", boolean.class).invoke(clone, isPrintStdoutOnSuccess());
            cls.getMethod("setTheme", themeClass).invoke(clone, clonedTheme);

            return clone;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public StatelessTestsetInfoConsoleReportEventListener<WrappedReportEntry, TestSetStats> createListener(
            ConsoleLogger logger) {
        return new ConsoleTreeReporter(logger, newReporterOptions());
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

    public boolean isPrintBlankLineBetweenTests() {
        return printBlankLineBetweenTests;
    }

    public void setPrintStacktraceOnError(boolean printStacktraceOnError) {
        this.printStacktraceOnError = printStacktraceOnError;
    }

    public void setPrintStacktraceOnFailure(boolean printStacktraceOnFailure) {
        this.printStacktraceOnFailure = printStacktraceOnFailure;
    }

    public void setPrintStderrOnError(boolean printStderrOnError) {
        this.printStderrOnError = printStderrOnError;
    }

    public void setPrintStderrOnFailure(boolean printStderrOnFailure) {
        this.printStderrOnFailure = printStderrOnFailure;
    }

    public void setPrintStderrOnSuccess(boolean printStderrOnSuccess) {
        this.printStderrOnSuccess = printStderrOnSuccess;
    }

    public void setPrintStdoutOnError(boolean printStdoutOnError) {
        this.printStdoutOnError = printStdoutOnError;
    }

    public void setPrintStdoutOnFailure(boolean printStdoutOnFailure) {
        this.printStdoutOnFailure = printStdoutOnFailure;
    }

    public void setPrintStdoutOnSuccess(boolean printStdoutOnSuccess) {
        this.printStdoutOnSuccess = printStdoutOnSuccess;
    }

    public void setHideResultsOnSuccess(boolean hideResultsOnSuccess) {
        this.hideResultsOnSuccess = hideResultsOnSuccess;
    }

    public void setPrintBlankLineBetweenTests(boolean printBlankLineBetweenTests) {
        this.printBlankLineBetweenTests = printBlankLineBetweenTests;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{"
                + "disable=" + isDisable()
                + ", usePhrasedFileName=" + isUsePhrasedFileName()
                + ", usePhrasedClassNameInRunning=" + isUsePhrasedClassNameInRunning()
                + ", usePhrasedClassNameInTestCaseSummary=" + isUsePhrasedClassNameInTestCaseSummary()
                + "}";
    }

    private ReporterOptions newReporterOptions() {
        return ReporterOptions.builder()
                .printStacktraceOnError(isPrintStacktraceOnError())
                .printStacktraceOnFailure(isPrintStacktraceOnFailure())
                .printStderrOnError(isPrintStderrOnError())
                .printStderrOnFailure(isPrintStderrOnFailure())
                .printStderrOnSuccess(isPrintStderrOnSuccess())
                .printStdoutOnError(isPrintStdoutOnError())
                .printStdoutOnFailure(isPrintStdoutOnFailure())
                .printStdoutOnSuccess(isPrintStdoutOnSuccess())
                .hideResultsOnSuccess(isHideResultsOnSuccess())
                .usePhrasedClassNameInRunning(isUsePhrasedClassNameInRunning())
                .usePhrasedClassNameInTestCaseSummary(isUsePhrasedClassNameInTestCaseSummary())
                .printBlankLineBetweenTests(isPrintBlankLineBetweenTests())
                .theme(getTheme())
                .build();
    }

}
