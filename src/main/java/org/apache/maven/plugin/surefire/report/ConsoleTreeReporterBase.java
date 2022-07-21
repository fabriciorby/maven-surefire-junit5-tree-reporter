package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;
import org.apache.maven.surefire.api.report.TestSetReportEntry;

import java.util.List;

abstract class ConsoleTreeReporterBase extends ConsoleReporter {

    public ConsoleTreeReporterBase(ConsoleLogger logger, boolean usePhrasedClassNameInRunning,
                                   boolean usePhrasedClassNameInTestCaseSummary) {
        super(logger, usePhrasedClassNameInRunning, usePhrasedClassNameInTestCaseSummary);
    }

    abstract TreePrinter getTreePrinter(List<WrappedReportEntry> classEntries, List<WrappedReportEntry> testEntries);

    @Override
    public void testSetStarting(TestSetReportEntry report) {
        new TestHandler(report)
                .prepare();
    }

    @Override
    public void testSetCompleted(WrappedReportEntry report, TestSetStats testSetStats, List<String> testResults) {
        new TestHandler(report, testSetStats)
                .print(this::getTreePrinter);
    }

}
