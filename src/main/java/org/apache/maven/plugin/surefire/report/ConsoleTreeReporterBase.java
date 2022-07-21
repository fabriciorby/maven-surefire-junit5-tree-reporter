package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;
import org.apache.maven.surefire.api.report.ReportEntry;
import org.apache.maven.surefire.api.report.TestSetReportEntry;

import java.util.*;

import static java.util.Collections.singletonList;
import static java.util.Collections.synchronizedMap;

abstract class ConsoleTreeReporterBase extends ConsoleReporter {

    protected static final Map<String, Set<String>> reportEntriesName = synchronizedMap(new HashMap<>());
    protected static final Map<String, List<WrappedReportEntry>> reportEntriesMap = synchronizedMap(new HashMap<>());
    protected static final Map<String, Collection<WrappedReportEntry>> testSetStatsMap = synchronizedMap(new HashMap<>());
    protected static final int $ = 36;

    public ConsoleTreeReporterBase(ConsoleLogger logger, boolean usePhrasedClassNameInRunning,
                               boolean usePhrasedClassNameInTestCaseSummary) {
        super(logger, usePhrasedClassNameInRunning, usePhrasedClassNameInTestCaseSummary);
    }

    @Override
    public void testSetStarting(TestSetReportEntry report) {
        if (hasNested(report)) {
            reportEntriesName.putIfAbsent(getRootSourceName(report), new HashSet<>());
            reportEntriesName.computeIfPresent(getRootSourceName(report),
                    (k, v) -> {
                        v.add(getRootSourceName(report));
                        v.add(report.getSourceName());
                        return v;
                    });
        }
    }

    @Override
    public void testSetCompleted(WrappedReportEntry report, TestSetStats testSetStats, List<String> testResults) {
        if (hasNested(testSetStats)) {
            testSetStatsMap.putIfAbsent(getRootSourceName(report), new ArrayList<>(testSetStats.getReportEntries()));
            reportEntriesMap.putIfAbsent(getRootSourceName(report), new ArrayList<>());
        }
        if (reportEntriesName.containsKey(getRootSourceName(report))) {
            reportEntriesMap.computeIfPresent(getRootSourceName(report), (k, v) -> {
                v.add(report);
                return v;
            });

            if (reportEntriesMap.get(getRootSourceName(report)).size() == reportEntriesName.get(getRootSourceName(report)).size()) {
                reportEntriesMap.get(getRootSourceName(report)).sort(Comparator.comparing(WrappedReportEntry::getSourceName));
                getTreePrinter(reportEntriesMap.get(getRootSourceName(report)), testSetStatsMap.get(getRootSourceName(report)))
                        .printTests();
                clean(getRootSourceName(report));
            }
        } else {
            getTreePrinter(singletonList(report), testSetStats.getReportEntries()).printTests();
        }
    }

    abstract TreePrinter getTreePrinter(List<WrappedReportEntry> classEntries, Collection<WrappedReportEntry> testEntries);

    protected void clean(String rootSourceName) {
        reportEntriesName.remove(rootSourceName);
        reportEntriesMap.remove(rootSourceName);
    }

    protected String getRootSourceName(ReportEntry reportEntry) {
        return reportEntry.getSourceName().split("\\$")[0];
    }

    protected boolean hasNested(TestSetStats testSetStats) {
        return testSetStats.getReportEntries()
                .stream()
                .anyMatch(this::hasNested);
    }

    protected boolean hasNested(ReportEntry reportEntry) {
        return reportEntry.getSourceName()
                .chars()
                .filter(c -> c == $)
                .count() > 0;
    }

}
