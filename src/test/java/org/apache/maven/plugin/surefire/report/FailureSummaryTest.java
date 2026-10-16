package org.apache.maven.plugin.surefire.report;

import org.apache.maven.surefire.api.report.RunMode;
import org.apache.maven.surefire.api.report.SimpleReportEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class FailureSummaryTest {

    private static final String BETA = "x.BetaTest";
    private static final String ALPHA = "x.AlphaTest";

    private final Utf8RecodingDeferredFileOutputStream stdout = new Utf8RecodingDeferredFileOutputStream("stdout");
    private final Utf8RecodingDeferredFileOutputStream stderr = new Utf8RecodingDeferredFileOutputStream("stderr");
    private final CapturingLogger logger = new CapturingLogger();

    @BeforeEach
    void clean() {
        Node.clearTree();
        FailureSummary.printAndClear();
    }

    @Test
    void printsTheFailedTestsAfterTheRunAndOnlyOnce() {
        runClasses(options(true, 50));
        logger.lines.clear();

        FailureSummary.printAndClear();

        assertThat(logger.lines).containsExactly(
                "Failed tests (3):",
                "  Beta > fails",
                "  Beta > Inner > errors",
                "  Beta > Inner > Deeper > fails too",
                "");
        // surefire closes the reporter once per fork, the others have nothing left to print
        logger.lines.clear();
        FailureSummary.printAndClear();
        assertThat(logger.lines).isEmpty();
        assertThat(FailureSummary.isEmpty()).isTrue();
    }

    @Test
    void cutsTheListAtMaxFailuresInSummary() {
        runClasses(options(true, 2));
        logger.lines.clear();

        FailureSummary.printAndClear();

        assertThat(logger.lines).containsExactly(
                "Failed tests (3):",
                "  Beta > fails",
                "  Beta > Inner > errors",
                "  ... and 1 more",
                "");
    }

    @Test
    void printsNothingWhenTheOptionIsOff() {
        runClasses(options(false, 50));
        logger.lines.clear();

        FailureSummary.printAndClear();

        assertThat(logger.lines).isEmpty();
        assertThat(FailureSummary.isEmpty()).isTrue();
    }

    @Test
    void forksCloseTheReporterOneAfterTheOther() throws Exception {
        runClasses(options(true, 50));
        logger.lines.clear();
        int forks = 4;
        ExecutorService executor = Executors.newFixedThreadPool(forks);
        CountDownLatch start = new CountDownLatch(1);
        for (int i = 0; i < forks; i++) {
            executor.submit(() -> {
                start.await();
                FailureSummary.printAndClear();
                return null;
            });
        }
        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)).isTrue();

        assertThat(logger.lines).filteredOn(line -> line.startsWith("Failed tests")).hasSize(1);
    }

    private void runClasses(ReporterOptions options) {
        ConsoleTreeReporter reporter = new ConsoleTreeReporter(logger, options);

        WrappedReportEntry alpha = wrap(classEntry(ALPHA, "", "Alpha"), ReportEntryType.SUCCESS);
        reporter.testSetStarting(alpha);
        reporter.testSetCompleted(alpha, stats(testEntry(ALPHA, "", "passes", ReportEntryType.SUCCESS),
                testEntry(ALPHA, "", "passes too", ReportEntryType.SUCCESS)), null);

        WrappedReportEntry beta = wrap(classEntry(BETA, "", "Beta"), ReportEntryType.SUCCESS);
        WrappedReportEntry inner = wrap(classEntry(BETA, "$Inner", "Inner"), ReportEntryType.SUCCESS);
        WrappedReportEntry deeper = wrap(classEntry(BETA, "$Inner$Deeper", "Deeper"), ReportEntryType.SUCCESS);
        reporter.testSetStarting(beta);
        reporter.testSetStarting(inner);
        reporter.testSetStarting(deeper);
        reporter.testSetCompleted(deeper, stats(
                testEntry(BETA, "", "fails", ReportEntryType.FAILURE),
                testEntry(BETA, "$Inner", "errors", ReportEntryType.ERROR),
                testEntry(BETA, "$Inner$Deeper", "fails too", ReportEntryType.FAILURE)), null);
        reporter.testSetCompleted(inner, stats(), null);
        reporter.testSetCompleted(beta, stats(), null);
    }

    private static ReporterOptions options(boolean printFailureSummary, int max) {
        return ReporterOptions.builder().theme(Theme.UNICODE).printFailureSummary(printFailureSummary)
                .maxFailuresInSummary(max).build();
    }

    private static SimpleReportEntry classEntry(String topLevel, String nestedPath, String displayName) {
        return new SimpleReportEntry(RunMode.NORMAL_RUN, 1L, topLevel, displayName, topLevel + nestedPath,
                null, null, null, null, null, Collections.emptyMap());
    }

    private WrappedReportEntry testEntry(String topLevel, String nestedPath, String displayName, ReportEntryType type) {
        return wrap(new SimpleReportEntry(RunMode.NORMAL_RUN, 1L, topLevel, null, topLevel + nestedPath,
                "test", displayName, null, 1, null, Collections.emptyMap()), type);
    }

    private WrappedReportEntry wrap(SimpleReportEntry entry, ReportEntryType type) {
        return new WrappedReportEntry(entry, type, 0L, 1, stdout, stderr);
    }

    private static TestSetStats stats(WrappedReportEntry... entries) {
        TestSetStats testSetStats = new TestSetStats(false, true);
        List<WrappedReportEntry> list = Arrays.asList(entries);
        for (WrappedReportEntry entry : list) {
            switch (entry.getReportEntryType()) {
                case ERROR:
                    testSetStats.testError(entry);
                    break;
                case FAILURE:
                    testSetStats.testFailure(entry);
                    break;
                default:
                    testSetStats.testSucceeded(entry);
            }
        }
        return testSetStats;
    }
}
