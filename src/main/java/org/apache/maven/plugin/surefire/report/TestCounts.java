package org.apache.maven.plugin.surefire.report;

import java.util.ArrayList;
import java.util.List;

/**
 * How many tests of a node, and of the nodes below it, passed, failed or were skipped. A failed test includes the
 * ones with an error.
 */
final class TestCounts {
    private int passed;
    private int failed;
    private int skipped;

    static TestCounts of(Node node) {
        TestCounts counts = new TestCounts();
        counts.add(node);
        return counts;
    }

    private void add(Node node) {
        node.wrappedReportEntries.forEach(this::add);
        node.branches.forEach(this::add);
    }

    private void add(WrappedReportEntry entry) {
        if (entry.isErrorOrFailure()) {
            failed++;
        } else if (entry.isSkipped()) {
            skipped++;
        } else if (entry.isSucceeded()) {
            passed++;
        }
    }

    /**
     * @return for example {@code (3 passed, 1 failed, 1 skipped)} without the categories that are zero, or an empty
     * string when there are no tests
     */
    String format() {
        List<String> parts = new ArrayList<>();
        if (passed > 0) parts.add(passed + " passed");
        if (failed > 0) parts.add(failed + " failed");
        if (skipped > 0) parts.add(skipped + " skipped");
        return parts.isEmpty() ? "" : "(" + String.join(", ", parts) + ")";
    }
}
