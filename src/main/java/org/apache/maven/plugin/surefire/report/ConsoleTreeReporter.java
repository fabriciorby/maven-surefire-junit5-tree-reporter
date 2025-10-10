package org.apache.maven.plugin.surefire.report;

import java.util.List;

import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;
import org.apache.maven.surefire.api.report.TestSetReportEntry;

public class ConsoleTreeReporter extends ConsoleReporter {
    private final ReporterOptions options;

    public ConsoleTreeReporter(ConsoleLogger logger, ReporterOptions options) {
        super(logger, options.isUsePhrasedClassNameInRunning(), options.isUsePhrasedClassNameInTestCaseSummary());
        this.options = options;
    }

    @Override
    public void testSetStarting(TestSetReportEntry report) {
        new TestReportHandler(report).prepare();
    }

    @Override
    public void testSetCompleted(WrappedReportEntry report, TestSetStats testSetStats, List<String> testResults) {
        new TestReportHandler(report, testSetStats).print(getTreePrinter());
    }

    private TreePrinter getTreePrinter() {
        return new TreePrinter(getConsoleLogger(), options);
    }
}
