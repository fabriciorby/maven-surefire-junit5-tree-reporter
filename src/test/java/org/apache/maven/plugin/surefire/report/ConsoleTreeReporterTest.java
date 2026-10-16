package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.VeryNestedExampleTest;
import org.apache.maven.plugin.surefire.log.PluginConsoleLogger;
import org.apache.maven.surefire.api.report.RunMode;
import org.apache.maven.surefire.api.report.SafeThrowable;
import org.apache.maven.surefire.api.report.StackTraceWriter;
import org.apache.maven.surefire.api.report.SimpleReportEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class ConsoleTreeReporterTest {

    //Test for org.apache.maven.plugin.surefire.NestedExampleTest
    Utf8RecodingDeferredFileOutputStream stdout = new Utf8RecodingDeferredFileOutputStream("stdout");
    Utf8RecodingDeferredFileOutputStream stderr = new Utf8RecodingDeferredFileOutputStream("stderr");

    static Logger logger = LoggerFactory.getLogger(ConsoleTreeReporterTest.class);

    @BeforeEach
    void cleanNode() {
        Node.clearTree();
    }

    @Test
    void testEmulator() {
        SurefireEmulator surefireEmulator = new SurefireEmulator(VeryNestedExampleTest.class);
        List<String> logs = surefireEmulator.run();
        Snapshot.assertMatches("emulator/VeryNestedExampleTest", logs);
    }

    @Test
    void emptyStdoutAndStderrAreNotPrintedOnSuccess() {
        // Issue #48: printStdoutOnSuccess and printStderrOnSuccess printed the headers even with nothing to show
        ReporterOptions printOutputOnSuccess = ReporterOptions.builder().theme(Theme.UNICODE)
                .printStdoutOnSuccess(true).printStderrOnSuccess(true).build();
        List<String> withOutputOptions = new SurefireEmulator(printOutputOnSuccess, VeryNestedExampleTest.class).run();
        Node.clearTree();
        List<String> withoutOutputOptions = new SurefireEmulator(VeryNestedExampleTest.class).run();
        assertThat(withOutputOptions).isEqualTo(withoutOutputOptions)
                .noneMatch(line -> line.contains("Standard out") || line.contains("Standard error"));
    }

    @Test
    void testSetStarting() {
        //Runs 4 times for this class
        SimpleReportEntry simpleReportEntry1 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest", "Nested Sample", null, null);
        SimpleReportEntry simpleReportEntry2 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$InnerTest", "Nested Sample Inner Test", null, null);
        SimpleReportEntry simpleReportEntry3 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$InnerTest$InnerInnerTest", "Nested Sample Inner Test Inner Inner Test", null, null);
        SimpleReportEntry simpleReportEntry4 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$InnerTest$InnerInnerTest$InnerInnerInnerTest", "Nested Sample Inner Test Inner Inner Test Inner Inner Inner Test", null, null);

        ConsoleTreeReporter consoleTreeReporter = new ConsoleTreeReporter(new PluginConsoleLogger(logger), ReporterOptions.builder().build());
        consoleTreeReporter.testSetStarting(simpleReportEntry1);
        consoleTreeReporter.testSetStarting(simpleReportEntry2);
        consoleTreeReporter.testSetStarting(simpleReportEntry3);
        consoleTreeReporter.testSetStarting(simpleReportEntry4);
    }

    @Test
    void testSetCompleted() {

        //TestStarting parameters
        SimpleReportEntry simpleReportEntry1 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest", "Nested Sample", null, null);
        SimpleReportEntry simpleReportEntry2 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$InnerTest", "Nested Sample Inner Test", null, null);
        SimpleReportEntry simpleReportEntry3 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$InnerTest$InnerInnerTest", "Nested Sample Inner Test Inner Inner Test", null, null);
        SimpleReportEntry simpleReportEntry4 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$InnerTest$InnerInnerTest$InnerInnerInnerTest", "Nested Sample Inner Test Inner Inner Test Inner Inner Inner Test", null, null);
        SimpleReportEntry simpleReportEntry5 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$FirstInnerTest", "Nested Sample Inner Test Inner Inner Test Inner Inner Inner Test First Inner Test", null, null);

        //Runs 1 time with all the information
        //Gets all SingleReportEntries with test names and add on a list of WrapperReportEntries to create a TestSetStats
        SimpleReportEntry firstTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest", "Nested Sample", "test", "Should pass");
        SimpleReportEntry secondTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest", "Nested Sample", "test2", "Should pass2");
        SimpleReportEntry thirdTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$InnerTest", "Nested Sample Inner Test", "test", "Inner test should pass");
        SimpleReportEntry fourthTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$InnerTest$InnerInnerTest", "Nested Sample Inner Test Inner Inner Test", "test", "Inner Inner Test should pass");
        SimpleReportEntry fifthTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$InnerTest$InnerInnerTest$InnerInnerInnerTest", "Nested Sample Inner Test Inner Inner Test Inner Inner Inner Test", "test", "Inner Inner Inner Test should pass");
        SimpleReportEntry sixthTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$FirstInnerTest", "Nested Sample First Inner Test", "test", "FirstInnerTest should show up");

        WrappedReportEntry wrappedReportEntry1 = new WrappedReportEntry(firstTest, ReportEntryType.SUCCESS, 0L, 1, stdout, stderr);
        WrappedReportEntry wrappedReportEntry2 = new WrappedReportEntry(secondTest, ReportEntryType.SUCCESS, 0L, 1, stdout, stderr);
        WrappedReportEntry wrappedReportEntry3 = new WrappedReportEntry(thirdTest, ReportEntryType.SUCCESS, 0L, 1, stdout, stderr);
        WrappedReportEntry wrappedReportEntry4 = new WrappedReportEntry(fourthTest, ReportEntryType.SUCCESS, 0L, 1, stdout, stderr);
        WrappedReportEntry wrappedReportEntry5 = new WrappedReportEntry(fifthTest, ReportEntryType.SUCCESS, 0L, 1, stdout, stderr);
        WrappedReportEntry wrappedReportEntry6 = new WrappedReportEntry(sixthTest, ReportEntryType.SUCCESS, 0L, 1, stdout, stderr);

        TestSetStats testSetStats = new TestSetStats(false, true);
        testSetStats.testSucceeded(wrappedReportEntry1);
        testSetStats.testSucceeded(wrappedReportEntry2);
        testSetStats.testSucceeded(wrappedReportEntry3);
        testSetStats.testSucceeded(wrappedReportEntry4);
        testSetStats.testSucceeded(wrappedReportEntry5);
        testSetStats.testSucceeded(wrappedReportEntry6);

        TestSetStats testSetStatsForClass = new TestSetStats(false, true);

        ConsoleTreeReporter consoleTreeReporter = new ConsoleTreeReporter(new PluginConsoleLogger(logger), ReporterOptions.builder().build());
        //This prepares the nested tests by filling the classNames
        consoleTreeReporter.testSetStarting(simpleReportEntry1);
        consoleTreeReporter.testSetStarting(simpleReportEntry2);
        consoleTreeReporter.testSetStarting(simpleReportEntry3);
        consoleTreeReporter.testSetStarting(simpleReportEntry4);
        consoleTreeReporter.testSetStarting(simpleReportEntry5);
        //As soon as it finished to add tests for all the nested classes that were prepared, then it prints
        consoleTreeReporter.testSetCompleted(wrappedReportEntry5, testSetStats, null);
        consoleTreeReporter.testSetCompleted(wrappedReportEntry4, testSetStatsForClass, null);
        consoleTreeReporter.testSetCompleted(wrappedReportEntry3, testSetStatsForClass, null);
        consoleTreeReporter.testSetCompleted(wrappedReportEntry2, testSetStatsForClass, null);
        consoleTreeReporter.testSetCompleted(wrappedReportEntry6, testSetStatsForClass, null);

        //TODO see how to unit test this
    }

    @Test
    void testHideSuccess() {
        //TestStarting parameters
        SimpleReportEntry simpleReportEntry1 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest", "Nested Sample", null, null);
        SimpleReportEntry simpleReportEntry2 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest", "Nested Sample Inner Test", null, null);
        SimpleReportEntry simpleReportEntry3 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest$InnerInnerTest", "Nested Sample Inner Test Inner Inner Test", null, null);
        SimpleReportEntry simpleReportEntry4 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest$InnerInnerTest$InnerInnerInnerTest", "Nested Sample Inner Test Inner Inner Test Inner Inner Inner Test", null, null);
        SimpleReportEntry simpleReportEntry5 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$FirstInnerTest", "Nested Sample First Inner Test", null, null);

        SimpleReportEntry firstTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest", "Nested Sample", "test", "Should not be displayed");
        WrappedReportEntry wrappedReportEntry1 = new WrappedReportEntry(firstTest, ReportEntryType.SUCCESS, 0L, 1, stdout, stderr);

        SimpleReportEntry secondTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest", "Nested Sample", "test2", "Should not be displayed");
        WrappedReportEntry wrappedReportEntry2 = new WrappedReportEntry(secondTest, ReportEntryType.SUCCESS, 0L, 1, stdout, stderr);

        SimpleReportEntry thirdTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest", "Nested Sample Inner Test", "test", "Inner failure test should be displayed");
        WrappedReportEntry wrappedReportEntry3 = new WrappedReportEntry(thirdTest, ReportEntryType.FAILURE, 0L, 1, stdout, stderr);

        SimpleReportEntry fourthTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest$InnerInnerTest", "Nested Sample Inner Test Inner Inner Test", "test", "Inner Inner error test should be displayed");
        WrappedReportEntry wrappedReportEntry4 = new WrappedReportEntry(fourthTest, ReportEntryType.ERROR, 0L, 1, stdout, stderr);

        SimpleReportEntry fifthTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest$InnerInnerTest$InnerInnerInnerTest", "Nested Sample Inner Test Inner Inner Test Inner Inner Inner Test", "test", "Inner Inner Inner skipped test should be displayed");
        WrappedReportEntry wrappedReportEntry5 = new WrappedReportEntry(fifthTest, ReportEntryType.SKIPPED, 0L, 1, stdout, stderr);

        SimpleReportEntry sixthTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$FirstInnerTest", "Nested Sample First Inner Test", "test", "FirstInnerTest should not be displayed");
        WrappedReportEntry wrappedReportEntry6 = new WrappedReportEntry(sixthTest, ReportEntryType.SUCCESS, 0L, 1, stdout, stderr);

        TestSetStats testSetStats = new TestSetStats(false, true);
        testSetStats.testSucceeded(wrappedReportEntry1);
        testSetStats.testSucceeded(wrappedReportEntry2);
        testSetStats.testFailure(wrappedReportEntry3);
        testSetStats.testError(wrappedReportEntry4);
        testSetStats.testSkipped(wrappedReportEntry5);
        testSetStats.testSucceeded(wrappedReportEntry6);

        TestSetStats testSetStatsForClass = new TestSetStats(false, true);

        ReporterOptions optionsHidingSuccess = ReporterOptions.builder().hideResultsOnSuccess(true).build();
        ConsoleTreeReporter consoleTreeReporter = new ConsoleTreeReporter(new PluginConsoleLogger(logger), optionsHidingSuccess);
        consoleTreeReporter.testSetStarting(simpleReportEntry1);
        consoleTreeReporter.testSetStarting(simpleReportEntry2);
        consoleTreeReporter.testSetStarting(simpleReportEntry3);
        consoleTreeReporter.testSetStarting(simpleReportEntry4);
        consoleTreeReporter.testSetStarting(simpleReportEntry5);
        consoleTreeReporter.testSetCompleted(wrappedReportEntry5, testSetStats, null);
        consoleTreeReporter.testSetCompleted(wrappedReportEntry4, testSetStatsForClass, null);
        consoleTreeReporter.testSetCompleted(wrappedReportEntry3, testSetStatsForClass, null);
        consoleTreeReporter.testSetCompleted(wrappedReportEntry2, testSetStatsForClass, null);
        consoleTreeReporter.testSetCompleted(wrappedReportEntry6, testSetStatsForClass, null);

    }

    /**
     * Reproduces bug: NoSuchElementException in TestReportHandler.print() when TestSetStats
     * contains entries with source names that were never registered via testSetStarting().
     *
     * This occurs with @Disabled tests + @TestFactory dynamic tests when Surefire reports
     * entries for classes/tests that don't have corresponding nodes in the tree.
     *
     * @see <a href="https://github.com/david-waltermire/maven-surefire-junit5-tree-reporter/issues/XX">Issue #XX</a>
     */
    @Test
    void testSetCompletedWithUnregisteredSourceName_shouldNotThrow() {
        // Register only the main test class - simulate a class with @TestFactory
        SimpleReportEntry mainClassEntry = new SimpleReportEntry(
                RunMode.NORMAL_RUN, 123L,
                "com.example.ExampleTest", "Example Test",
                null, null);

        // Test entry for the registered class
        SimpleReportEntry registeredTest = new SimpleReportEntry(
                RunMode.NORMAL_RUN, 123L,
                "com.example.ExampleTest", "Example Test",
                "dynamicTestsWithCollection", "Dynamic Tests");
        WrappedReportEntry wrappedRegisteredTest = new WrappedReportEntry(
                registeredTest, ReportEntryType.SUCCESS, 0L, 1, stdout, stderr);

        // Test entry for an UNREGISTERED source name - simulates dynamic container or disabled test
        // that got reported with a different source name than what was registered
        SimpleReportEntry unregisteredTest = new SimpleReportEntry(
                RunMode.NORMAL_RUN, 123L,
                "com.example.UnregisteredClass", "Unregistered Class",
                "someTest", "Some Test");
        WrappedReportEntry wrappedUnregisteredTest = new WrappedReportEntry(
                unregisteredTest, ReportEntryType.SKIPPED, 0L, 1, stdout, stderr);

        // TestSetStats contains both registered and unregistered entries
        TestSetStats testSetStats = new TestSetStats(false, true);
        testSetStats.testSucceeded(wrappedRegisteredTest);
        testSetStats.testSkipped(wrappedUnregisteredTest);

        ConsoleTreeReporter consoleTreeReporter = new ConsoleTreeReporter(
                new PluginConsoleLogger(logger), ReporterOptions.builder().build());

        // Only register the main class
        consoleTreeReporter.testSetStarting(mainClassEntry);

        // This should NOT throw NoSuchElementException even though testSetStats
        // contains entries with unregistered source names
        assertThatNoException().isThrownBy(() ->
                consoleTreeReporter.testSetCompleted(wrappedRegisteredTest, testSetStats, null));
    }

    @Test
    void testSetCompletedWithUnregisteredSourceName_shouldKeepEntry() {
        // A @Disabled class never starts its @Nested classes, yet their skipped entry is still reported
        SimpleReportEntry outer = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "DisabledExampleTest", null, null, null);
        WrappedReportEntry wrappedOuter = new WrappedReportEntry(outer, ReportEntryType.SKIPPED, 0L, 0, stdout, stderr);
        SimpleReportEntry inner = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "DisabledExampleTest$InnerTest", null, null, null, "disabled");
        WrappedReportEntry wrappedInner = new WrappedReportEntry(inner, ReportEntryType.SKIPPED, 0L, 0, stdout, stderr);

        TestSetStats testSetStats = new TestSetStats(false, true);
        testSetStats.testSkipped(wrappedInner);

        Node outerNode = Node.getRoot().addChildren("DisabledExampleTest");
        new TestReportHandler(wrappedOuter, testSetStats).print(new TreePrinter(new PluginConsoleLogger(logger), ReporterOptions.builder().build()));

        assertThat(outerNode.wrappedReportEntries).containsExactly(wrappedInner);
    }


    @Test
    void abortedTestShowsTheThrowableMessage() {
        // Issue #47: a failed assumption is reported as skipped with no message, the reason is only on the throwable
        StackTraceWriter abortedWriter = new StackTraceWriter() {
            @Override
            public String writeTraceToString() {
                return "";
            }

            @Override
            public String smartTrimmedStackTrace() {
                return "";
            }

            @Override
            public String writeTrimmedTraceToString() {
                return "";
            }

            @Override
            public SafeThrowable getThrowable() {
                return new SafeThrowable(new RuntimeException("Assumption failed: not on this OS"));
            }
        };
        SimpleReportEntry aborted = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "AbortedExampleTest", "Aborted Example", "test", "aborted test", (String) null, abortedWriter, 1, null, java.util.Collections.emptyMap());
        SimpleReportEntry disabled = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "AbortedExampleTest", "Aborted Example", "test2", "disabled test", "because reasons");
        SimpleReportEntry plain = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "AbortedExampleTest", "Aborted Example", "test3", "skipped test");
        WrappedReportEntry wrappedAborted = new WrappedReportEntry(aborted, ReportEntryType.SKIPPED, 0L, 1, stdout, stderr);
        WrappedReportEntry wrappedDisabled = new WrappedReportEntry(disabled, ReportEntryType.SKIPPED, 0L, 1, stdout, stderr);
        WrappedReportEntry wrappedPlain = new WrappedReportEntry(plain, ReportEntryType.SKIPPED, 0L, 1, stdout, stderr);

        TestSetStats testSetStats = new TestSetStats(false, true);
        testSetStats.testSkipped(wrappedAborted);
        testSetStats.testSkipped(wrappedDisabled);
        testSetStats.testSkipped(wrappedPlain);

        CapturingLogger capturingLogger = new CapturingLogger();
        ReporterOptions options = ReporterOptions.builder().theme(Theme.ASCII).build();
        ConsoleTreeReporter consoleTreeReporter = new ConsoleTreeReporter(capturingLogger, options);
        consoleTreeReporter.testSetStarting(new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "AbortedExampleTest", "Aborted Example", null, null));
        consoleTreeReporter.testSetCompleted(wrappedAborted, testSetStats, null);

        assertThat(capturingLogger.lines)
                .anyMatch(line -> line.contains("aborted test (Assumption failed: not on this OS)"))
                .anyMatch(line -> line.contains("disabled test (because reasons)"))
                .anyMatch(line -> line.endsWith("skipped test") || line.contains("skipped test - "));
    }

}
