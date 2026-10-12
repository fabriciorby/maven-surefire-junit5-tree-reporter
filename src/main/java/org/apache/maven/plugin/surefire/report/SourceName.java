package org.apache.maven.plugin.surefire.report;

import org.apache.maven.surefire.api.report.ReportEntry;

/**
 * Resolves the binary class name of a test source, e.g. {@code Outer$Inner$InnerInner}.
 * <p>
 * Since maven-surefire-plugin 3.5.4 {@link ReportEntry#getSourceName()} returns only the top level class
 * for JUnit5 nested tests. The nested path is available again from 3.6.0 on through
 * {@link ReportEntry#getSourceQualifiedName()}, which does not exist on 3.5.3 and older.
 */
final class SourceName {

    private static volatile boolean qualifiedNameSupported = true;

    private SourceName() {
    }

    static String of(ReportEntry reportEntry) {
        String sourceName = reportEntry.getSourceName();
        String qualifiedName = getSourceQualifiedName(reportEntry);
        // WrappedReportEntry falls back to the display name when no qualified name was sent, so only trust
        // values that actually describe the same class.
        if (qualifiedName != null && sourceName != null && qualifiedName.startsWith(sourceName)) {
            return qualifiedName;
        }
        return sourceName;
    }

    private static String getSourceQualifiedName(ReportEntry reportEntry) {
        if (!qualifiedNameSupported) return null;
        try {
            return reportEntry.getSourceQualifiedName();
        } catch (NoSuchMethodError | AbstractMethodError e) {
            // Running with maven-surefire-plugin 3.5.3 or older
            qualifiedNameSupported = false;
            return null;
        }
    }
}
