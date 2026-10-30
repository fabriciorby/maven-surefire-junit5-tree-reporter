package org.apache.maven.plugin.surefire.report;

import org.apache.maven.surefire.api.report.ReportEntry;
import org.apache.maven.surefire.api.report.RunMode;
import org.apache.maven.surefire.api.report.SimpleReportEntry;

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

    public void prepare() {
        node.addNode(report);
        if (hasNestedTests()) {
            markClassNamesForNestedTests();
        }
    }

    public void print(TreePrinter treePrinter) {
        // Node may not exist if this report's source was never registered via testSetStarting().
        // This can happen with @Disabled tests or @TestFactory dynamic tests where the source
        // name differs from what was registered.
        Optional<Node> optionalNode = Node.getBranchNode(node, getTestClassPath(SourceName.of(report)));
        if (!optionalNode.isPresent()) {
            return;
        }
        Node classToBeTested = optionalNode.get();

        if (testSetStats != null) {
            // Entries whose node is missing are added to the class being completed, or to a node built from them
            Map<Node, String> created = new LinkedHashMap<>();
            testSetStats.getReportEntries()
                    .forEach(entry -> findOrCreateNode(entry, classToBeTested, created).wrappedReportEntries.add(entry));
            describeCreatedNodes(created);
        }

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

    /**
     * Surefire 3.6.1 stopped sending a test set for each nested class, so the nodes of the nested classes are never
     * registered and their entries come with the test set of the outer class. The nodes are built from those entries.
     */
    private Node findOrCreateNode(ReportEntry entry, Node classToBeTested, Map<Node, String> created) {
        List<String> path = getTestClassPath(SourceName.of(entry));
        Optional<Node> existing = Node.getBranchNode(node, path);
        if (existing.isPresent()) {
            if (created.containsKey(existing.get()) && !isBlank(entry.getSourceText())) {
                created.put(existing.get(), entry.getSourceText());
            }
            return existing.get();
        }
        // An entry without a name stands for a whole class that never started (e.g. nested classes of a @Disabled class)
        if (entry.getName() == null || path.size() < 2 || !path.get(0).equals(sourceRootName)) {
            return classToBeTested;
        }
        Node leaf = node.addNode(entry);
        Node current = leaf;
        for (int i = path.size() - 1; i > 0; i--) {
            if (current.getClassReportEntry() == null && !created.containsKey(current)) {
                created.put(current, i == path.size() - 1 && !isBlank(entry.getSourceText())
                        ? entry.getSourceText() : path.get(i));
            }
            current = current.getParent();
        }
        return leaf;
    }

    private void describeCreatedNodes(Map<Node, String> created) {
        created.forEach((createdNode, name) -> {
            SimpleReportEntry classEntry = new SimpleReportEntry(
                    RunMode.NORMAL_RUN, 0L, name, null, null, null);
            createdNode.setClassReportEntry(new WrappedReportEntry(
                    classEntry, ReportEntryType.SUCCESS, 0L, elapsedOf(createdNode), null, null));
        });
    }

    private static int elapsedOf(Node subtree) {
        int elapsed = 0;
        for (WrappedReportEntry entry : subtree.wrappedReportEntries) {
            elapsed += entry.getElapsed(0);
        }
        for (Node branch : subtree.branches) {
            elapsed += elapsedOf(branch);
        }
        return elapsed;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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
        classNames.computeIfPresent(sourceRootName, addToCollection(SourceName.of(report)));
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
        return SourceName.of(report).split("\\$", -1)[0];
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
        return SourceName.of(reportEntry).chars().anyMatch(c -> c == $);
    }
}
