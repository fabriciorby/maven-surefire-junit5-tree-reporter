package org.apache.maven.plugin.surefire.report;

import org.apache.maven.surefire.api.report.RunMode;
import org.apache.maven.surefire.api.report.SimpleReportEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Since maven-surefire-plugin 3.5.4 the source name of a JUnit5 nested test is only the top level class, and
 * from 3.6.0 on the nested path is sent as the source qualified name. This emulates the events sent by 3.6.0.
 */
class QualifiedSourceNameTest {

    private static final String OUTER = "org.apache.maven.plugin.surefire.NestedExampleTest";

    private final Utf8RecodingDeferredFileOutputStream stdout = new Utf8RecodingDeferredFileOutputStream("stdout");
    private final Utf8RecodingDeferredFileOutputStream stderr = new Utf8RecodingDeferredFileOutputStream("stderr");
    private final CapturingLogger logger = new CapturingLogger();

    @BeforeEach
    void cleanNode() {
        Node.clearTree();
    }

    @Test
    void sourceNameUsesQualifiedName() {
        assertThat(SourceName.of(classEntry("$InnerTest", "Inner Test"))).isEqualTo(OUTER + "$InnerTest");
    }

    @Test
    void sourceNameFallsBackToSourceNameWithoutQualifiedName() {
        SimpleReportEntry entry = new SimpleReportEntry(RunMode.NORMAL_RUN, 1L, OUTER + "$InnerTest", "Inner Test", null, null);
        assertThat(SourceName.of(entry)).isEqualTo(OUTER + "$InnerTest");
    }

    @Test
    void sourceNameIgnoresDisplayNameFallback() {
        // WrappedReportEntry returns the display name when the original entry has no qualified name
        WrappedReportEntry entry = wrap(new SimpleReportEntry(RunMode.NORMAL_RUN, 1L, OUTER, "Nested Sample", null, null));
        assertThat(entry.getSourceQualifiedName()).isEqualTo("Nested Sample");
        assertThat(SourceName.of(entry)).isEqualTo(OUTER);
    }

    @Test
    void printsNestedTreeWithQualifiedNames() {
        ConsoleTreeReporter reporter = new ConsoleTreeReporter(logger, ReporterOptions.builder().build());

        WrappedReportEntry outer = wrap(classEntry("", "Nested Sample"));
        WrappedReportEntry first = wrap(classEntry("$FirstInnerTest", "First Inner Test"));
        WrappedReportEntry inner = wrap(classEntry("$InnerTest", "Inner Test"));
        WrappedReportEntry innerInner = wrap(classEntry("$InnerTest$InnerInnerTest", "Inner Inner Test"));
        WrappedReportEntry innerInnerInner = wrap(classEntry("$InnerTest$InnerInnerTest$InnerInnerInnerTest", "Inner Inner Inner Test"));

        // Same order of events maven-surefire-plugin 3.6.0 sends for NestedExampleTest
        reporter.testSetStarting(outer);
        reporter.testSetStarting(first);
        reporter.testSetCompleted(first, stats(
                testEntry("", "Should pass"),
                testEntry("", "Should pass2"),
                testEntry("$FirstInnerTest", "FirstInnerTest should show up")), null);
        reporter.testSetStarting(inner);
        reporter.testSetStarting(innerInner);
        reporter.testSetStarting(innerInnerInner);
        reporter.testSetCompleted(innerInnerInner, stats(
                testEntry("$InnerTest", "Inner test should pass"),
                testEntry("$InnerTest$InnerInnerTest", "Inner Inner Test should pass"),
                testEntry("$InnerTest$InnerInnerTest$InnerInnerInnerTest", "Inner Inner Inner Test should pass")), null);
        reporter.testSetCompleted(innerInner, stats(), null);
        reporter.testSetCompleted(inner, stats(), null);

        assertThat(logger.lines).as("nothing is printed before the top level class completes").isEmpty();

        reporter.testSetCompleted(outer, stats(), null);

        assertThat(logger.lines).hasSize(11);
        assertThat(logger.lines.get(0)).contains("Nested Sample").doesNotContain("Inner");
        assertThat(logger.lines.get(1)).contains("Should pass");
        assertThat(logger.lines.get(2)).contains("Should pass2");
        assertThat(logger.lines.get(3)).contains("First Inner Test");
        assertThat(logger.lines.get(4)).contains("FirstInnerTest should show up");
        assertThat(logger.lines.get(5)).contains("Inner Test").doesNotContain("Inner Inner");
        assertThat(logger.lines.get(6)).contains("Inner test should pass");
        assertThat(logger.lines.get(7)).contains("Inner Inner Test").doesNotContain("Inner Inner Inner");
        assertThat(logger.lines.get(8)).contains("Inner Inner Test should pass");
        assertThat(logger.lines.get(9)).contains("Inner Inner Inner Test");
        assertThat(logger.lines.get(10)).contains("Inner Inner Inner Test should pass");
        assertThat(Node.getRoot().hasBranches()).isFalse();
        Snapshot.assertMatches("reporter/nested-tree-with-qualified-names", logger.lines);
    }

    @Test
    void printsNestedTreeWhenOnlyTheTopLevelClassHasTestSet() {
        ConsoleTreeReporter reporter = new ConsoleTreeReporter(logger, ReporterOptions.builder().build());
        WrappedReportEntry outer = wrap(classEntry("", "Nested Sample"));

        // maven-surefire-plugin 3.6.1 only starts and completes the test set of the top level class,
        // the tests of the nested classes come with it
        reporter.testSetStarting(outer);
        reporter.testSetCompleted(outer, stats(
                testEntry("", "Nested Sample", "Should pass"),
                testEntry("$InnerTest", "Inner Test", "Inner test should pass"),
                testEntry("$InnerTest$InnerInnerTest$InnerInnerInnerTest", "Inner Inner Inner Test", "Deepest should pass")), null);

        assertThat(logger.lines).hasSize(7);
        assertThat(logger.lines.get(0)).contains("Nested Sample").doesNotContain("Inner");
        assertThat(logger.lines.get(1)).contains("Should pass");
        assertThat(logger.lines.get(2)).contains("Inner Test").doesNotContain("Inner Inner");
        assertThat(logger.lines.get(3)).contains("Inner test should pass");
        // a nested class without tests of its own is named after its path
        assertThat(logger.lines.get(4)).contains("InnerInnerTest").doesNotContain("Inner Inner Inner");
        assertThat(logger.lines.get(5)).contains("Inner Inner Inner Test");
        assertThat(logger.lines.get(6)).contains("Deepest should pass");
        assertThat(Node.getRoot().hasBranches()).isFalse();
    }

    @Test
    void printsSimpleNameOfSkippedClassThatNeverStarted() {
        ConsoleTreeReporter reporter = new ConsoleTreeReporter(logger, ReporterOptions.builder().build());
        WrappedReportEntry outer = wrap(classEntry("", "Disabled Sample"));
        // Events maven-surefire-plugin 3.6.0 sends for the @Nested class of a @Disabled class
        WrappedReportEntry inner = new WrappedReportEntry(new SimpleReportEntry(RunMode.NORMAL_RUN, 1L, OUTER,
                OUTER + "$InnerTest", OUTER + "$InnerTest", null, null, null, null, "whole class disabled",
                Collections.emptyMap()), ReportEntryType.SKIPPED, 0L, 0, stdout, stderr);
        TestSetStats stats = new TestSetStats(false, true);
        stats.testSkipped(inner);

        reporter.testSetStarting(outer);
        reporter.testSetCompleted(outer, stats, null);

        assertThat(logger.lines).hasSize(2);
        assertThat(logger.lines.get(1)).contains("InnerTest (whole class disabled)").doesNotContain("$");
    }

    @Test
    void printsClassNameOfErroredTestWithoutName() {
        ConsoleTreeReporter reporter = new ConsoleTreeReporter(logger, ReporterOptions.builder().build());
        WrappedReportEntry bad = wrap(new SimpleReportEntry(RunMode.NORMAL_RUN, 1L, "x.BadTest", null, null, null));
        // JUnit4 InvalidTestClassError run by the vintage engine: the error has no test name
        WrappedReportEntry error = new WrappedReportEntry(new SimpleReportEntry(RunMode.NORMAL_RUN, 1L, "x.BadTest",
                null, null, null), ReportEntryType.ERROR, 0L, 1, stdout, stderr);
        TestSetStats stats = new TestSetStats(false, true);
        stats.testError(error);

        reporter.testSetStarting(bad);
        reporter.testSetCompleted(bad, stats, null);

        assertThat(logger.lines).hasSize(2);
        assertThat(logger.lines.get(1)).contains("BadTest").doesNotContain("null");
    }

    private static SimpleReportEntry classEntry(String nestedPath, String displayName) {
        return new SimpleReportEntry(RunMode.NORMAL_RUN, 1L, OUTER, displayName, OUTER + nestedPath,
                null, null, null, null, null, Collections.emptyMap());
    }

    private WrappedReportEntry testEntry(String nestedPath, String displayName) {
        return wrap(new SimpleReportEntry(RunMode.NORMAL_RUN, 1L, OUTER, null, OUTER + nestedPath,
                "test", displayName, null, 1, null, Collections.emptyMap()));
    }

    private WrappedReportEntry testEntry(String nestedPath, String classDisplayName, String displayName) {
        return wrap(new SimpleReportEntry(RunMode.NORMAL_RUN, 1L, OUTER, classDisplayName, OUTER + nestedPath,
                "test", displayName, null, 1, null, Collections.emptyMap()));
    }

    private WrappedReportEntry wrap(SimpleReportEntry entry) {
        return new WrappedReportEntry(entry, ReportEntryType.SUCCESS, 0L, 1, stdout, stderr);
    }

    private static TestSetStats stats(WrappedReportEntry... entries) {
        TestSetStats testSetStats = new TestSetStats(false, true);
        for (WrappedReportEntry entry : entries) {
            testSetStats.testSucceeded(entry);
        }
        return testSetStats;
    }
}
