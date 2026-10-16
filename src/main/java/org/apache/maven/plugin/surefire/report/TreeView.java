package org.apache.maven.plugin.surefire.report;

/**
 * The part of a class tree that is printed.
 * <p>
 * The connectors of {@link ActualTreePrinter} are worked out from the nodes it is given, so a node that is not
 * printed must not be among them. The view is a copy of the tree without the nodes that the options hide, and the
 * printer is unaware of the difference. Without an option that needs it there is no copy, and the tree itself is
 * printed.
 */
final class TreeView {

    private final ReporterOptions options;

    TreeView(ReporterOptions options) {
        this.options = options;
    }

    Node of(Node tree) {
        if (!options.isPrintTestCounts() && !options.isCollapseSuccessfulClasses()) {
            return tree;
        }
        TestCounts counts = TestCounts.of(tree);
        if (options.isCollapseSuccessfulClasses() && !counts.hasFailed()) {
            // Just the line of the class, with the counts of what is below it
            Node collapsed = new Node(tree.getName(), tree.getParent());
            collapsed.setClassReportEntry(tree.getClassReportEntry());
            collapsed.counts = counts;
            return collapsed;
        }
        return copyWithBranches(tree, tree.getParent());
    }

    private Node copyWithBranches(Node node, Node viewParent) {
        Node copy = new Node(node.getName(), viewParent);
        copy.setClassReportEntry(node.getClassReportEntry());
        if (options.isPrintTestCounts()) {
            copy.counts = TestCounts.of(node);
        }
        copy.wrappedReportEntries.addAll(node.wrappedReportEntries);
        for (Node branch : node.branches) {
            copy.branches.add(copyWithBranches(branch, copy));
        }
        return copy;
    }
}
