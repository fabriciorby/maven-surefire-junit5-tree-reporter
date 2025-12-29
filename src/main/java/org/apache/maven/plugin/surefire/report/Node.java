package org.apache.maven.plugin.surefire.report;

import org.apache.maven.surefire.api.report.ReportEntry;
import org.apache.maven.surefire.api.report.SimpleReportEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Node {
    private static final Node ROOT = new Node("ROOT", 0);
    final List<Node> branches = new ArrayList<>();
    private final Map<String, Node> branchMap = new HashMap<>();
    private final Node parent;
    private final String name;
    private final int depth;
    public final List<WrappedReportEntry> wrappedReportEntries = new ArrayList<>();
    private WrappedReportEntry classReportEntry;

    public static void clearTree() {
        ROOT.branches.clear();
        ROOT.branchMap.clear();
    }

    public String getName() {
        return name;
    }

    public int getDepth() {
        return depth;
    }

    public static Node getRoot() {
        return ROOT;
    }

    public boolean hasBranches() {
       return !branches.isEmpty();
    }

    public void removeBranch(Node branch) {
        branches.remove(branch);
        branchMap.remove(branch.name);
    }

    private Node(String name, int nestLevel) {
        this(name, nestLevel, null);
    }

    private Node(String name, int depth, Node parent) {
        this.name = name;
        this.depth = depth;
        this.parent = parent;
    }

    Node(String name, Node parent) {
        this.name = name;
        this.depth = parent.depth + 1;
        this.parent = parent;
    }

    public Node addNode(ReportEntry reportEntry) {
        String[] nodes = reportEntry.getSourceName().split("\\$", -1);
        return addChildren(nodes);
    }

    protected Node addChildren(String... nodes) {
        return addChildren(Arrays.stream(nodes).collect(Collectors.toList()));
    }

    protected Node addChildren(List<String> nodes) {
        if (nodes.isEmpty()) return this;
        return getBranchNode(nodes.get(0))
                .orElseGet(() -> generateBranch(nodes.get(0)))
                .addChildren(removeFirst(nodes));
    }

    private Node generateBranch(String name) {
        Node branch = new Node(name, this);
        branches.add(branch);
        branchMap.put(name, branch);
        return branch;
    }

    private <T> List<T> removeFirst(List<T> list) {
        return list.subList(1, list.size());
    }

    boolean containsBranch(String reportName) {
        return branchMap.containsKey(reportName);
    }

    Optional<Node> getBranchNode(String reportName) {
        return Optional.ofNullable(branchMap.get(reportName));
    }

    Optional<Node> getBranchNode(List<String> nodePath) {
        return getBranchNode(this, nodePath);
    }

    static Optional<Node> getBranchNode(Node node, List<String> nodePath) {
        if (nodePath.size() == 1) {
            return node.getBranchNode(nodePath.get(0));
        }
        if (node.getBranchNode(nodePath.get(0)).isPresent())
            return getBranchNode(node.getBranchNode(nodePath.get(0)).get(), nodePath.subList(1, nodePath.size()));
        return Optional.empty();
    }

    public Optional<Node> getParent(String parentName) {
        if (parent == null) return Optional.empty();
        if (parent.getName().equals(parentName)) return Optional.of(parent);
        return parent.getParent(parentName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Node)
            return ((Node) obj).name.equals(this.name);
        else
            return false;
    }

    public Node getParent() {
        return this.parent;
    }

    public WrappedReportEntry getClassReportEntry() {
        return classReportEntry;
    }

    public void setClassReportEntry(WrappedReportEntry classReportEntry) {
        this.classReportEntry = classReportEntry;
    }
}
