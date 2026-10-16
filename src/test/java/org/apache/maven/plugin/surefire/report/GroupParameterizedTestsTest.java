package org.apache.maven.plugin.surefire.report;

import org.apache.maven.surefire.api.report.RunMode;
import org.apache.maven.surefire.api.report.SimpleReportEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The names of the entries are the ones maven-surefire-plugin sends for the invocations of a parameterized test: the
 * name of the method with its signature and the index of the invocation.
 */
class GroupParameterizedTestsTest {

    private static final String SOURCE = "x.SampleTest";

    private final Utf8RecodingDeferredFileOutputStream stdout = new Utf8RecodingDeferredFileOutputStream("stdout");
    private final Utf8RecodingDeferredFileOutputStream stderr = new Utf8RecodingDeferredFileOutputStream("stderr");
    private final CapturingLogger logger = new CapturingLogger();

    @BeforeEach
    void cleanNode() {
        Node.clearTree();
    }

    @Test
    void groupsTheInvocationsOfTheSameMethod() {
        run(ReporterOptions.builder().theme(Theme.UNICODE).build(),
                test("plain", null, ReportEntryType.SUCCESS, "First"),
                test("add(int)[1]", "[1] 1", ReportEntryType.SUCCESS, null),
                test("add(int)[2]", "[2] 2", ReportEntryType.FAILURE, null),
                test("add(String)[1]", "[1] a", ReportEntryType.SUCCESS, null),
                test("plain2", null, ReportEntryType.SUCCESS, "Second"),
                test("repeat()[1]", "repeat() repetition 1 of 2", ReportEntryType.SUCCESS, null),
                test("repeat()[2]", "repeat() repetition 2 of 2", ReportEntryType.SKIPPED, null));

        assertThat(lines()).containsExactly(
                "── Sample",
                "   ├─ ✔ First",
                "   ├─ ┬─ add(int)",
                "   │  ├─ ✔ [1] 1",
                "   │  └─ ✘ [2] 2",
                "   ├─ ┬─ add(String)",
                "   │  └─ ✔ [1] a",
                "   ├─ ✔ Second",
                "   └─ ┬─ repeat()",
                "      ├─ ✔ repetition 1 of 2",
                "      └─ ↷ repetition 2 of 2");
    }

    @Test
    void connectorsFollowTheVisibleItemsWhenSuccessIsHidden() {
        run(ReporterOptions.builder().theme(Theme.UNICODE)
                        .hideResultsOnSuccess(true).build(),
                test("add(int)[1]", "[1] 1", ReportEntryType.SUCCESS, null),
                test("add(int)[2]", "[2] 2", ReportEntryType.FAILURE, null),
                test("allGreen(int)[1]", "[1] 1", ReportEntryType.SUCCESS, null),
                test("allGreen(int)[2]", "[2] 2", ReportEntryType.SUCCESS, null),
                test("plain", null, ReportEntryType.SUCCESS, "Plain"));

        assertThat(lines()).containsExactly(
                "── Sample",
                "   └─ ┬─ add(int)",
                "      └─ ✘ [2] 2");
    }

    @Test
    void doesNotGroupNamesThatOnlyLookLikeAnInvocation() {
        run(ReporterOptions.builder().theme(Theme.UNICODE).build(),
                test("looksLikeOne", "foo[1]", ReportEntryType.SUCCESS, null),
                test("looksLikeAnother", "[1] looks like an index", ReportEntryType.SUCCESS, null));

        assertThat(lines()).containsExactly(
                "── Sample",
                "   ├─ ✔ foo[1]",
                "   └─ ✔ [1] looks like an index");
    }

    @Test
    void labelsTheInvocations() {
        assertThat(label("add(int, int)[2]", "add(int, int)[2] 3")).isEqualTo("[2] 3");
        assertThat(label("add(int, int)[2]", "add(int, int) a custom name")).isEqualTo("a custom name");
        assertThat(label("add(int, int)[2]", "Display name[2] 3")).isEqualTo("[2] 3");
        assertThat(label("method()[1][2]", "box method() inside")).isEqualTo("box method() inside");
        assertThat(label("method()[1]", "method()")).isEqualTo("method()");
        assertThat(label("plain", "A display name")).isEqualTo("A display name");
    }

    private String label(String name, String nameText) {
        WrappedReportEntry entry = test(name, nameText, ReportEntryType.SUCCESS, null);
        return ParameterizedInvocations.labelOf(entry, entry.getReportName());
    }

    private void run(ReporterOptions options, WrappedReportEntry... tests) {
        ConsoleTreeReporter reporter = new ConsoleTreeReporter(logger, options);
        WrappedReportEntry clazz = new WrappedReportEntry(
                new SimpleReportEntry(RunMode.NORMAL_RUN, 1L, SOURCE, "Sample", null, null),
                ReportEntryType.SUCCESS, 0L, 1, stdout, stderr);
        TestSetStats stats = new TestSetStats(false, true);
        for (WrappedReportEntry test : tests) {
            switch (test.getReportEntryType()) {
                case FAILURE:
                    stats.testFailure(test);
                    break;
                case SKIPPED:
                    stats.testSkipped(test);
                    break;
                default:
                    stats.testSucceeded(test);
            }
        }
        reporter.testSetStarting(clazz);
        reporter.testSetCompleted(clazz, stats, null);
    }

    /** A test as surefire reports it: the name of the method, and the display name when there is one */
    private WrappedReportEntry test(String name, String nameText, ReportEntryType type, String displayName) {
        return new WrappedReportEntry(
                new SimpleReportEntry(RunMode.NORMAL_RUN, 1L, SOURCE, "Sample", name,
                        nameText != null ? nameText : displayName),
                type, 0L, 1, stdout, stderr);
    }

    /** The printed lines without colors and elapsed times */
    private List<String> lines() {
        return logger.lines.stream()
                .map(line -> line.replaceAll("\u001B\\[[;\\d]*m", "").replaceFirst(" - [^-]*$", ""))
                .collect(Collectors.toList());
    }
}
