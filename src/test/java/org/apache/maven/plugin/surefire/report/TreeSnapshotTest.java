package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.NestedExampleTest;
import org.apache.maven.plugin.surefire.ParameterizedExampleTest;
import org.apache.maven.plugin.surefire.VeryNestedExampleTest;
import org.apache.maven.surefire.api.report.RunMode;
import org.apache.maven.surefire.api.report.SimpleReportEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
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
    @ValueSource(classes = {NestedExampleTest.class, VeryNestedExampleTest.class, ParameterizedExampleTest.class})
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
