package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.log.PluginConsoleLogger;
import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;
import org.apache.maven.surefire.api.report.RunMode;
import org.apache.maven.surefire.api.report.SimpleReportEntry;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ConsoleTreeReporterTest {

    //Test for NestedExampleTest

    Utf8RecodingDeferredFileOutputStream stdout = new Utf8RecodingDeferredFileOutputStream("stdout");
    Utf8RecodingDeferredFileOutputStream stderr = new Utf8RecodingDeferredFileOutputStream("stderr");

    @Test
    void testSetStarting() {
        //Runs 4 times for this class
        SimpleReportEntry simpleReportEntry1 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest", "Nested Sample", null, null);
        SimpleReportEntry simpleReportEntry2 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest", "Inner Test", null, null);
        SimpleReportEntry simpleReportEntry3 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest$InnerInnerTest", "Inner Inner Test", null, null);
        SimpleReportEntry simpleReportEntry4 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest$InnerInnerTest$InnerInnerInnerTest", "Inner Inner Inner Test", null, null);
    }

    @Test
    void testSetCompleted() throws PlexusContainerException {

        //TestStarting parameters
        SimpleReportEntry simpleReportEntry1 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest", "Nested Sample", null, null);
        SimpleReportEntry simpleReportEntry2 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest", "Inner Test", null, null);
        SimpleReportEntry simpleReportEntry3 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest$InnerInnerTest", "Inner Inner Test", null, null);
        SimpleReportEntry simpleReportEntry4 = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest$InnerInnerTest$InnerInnerInnerTest", "Inner Inner Inner Test", null, null);

        //Runs 1 time with all the information
        //Gets all SingleReportEntries with test names and add on a list of WrapperReportEntries to create a TestSetStats
        SimpleReportEntry firstTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest", "Nested Sample", "test", "Should pass");
        SimpleReportEntry secondTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest", "Nested Sample", "test2", "Should pass2");
        SimpleReportEntry thirdTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest", "Inner Test", "test", "Inner test should pass");
        SimpleReportEntry fourthTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest$InnerInnerTest", "Inner Inner Test", "test", "Inner Inner Test should pass");
        SimpleReportEntry fifthTest = new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, "NestedExampleTest$InnerTest$InnerInnerTest$InnerInnerInnerTest", "Inner Inner Inner Test", "test", "Inner Inner Inner Test should pass");

        WrappedReportEntry wrappedReportEntry1 = new WrappedReportEntry(firstTest, ReportEntryType.SUCCESS, 1, stdout, stderr);
        WrappedReportEntry wrappedReportEntry2 = new WrappedReportEntry(secondTest, ReportEntryType.SUCCESS, 1, stdout, stderr);
        WrappedReportEntry wrappedReportEntry3 = new WrappedReportEntry(thirdTest, ReportEntryType.SUCCESS, 1, stdout, stderr);
        WrappedReportEntry wrappedReportEntry4 = new WrappedReportEntry(fourthTest, ReportEntryType.SUCCESS, 1, stdout, stderr);
        WrappedReportEntry wrappedReportEntry5 = new WrappedReportEntry(fifthTest, ReportEntryType.SUCCESS, 1, stdout, stderr);

        TestSetStats testSetStats = new TestSetStats(false, true);
        testSetStats.testSucceeded(wrappedReportEntry1);
        testSetStats.testSucceeded(wrappedReportEntry2);
        testSetStats.testSucceeded(wrappedReportEntry3);
        testSetStats.testSucceeded(wrappedReportEntry4);
        testSetStats.testSucceeded(wrappedReportEntry5);

        DefaultPlexusContainer container = new DefaultPlexusContainer();
        container.getLogger();

        ConsoleTreeReporter consoleTreeReporter = new ConsoleTreeReporter(new PluginConsoleLogger(container.getLogger()), false, false);
        consoleTreeReporter.testSetStarting(simpleReportEntry1);
        consoleTreeReporter.testSetStarting(simpleReportEntry2);
        consoleTreeReporter.testSetStarting(simpleReportEntry3);
        consoleTreeReporter.testSetStarting(simpleReportEntry4);
        consoleTreeReporter.testSetCompleted(null, testSetStats, null);
    }

}