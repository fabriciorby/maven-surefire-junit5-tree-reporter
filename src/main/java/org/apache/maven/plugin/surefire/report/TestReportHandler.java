package org.apache.maven.plugin.surefire.report;

import org.apache.maven.surefire.api.report.ReportEntry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;

public class TestReportHandler {

    protected static final Node node = Node.getRoot();
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

    public String getQualifiedName(ReportEntry reportEntry) {
        if (reportEntry instanceof WrappedReportEntry) {
            return ((WrappedReportEntry) reportEntry).getSourceQualifiedName("");
        }
        return reportEntry.getSourceQualifiedName();
    }

    public void prepare() {
        node.addNode(report);
        if (hasNestedTests()) {
            markClassNamesForNestedTests();
        }
    }

    public void print(TreePrinter treePrinter) {
        if (testSetStats != null) {
            testSetStats.getReportEntries()
                    .forEach(entry -> Node.getBranchNode(node, getTestClassPath(getQualifiedName(entry))).get().wrappedReportEntries.add(entry));
        }

        Node classToBeTested = Node.getBranchNode(node, getTestClassPath(getQualifiedName(report))).get();
        classToBeTested.setClassReportEntry((WrappedReportEntry) report);

        if (isMarkedAsNestedTest()) {
            prepareEntriesForNestedTests();
            if (isNestedTestReadyToPrint()) {
                printNestedTests(treePrinter, classToBeTested);
            }
        } else {
            printTests(treePrinter, classToBeTested);
        }
    }

    List<String> getTestClassPath(String sourceName) {
        return Arrays.stream(sourceName.split("\\$", -1)).collect(Collectors.toList());
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
        classNames.computeIfPresent(sourceRootName, addToCollection(getQualifiedName(report)));
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

    private void prepareEntriesForNestedTests() {
        if (hasNestedTests()) {
            prepareTestEntriesForNestedTest();
        }
        prepareClassEntriesForNestedTest();
    }

    private boolean isNestedTestReadyToPrint() {
        return getClassEntryList().size() == getClassNameList().size();
    }

    private void printNestedTests(TreePrinter treePrinter, Node classToBeTested) {
        treePrinter.printTests(classToBeTested);
        cleanEntries();
    }

    private void printTests(TreePrinter treePrinter, Node classToBeTested) {
        treePrinter.printTests(classToBeTested);
    }

    private <J, K, V extends Collection<K>> BiFunction<J, V, V> addToCollection(K obj) {
        return (k, v) -> {
            v.add(obj);
            return v;
        };
    }

    private String getSourceRootName() {
        return getQualifiedName(report).split("\\$", -1)[0];
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
        return getQualifiedName(reportEntry).chars().anyMatch(c -> c == $);
    }
}
