package org.apache.maven.plugin.surefire.report;

import org.apache.maven.surefire.api.report.RunMode;
import org.apache.maven.surefire.api.report.SimpleReportEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

    @TempDir
    File dir;

    private final Utf8RecodingDeferredFileOutputStream stdout = new Utf8RecodingDeferredFileOutputStream("stdout");
    private final Utf8RecodingDeferredFileOutputStream stderr = new Utf8RecodingDeferredFileOutputStream("stderr");
    private final CapturingLogger logger = new CapturingLogger();

    @BeforeEach
    void clean() {
        Node.clearTree();
        FailureSummary.printAndClear(null);
    }

    @Test
    void printsTheFailedTestsAfterTheRunAndOnlyOnce() {
        runClasses(options(true, false, 50));
        logger.lines.clear();

        FailureSummary.printAndClear(null);

        assertThat(logger.lines).containsExactly(
                "Failed tests (3):",
                "  Beta > fails",
                "  Beta > Inner > errors",
                "  Beta > Inner > Deeper > fails too",
                "");
        // surefire closes the reporter once per fork, the others have nothing left to print
        logger.lines.clear();
        FailureSummary.printAndClear(null);
        assertThat(logger.lines).isEmpty();
        assertThat(FailureSummary.isEmpty()).isTrue();
    }

    @Test
    void cutsTheListAtMaxFailuresInSummary() {
        runClasses(options(true, false, 2));
        logger.lines.clear();

        FailureSummary.printAndClear(null);

        assertThat(logger.lines).containsExactly(
                "Failed tests (3):",
                "  Beta > fails",
                "  Beta > Inner > errors",
                "  ... and 1 more",
                "");
    }

    @Test
    void printsNothingWhenTheOptionIsOff() {
        runClasses(options(false, false, 50));
        logger.lines.clear();

        FailureSummary.printAndClear(null);

        assertThat(logger.lines).isEmpty();
        assertThat(FailureSummary.isEmpty()).isTrue();
    }

    @Test
    void remembersNothingWhenNoOptionIsOn() {
        runClasses(options(false, false, 50));

        assertThat(FailureSummary.isEmpty()).isTrue();
    }

    @Test
    void appendsMarkdownWithoutOverwritingTheFile() throws IOException {
        File summary = new File(dir, "summary.md");
        Files.write(summary.toPath(), "# Earlier step\n".getBytes(StandardCharsets.UTF_8));
        runClasses(options(false, true, 50));

        FailureSummary.printAndClear(summary.getPath());

        String markdown = new String(Files.readAllBytes(summary.toPath()), StandardCharsets.UTF_8);
        assertThat(markdown).startsWith("# Earlier step\n### Test results\n\n5 tests: 2 passed, 3 failed, 0 skipped\n\n<details>\n"
                + "<summary>Beta: 3 failed</summary>\n\n```text\n");
        assertThat(markdown).contains("Beta -").contains("fails").contains("Deeper");
        assertThat(markdown).endsWith("```\n\n</details>\n\nClasses without failures: `Alpha`\n\n");
        // the tree of the class without failures is not kept
        assertThat(markdown).doesNotContain("passes");

        runClasses(options(false, true, 50));
        FailureSummary.printAndClear(summary.getPath());
        assertThat(new String(Files.readAllBytes(summary.toPath()), StandardCharsets.UTF_8))
                .as("a second execution adds a section").hasSize(markdown.length() * 2 - "# Earlier step\n".length());
    }

    @Test
    void doesNotWriteWithoutTheFile() {
        runClasses(options(false, true, 50));

        FailureSummary.printAndClear(null);
        assertThat(FailureSummary.isEmpty()).isTrue();

        runClasses(options(false, true, 50));
        FailureSummary.printAndClear("");
        assertThat(FailureSummary.isEmpty()).isTrue();
        assertThat(dir.list()).isEmpty();
    }

    @Test
    void forksCloseTheReporterOneAfterTheOther() throws Exception {
        runClasses(options(true, false, 50));
        logger.lines.clear();
        int forks = 4;
        ExecutorService executor = Executors.newFixedThreadPool(forks);
        CountDownLatch start = new CountDownLatch(1);
        for (int i = 0; i < forks; i++) {
            executor.submit(() -> {
                start.await();
                FailureSummary.printAndClear(null);
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

    private static ReporterOptions options(boolean printFailureSummary, boolean githubStepSummary, int max) {
        return ReporterOptions.builder().theme(Theme.UNICODE).printFailureSummary(printFailureSummary)
                .githubStepSummary(githubStepSummary).maxFailuresInSummary(max).build();
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
