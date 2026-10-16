package org.apache.maven.plugin.surefire.extensions.junit5;

import org.apache.maven.plugin.surefire.extensions.SurefireConsoleOutputReporter;
import org.apache.maven.plugin.surefire.loader.SurefireClassLoaderModifier;
import org.apache.maven.plugin.surefire.report.FailureSummary;
import org.apache.maven.surefire.api.report.TestOutputReportEntry;
import org.apache.maven.surefire.api.report.TestSetReportEntry;
import org.apache.maven.surefire.extensions.ConsoleOutputReportEventListener;

import java.io.File;
import java.io.PrintStream;

/**
 * Replaces {@code <consoleOutputReporter><disable>true</disable></consoleOutputReporter>} when the failure summary
 * (option {@code printFailureSummary} of {@link JUnit5StatelessTestsetInfoTreeReporter}) is wanted.
 * <p>
 * Surefire has no end-of-execution call for the reporter of the tree, but it closes the listener of the console
 * output reporter after printing the totals of the execution. This reporter does what {@code disable} does while the
 * tests run, it drops the output captured from the tests, and prints the summary when it is closed.
 */
public class JUnit5ConsoleOutputTreeReporter extends SurefireConsoleOutputReporter {

    @Override
    public ConsoleOutputReportEventListener createListener(
            File reportsDirectory, String reportNameSuffix, Integer forkNumber) {
        return new SummaryListener();
    }

    @Override
    public ConsoleOutputReportEventListener createListener(PrintStream out, PrintStream err) {
        return new SummaryListener();
    }

    @Override
    public Object clone(ClassLoader target) {
        try {
            new SurefireClassLoaderModifier(target).addThisToSurefireClassLoader();
            return super.clone(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "JUnit5ConsoleOutputTreeReporter{"
                + "disable=" + isDisable()
                + ", encoding=" + getEncoding()
                + '}';
    }

    private static final class SummaryListener implements ConsoleOutputReportEventListener {
        @Override
        public void testSetStarting(TestSetReportEntry reportEntry) {
        }

        @Override
        public void testSetCompleted(TestSetReportEntry testSetReportEntry) {
        }

        @Override
        public void writeTestOutput(TestOutputReportEntry reportEntry) {
        }

        @Override
        public void close() {
            FailureSummary.printAndClear();
        }
    }
}
