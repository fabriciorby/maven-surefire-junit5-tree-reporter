package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.MixedResultsExampleTest;
import org.apache.maven.plugin.surefire.NestedExampleTest;
import org.apache.maven.plugin.surefire.ParameterizedExampleTest;
import org.apache.maven.plugin.surefire.SiblingsExampleTest;
import org.apache.maven.plugin.surefire.VeryNestedExampleTest;
import org.apache.maven.surefire.api.report.RunMode;
import org.apache.maven.surefire.api.report.SimpleReportEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Asserts the whole printed tree against the golden files in {@code src/test/resources/snapshots}.
 * Run with {@code -Dsnapshot.update=true} to accept an intended change in the output.
 */
class TreeSnapshotTest {

    @BeforeEach
    void cleanNode() {
        Node.clearTree();
    }

    @ParameterizedTest
    @ValueSource(classes = {NestedExampleTest.class, VeryNestedExampleTest.class, ParameterizedExampleTest.class,
            SiblingsExampleTest.class})
    void printsExampleClass(Class<?> testClass) {
        Snapshot.assertMatches("emulator/" + testClass.getSimpleName(), new SurefireEmulator(testClass).run());
    }

    @ParameterizedTest
    @EnumSource(Theme.class)
    void printsWithTheme(Theme theme) {
        ReporterOptions options = ReporterOptions.builder().theme(theme).build();
        Snapshot.assertMatches("theme/" + theme.name().toLowerCase(Locale.ROOT),
                new SurefireEmulator(options, VeryNestedExampleTest.class).run());
    }

    @Test
    void printsBlankLineBetweenTests() {
        ReporterOptions options = ReporterOptions.builder().theme(Theme.UNICODE).printBlankLineBetweenTests(true).build();
        Snapshot.assertMatches("options/print-blank-line-between-tests",
                new SurefireEmulator(options, NestedExampleTest.class).run());
    }

    @Test
    void hideResultsOnSuccessKeepsTheLastConnectorOnTheLastVisibleTest() {
        // The last tests of the class pass and are hidden, so the failure before them is the last line of its class
        ReporterOptions options = ReporterOptions.builder().theme(Theme.UNICODE).hideResultsOnSuccess(true).build();
        Snapshot.assertMatches("options/hide-results-on-success-last-test-hidden",
                new SurefireEmulator(options, MixedResultsExampleTest.class)
                        .withOutcome("MixedResultsExampleTest.test2", ReportEntryType.FAILURE)
                        .withOutcome("RedInner.test", ReportEntryType.FAILURE)
                        .run());
    }

    @Test
    void printsTestCounts() {
        Snapshot.assertMatches("options/print-test-counts", mixedResults(options().printTestCounts(true)));
    }

    @Test
    void collapsesSuccessfulClasses() {
        ReporterOptions options = options().collapseSuccessfulClasses(true).build();
        Snapshot.assertMatches("options/collapse-successful-classes-failed",
                new SurefireEmulator(options, MixedResultsExampleTest.class)
                        .withOutcome("RedInner.test", ReportEntryType.FAILURE).run());
        Node.clearTree();
        Snapshot.assertMatches("options/collapse-successful-classes-green",
                new SurefireEmulator(options, VeryNestedExampleTest.class).run());
        Node.clearTree();
        Snapshot.assertMatches("options/collapse-successful-classes-skipped",
                new SurefireEmulator(options, MixedResultsExampleTest.class)
                        .withOutcome("SkippedInner.test", ReportEntryType.SKIPPED).run());
    }

    @Test
    void hidesSuccessfulNestedClasses() {
        Snapshot.assertMatches("options/hide-successful-nested-classes",
                mixedResults(options().hideSuccessfulNestedClasses(true)));
    }

    @Test
    void hidesSuccessfulNestedClassesWithHiddenResults() {
        Snapshot.assertMatches("options/hide-successful-nested-classes-and-results",
                mixedResults(options().hideSuccessfulNestedClasses(true).hideResultsOnSuccess(true)));
    }

    @Test
    void hidesTheLastNestedClassesKeepingTheLastConnector() {
        // The nested classes before and after Red Inner are green, so it is the only one left and the last
        Snapshot.assertMatches("options/hide-successful-nested-classes-last",
                new SurefireEmulator(options().hideSuccessfulNestedClasses(true).build(), MixedResultsExampleTest.class)
                        .withOutcome("RedDeep.test", ReportEntryType.FAILURE).run());
    }

    @Test
    void focusesOnFailures() {
        Snapshot.assertMatches("options/failure-focus", mixedResults(options()
                .printTestCounts(true).collapseSuccessfulClasses(true).hideSuccessfulNestedClasses(true)
                .hideResultsOnSuccess(true)));
    }

    @Test
    void keepsTheDefaultTreeWithTheFailureFocusOptionsOff() {
        Snapshot.assertMatches("options/failure-focus-off", mixedResults(options()));
    }

    private static ReporterOptions.Builder options() {
        return ReporterOptions.builder().theme(Theme.UNICODE);
    }

    private static List<String> mixedResults(ReporterOptions.Builder options) {
        return new SurefireEmulator(options.build(), MixedResultsExampleTest.class)
                .withOutcome("MixedResultsExampleTest.test2", ReportEntryType.FAILURE)
                .withOutcome("MixedResultsExampleTest.test3", ReportEntryType.SKIPPED)
                .withOutcome("RedInner.test", ReportEntryType.FAILURE)
                .withOutcome("RedDeep.test", ReportEntryType.ERROR)
                .withOutcome("SkippedInner.test", ReportEntryType.SKIPPED)
                .run();
    }

    @Test
    void printsClassWhileAnotherClassIsRunning() {
        // With parallel test classes, the classes that started and did not complete yet are in the tree too, issue #82
        ConsoleTreeReporter reporter = new ConsoleTreeReporter(new CapturingLogger(), ReporterOptions.builder().build());
        String runningClass = NestedExampleTest.class.getName();
        reporter.testSetStarting(classEntry(runningClass, runningClass));
        reporter.testSetStarting(classEntry(runningClass, runningClass + "$InnerTest"));

        Snapshot.assertMatches("emulator/VeryNestedExampleTest", new SurefireEmulator(VeryNestedExampleTest.class).run());
    }

    private static SimpleReportEntry classEntry(String topLevelClass, String testClass) {
        return new SimpleReportEntry(RunMode.NORMAL_RUN, 1L, topLevelClass, null,
                testClass, null, null, null, null, null, Collections.emptyMap());
    }
}
