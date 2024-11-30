package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.NestedExampleTest;
import org.apache.maven.plugin.surefire.log.PluginConsoleLogger;
import org.apache.maven.surefire.api.report.RunMode;
import org.apache.maven.surefire.api.report.SimpleReportEntry;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsoleTreeReporterTest {

    //Test for org.apache.maven.plugin.surefire.NestedExampleTest
    Utf8RecodingDeferredFileOutputStream stdout = new Utf8RecodingDeferredFileOutputStream("stdout");
    Utf8RecodingDeferredFileOutputStream stderr = new Utf8RecodingDeferredFileOutputStream("stderr");

    static DefaultPlexusContainer container;
    static Logger logger;

    @BeforeAll
    static void setupContainer() throws PlexusContainerException {
        container = new DefaultPlexusContainer();
        logger = container.getLogger();
    }

    @Test
    void testEmulator() {
        // Now we can check the output of any Test class using this
        // TODO: Add some proxy before the logger or something so we can assert the output
        // TODO: Add some objects with relevant information inside the emulator
        new SurefireEmulator().run(NestedExampleTest.class);
    }

    @Test
    void testSetStarting() {
        //Runs 4 times for this class
        SimpleReportEntry simpleReportEntry1 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest", "Nested Sample", null, null);
        SimpleReportEntry simpleReportEntry2 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$InnerTest", "Inner Test", null, null);
        SimpleReportEntry simpleReportEntry3 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$InnerTest$InnerInnerTest", "Inner Inner Test", null, null);
        SimpleReportEntry simpleReportEntry4 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$InnerTest$InnerInnerTest$InnerInnerInnerTest", "Inner Inner Inner Test", null, null);

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
        SimpleReportEntry simpleReportEntry2 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$InnerTest", "Inner Test", null, null);
        SimpleReportEntry simpleReportEntry3 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$InnerTest$InnerInnerTest", "Inner Inner Test", null, null);
        SimpleReportEntry simpleReportEntry4 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$InnerTest$InnerInnerTest$InnerInnerInnerTest", "Inner Inner Inner Test", null, null);

        //Runs 1 time with all the information
        //Gets all SingleReportEntries with test names and add on a list of WrapperReportEntries to create a TestSetStats
        SimpleReportEntry firstTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest", "Nested Sample", "test", "Should pass");
        SimpleReportEntry secondTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest", "Nested Sample", "test2", "Should pass2");
        SimpleReportEntry thirdTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$InnerTest", "Inner Test", "test", "Inner test should pass");
        SimpleReportEntry fourthTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$InnerTest$InnerInnerTest", "Inner Inner Test", "test", "Inner Inner Test should pass");
        SimpleReportEntry fifthTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$InnerTest$InnerInnerTest$InnerInnerInnerTest", "Inner Inner Inner Test", "test", "Inner Inner Inner Test should pass");
        SimpleReportEntry sixthTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "org.apache.maven.plugin.surefire.NestedExampleTest$FirstInnerTest", "First Inner Test", "test", "FirstInnerTest should show up");

        WrappedReportEntry wrappedReportEntry1 = new WrappedReportEntry(firstTest, ReportEntryType.SUCCESS, 1, stdout, stderr);
        WrappedReportEntry wrappedReportEntry2 = new WrappedReportEntry(secondTest, ReportEntryType.SUCCESS, 1, stdout, stderr);
        WrappedReportEntry wrappedReportEntry3 = new WrappedReportEntry(thirdTest, ReportEntryType.SUCCESS, 1, stdout, stderr);
        WrappedReportEntry wrappedReportEntry4 = new WrappedReportEntry(fourthTest, ReportEntryType.SUCCESS, 1, stdout, stderr);
        WrappedReportEntry wrappedReportEntry5 = new WrappedReportEntry(fifthTest, ReportEntryType.SUCCESS, 1, stdout, stderr);
        WrappedReportEntry wrappedReportEntry6 = new WrappedReportEntry(sixthTest, ReportEntryType.SUCCESS, 1, stdout, stderr);

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
        //As soon as it finished to add tests for all the nested classes that were prepared, then it prints
        consoleTreeReporter.testSetCompleted(wrappedReportEntry5, testSetStats, null);
        consoleTreeReporter.testSetCompleted(wrappedReportEntry4, testSetStatsForClass, null);
        consoleTreeReporter.testSetCompleted(wrappedReportEntry3, testSetStatsForClass, null);
        consoleTreeReporter.testSetCompleted(wrappedReportEntry2, testSetStatsForClass, null);

        //TODO see how to unit test this
    }

    @Test
    void testHideSuccess() {
        //TestStarting parameters
        SimpleReportEntry simpleReportEntry1 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest", "Nested Sample", null, null);
        SimpleReportEntry simpleReportEntry2 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest", "Inner Test", null, null);
        SimpleReportEntry simpleReportEntry3 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest$InnerInnerTest", "Inner Inner Test", null, null);
        SimpleReportEntry simpleReportEntry4 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest$InnerInnerTest$InnerInnerInnerTest", "Inner Inner Inner Test", null, null);

        SimpleReportEntry firstTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest", "Nested Sample", "test", "Should not be displayed");
        WrappedReportEntry wrappedReportEntry1 = new WrappedReportEntry(firstTest, ReportEntryType.SUCCESS, 1, stdout, stderr);

        SimpleReportEntry secondTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest", "Nested Sample", "test2", "Should not be displayed");
        WrappedReportEntry wrappedReportEntry2 = new WrappedReportEntry(secondTest, ReportEntryType.SUCCESS, 1, stdout, stderr);

        SimpleReportEntry thirdTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest", "Inner Test", "test", "Inner failure test should be displayed");
        WrappedReportEntry wrappedReportEntry3 = new WrappedReportEntry(thirdTest, ReportEntryType.FAILURE, 1, stdout, stderr);

        SimpleReportEntry fourthTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest$InnerInnerTest", "Inner Inner Test", "test", "Inner Inner error test should be displayed");
        WrappedReportEntry wrappedReportEntry4 = new WrappedReportEntry(fourthTest, ReportEntryType.ERROR, 1, stdout, stderr);

        SimpleReportEntry fifthTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest$InnerInnerTest$InnerInnerInnerTest", "Inner Inner Inner Test", "test", "Inner Inner Inner skipped test should be displayed");
        WrappedReportEntry wrappedReportEntry5 = new WrappedReportEntry(fifthTest, ReportEntryType.SKIPPED, 1, stdout, stderr);

        SimpleReportEntry sixthTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$FirstInnerTest", "First Inner Test", "test", "FirstInnerTest should not be displayed");
        WrappedReportEntry wrappedReportEntry6 = new WrappedReportEntry(sixthTest, ReportEntryType.SUCCESS, 1, stdout, stderr);

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
        consoleTreeReporter.testSetCompleted(wrappedReportEntry5, testSetStats, null);
        consoleTreeReporter.testSetCompleted(wrappedReportEntry4, testSetStatsForClass, null);
        consoleTreeReporter.testSetCompleted(wrappedReportEntry3, testSetStatsForClass, null);
        consoleTreeReporter.testSetCompleted(wrappedReportEntry2, testSetStatsForClass, null);

    }


}
