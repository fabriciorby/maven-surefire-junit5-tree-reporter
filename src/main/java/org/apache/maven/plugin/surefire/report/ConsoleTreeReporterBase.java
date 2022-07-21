package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;
import org.apache.maven.surefire.api.report.ReportEntry;
import org.apache.maven.surefire.api.report.TestSetReportEntry;

import java.util.*;
import java.util.function.BiFunction;

import static java.util.Collections.*;

abstract class ConsoleTreeReporterBase extends ConsoleReporter {

    protected static final Map<String, Set<String>> classNames = synchronizedMap(new HashMap<>());
    protected static final Map<String, List<WrappedReportEntry>> classEntries = synchronizedMap(new HashMap<>());
    protected static final Map<String, List<WrappedReportEntry>> testEntries = synchronizedMap(new HashMap<>());
    protected static final int $ = 36;

    public ConsoleTreeReporterBase(ConsoleLogger logger, boolean usePhrasedClassNameInRunning,
                               boolean usePhrasedClassNameInTestCaseSummary) {
        super(logger, usePhrasedClassNameInRunning, usePhrasedClassNameInTestCaseSummary);
    }

    abstract TreePrinter getTreePrinter(List<WrappedReportEntry> classEntries, List<WrappedReportEntry> testEntries);

    @Override
    public void testSetStarting(TestSetReportEntry report) {
        if (hasNestedTests(report)) {
            prepareClassNamesForNestedTests(report);
        }
    }

    @Override
    public void testSetCompleted(WrappedReportEntry report, TestSetStats testSetStats, List<String> testResults) {
        if (isThisEntryNestedClass(report)) {
            if (hasNestedTests(testSetStats)) {
                prepareTestEntriesForNestedTest(report, testSetStats);
            }
            prepareClassEntriesForNestedTest(report);
            if (getClassEntryList(report).size() == getClassNameList(report).size()) {
                sortClassEntryList(report);
                getTreePrinter(getClassEntryList(report), getTestEntryList(report))
                        .printTests();
                cleanEntries(report);
            }
        } else {
            getTreePrinter(singletonList(report), new ArrayList<>(testSetStats.getReportEntries()))
                    .printTests();
        }
    }

    private boolean isThisEntryNestedClass(WrappedReportEntry report) {
        return classNames.containsKey(getRootSourceName(report));
    }

    private List<WrappedReportEntry> getTestEntryList(WrappedReportEntry report) {
        return testEntries.get(getRootSourceName(report));
    }

    private void prepareTestEntriesForNestedTest(WrappedReportEntry report, TestSetStats testSetStats) {
        testEntries.putIfAbsent(getRootSourceName(report), new ArrayList<>(testSetStats.getReportEntries()));
    }

    private void prepareClassEntriesForNestedTest(WrappedReportEntry report) {
        classEntries.putIfAbsent(getRootSourceName(report), new ArrayList<>());
        classEntries.computeIfPresent(getRootSourceName(report), addToCollection(report));
    }

    private void prepareClassNamesForNestedTests(TestSetReportEntry report) {
        classNames.putIfAbsent(getRootSourceName(report), new HashSet<>(singleton(getRootSourceName(report))));
        classNames.computeIfPresent(getRootSourceName(report), addToCollection(report.getSourceName()));
    }

    private Set<String> getClassNameList(WrappedReportEntry report) {
        return classNames.get(getRootSourceName(report));
    }

    private List<WrappedReportEntry> getClassEntryList(WrappedReportEntry report) {
        return classEntries.get(getRootSourceName(report));
    }

    private void sortClassEntryList(WrappedReportEntry report) {
        getClassEntryList(report)
                .sort(Comparator.comparing(WrappedReportEntry::getSourceName));
    }

    protected <J, K, V extends Collection<K>> BiFunction<J, V, V> addToCollection(K obj) {
        return (k, v) -> {
            v.add(obj);
            return v;
        };
    }

    protected void cleanEntries(WrappedReportEntry report) {
        classNames.remove(getRootSourceName(report));
        classEntries.remove(getRootSourceName(report));
        testEntries.remove(getRootSourceName(report));
    }

    protected String getRootSourceName(ReportEntry reportEntry) {
        return reportEntry.getSourceName().split("\\$", -1)[0];
    }

    protected boolean hasNestedTests(TestSetStats testSetStats) {
        return testSetStats.getReportEntries()
                .stream()
                .anyMatch(this::hasNestedTests);
    }

    protected boolean hasNestedTests(ReportEntry reportEntry) {
        return reportEntry.getSourceName()
                .chars()
                .filter(c -> c == $)
                .count() > 0;
    }

}
