package org.apache.maven.plugin.surefire.report;

import java.util.List;

import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;
import org.apache.maven.surefire.api.report.TestSetReportEntry;

public class ConsoleTreeReporter extends ConsoleReporter {
    /**
     * With forkCount above 1 surefire reports each fork from its own thread, each with its own reporter, while the tree
     * is shared by all of them. The tree is only changed and printed under this lock, so each class is printed in one
     * piece.
     */
    private static final Object TREE_LOCK = new Object();

    private final ReporterOptions options;

    public ConsoleTreeReporter(ConsoleLogger logger, ReporterOptions options) {
        super(logger, options.isUsePhrasedClassNameInRunning(), options.isUsePhrasedClassNameInTestCaseSummary());
        this.options = options;
    }

    @Override
    public void testSetStarting(TestSetReportEntry report) {
        synchronized (TREE_LOCK) {
            new TestReportHandler(report).prepare();
        }
    }

    @Override
    public void testSetCompleted(WrappedReportEntry report, TestSetStats testSetStats, List<String> testResults) {
        synchronized (TREE_LOCK) {
            new TestReportHandler(report, testSetStats).print(getTreePrinter());
        }
    }

    private TreePrinter getTreePrinter() {
        return new TreePrinter(getConsoleLogger(), options);
    }
}
