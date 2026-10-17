package org.apache.maven.plugin.surefire.report;

import org.apache.maven.surefire.api.report.RunMode;
import org.apache.maven.surefire.api.report.SimpleReportEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlowTestThresholdTest {

    private static final String CLASS_NAME = "x.SlowTest";

    private final Utf8RecodingDeferredFileOutputStream stdout = new Utf8RecodingDeferredFileOutputStream("stdout");
    private final Utf8RecodingDeferredFileOutputStream stderr = new Utf8RecodingDeferredFileOutputStream("stderr");
    private final CapturingLogger logger = new CapturingLogger();

    @BeforeEach
    void cleanNode() {
        Node.clearTree();
    }

    @ParameterizedTest
    @CsvSource({"500ms,500", "500,500", "2s,2000", "1.5s,1500", "' 250 ms ',250", "2S,2000", "10MS,10"})
    void parsesThreshold(String value, long millis) {
        assertThat(ReporterOptions.parseSlowTestThreshold(value)).isEqualTo(millis);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void blankThresholdDisablesTheMarker(String value) {
        assertThat(ReporterOptions.parseSlowTestThreshold(value)).isEqualTo(ReporterOptions.SLOW_TEST_THRESHOLD_DISABLED);
        assertThat(ReporterOptions.builder().slowTestThreshold(value).build().getSlowTestThresholdMillis())
                .isEqualTo(ReporterOptions.SLOW_TEST_THRESHOLD_DISABLED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"fast", "-5", "0", "0ms", "1m", "5 seconds", "ms", "1,5s"})
    void invalidThresholdFailsWithClearMessage(String value) {
        assertThatThrownBy(() -> ReporterOptions.parseSlowTestThreshold(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("slowTestThreshold")
                .hasMessageContaining("'" + value + "'")
                .hasMessageContaining("500ms");
    }

    @ParameterizedTest
    @EnumSource(Theme.class)
    void marksTestsAtOrAboveTheThreshold(Theme theme) {
        run(theme, "500ms", ReportEntryType.SUCCESS, 499, ReportEntryType.SUCCESS, 500, ReportEntryType.FAILURE, 900);

        assertThat(logger.lines.get(1)).doesNotContain(theme.slow());
        assertThat(logger.lines.get(2)).contains("test500 " + theme.slow() + " - ");
        assertThat(logger.lines.get(3)).contains("test900 " + theme.slow() + " - ");
    }

    @Test
    void marksFailuresAndErrorsToo() {
        run(Theme.ASCII, "1ms", ReportEntryType.ERROR, 5, ReportEntryType.FAILURE, 6);

        assertThat(logger.lines.get(1)).contains("test5 [SLOW] - ");
        assertThat(logger.lines.get(2)).contains("test6 [SLOW] - ");
    }

    @Test
    void markerIsBeforeTheElapsedTime() {
        run(Theme.ASCII, "1ms", ReportEntryType.SUCCESS, 5);

        assertThat(logger.lines.get(1)).matches(".*test5 \\[SLOW\\] - [\\d.,]+( s)?$");
    }

    @Test
    void skippedTestsAreNeverMarked() {
        run(Theme.ASCII, "1ms", ReportEntryType.SKIPPED, 5000);

        assertThat(logger.lines.get(1)).doesNotContain("[SLOW]");
    }

    @Test
    void nothingIsMarkedByDefault() {
        run(Theme.ASCII, null, ReportEntryType.SUCCESS, 100000);

        assertThat(logger.lines).noneMatch(line -> line.contains("[SLOW]"));
    }

    private void run(Theme theme, String threshold, Object... typesAndElapsed) {
        ReporterOptions options = ReporterOptions.builder().theme(theme).slowTestThreshold(threshold).build();
        ConsoleTreeReporter reporter = new ConsoleTreeReporter(logger, options);
        SimpleReportEntry classEntry = new SimpleReportEntry(RunMode.NORMAL_RUN, 1L, CLASS_NAME, "Slow Sample", null, null);
        TestSetStats stats = new TestSetStats(false, true);
        for (int i = 0; i < typesAndElapsed.length; i += 2) {
            ReportEntryType type = (ReportEntryType) typesAndElapsed[i];
            int elapsed = (Integer) typesAndElapsed[i + 1];
            SimpleReportEntry entry = new SimpleReportEntry(RunMode.NORMAL_RUN, 1L, CLASS_NAME, "Slow Sample", "test",
                    "test" + elapsed, null, null, elapsed, null, Collections.emptyMap());
            WrappedReportEntry wrapped = new WrappedReportEntry(entry, type, 0L, elapsed, stdout, stderr);
            switch (type) {
                case SUCCESS: stats.testSucceeded(wrapped); break;
                case FAILURE: stats.testFailure(wrapped); break;
                case ERROR: stats.testError(wrapped); break;
                default: stats.testSkipped(wrapped);
            }
        }
        WrappedReportEntry wrappedClass = new WrappedReportEntry(classEntry, ReportEntryType.SUCCESS, 0L, 0, stdout, stderr);
        reporter.testSetStarting(wrappedClass);
        reporter.testSetCompleted(wrappedClass, stats, null);
    }
}
