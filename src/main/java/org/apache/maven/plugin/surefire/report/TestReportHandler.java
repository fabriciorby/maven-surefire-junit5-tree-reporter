package org.apache.maven.plugin.surefire.report;

import org.apache.maven.surefire.api.report.ReportEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

public class TestReportHandler {

    protected static final Map<String, Set<String>> classNames = new ConcurrentHashMap<>();
    protected static final Map<String, List<WrappedReportEntry>> classEntries = new ConcurrentHashMap<>();
    protected static final Map<String, List<WrappedReportEntry>> testEntries = new ConcurrentHashMap<>();
    protected static final int $ = 36;

    private final ReportEntry report;
    private final TestSetStats testSetStats;
    private final String sourceRootName;

    public TestReportHandler(ReportEntry report, TestSetStats testSetStats) {
        this.report = report;
        this.testSetStats = testSetStats;
        this.sourceRootName = getSourceRootName();
    }

    public TestReportHandler(ReportEntry report) {
        this(report, null);
    }

    public void prepare() {
        if (hasNestedTests()) {
            markClassNamesForNestedTests();
        }
    }

    public void print(BiFunction<List<WrappedReportEntry>, List<WrappedReportEntry>, TreePrinter> getTreePrinter) {
        if (isMarkedAsNestedTest()) {
            prepareEntriesForNestedTests();
            if (isNestedTestReadyToPrint()) {
                printNestedTests(getTreePrinter);
            }
        } else {
            printTests(getTreePrinter);
        }
    }

    private boolean isMarkedAsNestedTest() {
        return classNames.containsKey(sourceRootName);
    }

    private void prepareClassEntriesForNestedTest() {
        classEntries.computeIfAbsent(sourceRootName, k -> new ArrayList<>()).add((WrappedReportEntry) report);
    }

    private List<WrappedReportEntry> getClassEntryList() {
        return classEntries.get(sourceRootName);
    }

    private void markClassNamesForNestedTests() {
        classNames.putIfAbsent(sourceRootName, new HashSet<>(singleton(sourceRootName)));
        classNames.computeIfPresent(sourceRootName, addToCollection(report.getSourceName()));
    }

    private Set<String> getClassNameList() {
        return classNames.get(sourceRootName);
    }

    private void prepareTestEntriesForNestedTest() {
        testEntries.computeIfAbsent(sourceRootName, $ -> new ArrayList<>()).addAll(testSetStats.getReportEntries());
    }

    private List<WrappedReportEntry> getTestEntryList() {
        return testEntries.get(sourceRootName);
    }

    private void cleanEntries() {
        classNames.remove(sourceRootName);
        classEntries.remove(sourceRootName);
        testEntries.remove(sourceRootName);
    }

    private void sortClassEntryList() {
        getClassEntryList()
                .sort(Comparator.comparing(ReportEntry::getSourceName));
    }

    private void prepareEntriesForNestedTests() {
        if (hasNestedTests()) {
            prepareTestEntriesForNestedTest();
        }
        prepareClassEntriesForNestedTest();
    }

    private boolean isNestedTestReadyToPrint() {
        return getClassEntryList().size() == getClassNameList().size();
    }

    private void printNestedTests(BiFunction<List<WrappedReportEntry>,List<WrappedReportEntry>, TreePrinter> getTreePrinter) {
        sortClassEntryList();
        getTreePrinter
                .apply(getClassEntryList(), getTestEntryList())
                .printTests();
        cleanEntries();
    }

    private void printTests(BiFunction<List<WrappedReportEntry>,List<WrappedReportEntry>, TreePrinter> getTreePrinter) {
        getTreePrinter
                .apply(singletonList((WrappedReportEntry) report), new ArrayList<>(testSetStats.getReportEntries()))
                .printTests();
    }

    private <J, K, V extends Collection<K>> BiFunction<J, V, V> addToCollection(K obj) {
        return (k, v) -> {
            v.add(obj);
            return v;
        };
    }

    private String getSourceRootName() {
        return report.getSourceName().split("\\$", -1)[0];
    }

    private boolean hasNestedTests() {
        if (this.testSetStats == null) {
            return hasNestedTests(report);
        } else {
            return hasNestedTests(testSetStats);
        }
    }

    private boolean hasNestedTests(TestSetStats testSetStats) {
        return testSetStats.getReportEntries()
                .stream()
                .anyMatch(this::hasNestedTests);
    }

    private boolean hasNestedTests(ReportEntry reportEntry) {
        return reportEntry.getSourceName().chars().anyMatch(c -> c == $);
    }
}
