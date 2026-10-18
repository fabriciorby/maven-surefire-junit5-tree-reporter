package org.apache.maven.plugin.surefire.report;

import org.apache.maven.surefire.api.report.ReportEntry;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Recognizes the invocations of a {@code @ParameterizedTest}, {@code @RepeatedTest} or {@code @TestFactory}.
 * <p>
 * maven-surefire-plugin reports {@link ReportEntry#getName()} as the name of the method, not as its display name.
 * For an invocation it appends the index of the invocation: {@code add(int, int)[2]}, and
 * {@code containerFactory()[1][2]} inside a dynamic container. A Java method name never contains {@code [}, so a
 * name ending in indexes is an invocation. See the spike notes for what each surefire version sends: up to 3.2.5
 * the indexes are missing for a method without parameters, which is therefore not recognized and printed as before.
 */
final class ParameterizedInvocations {

    private static final Pattern INVOCATION = Pattern.compile("^(.+?)((?:\\[\\d+\\])+)$");
    private static final Pattern LAST_INDEX = Pattern.compile("\\[\\d+\\]$");

    private final String method;
    private final String indexes;

    private ParameterizedInvocations(String method, String indexes) {
        this.method = method;
        this.indexes = indexes;
    }

    /**
     * @return the method an entry is an invocation of, with its signature as surefire reports it, for example
     * {@code add(int, int)}, or {@code null} when the entry is not an invocation
     */
    static String methodOf(WrappedReportEntry entry) {
        ParameterizedInvocations invocation = parse(entry);
        return invocation == null ? null : invocation.method;
    }

    /**
     * The name printed for an invocation under the node of its method: what the display name has in addition to the
     * method.
     * <ul>
     *   <li>{@code add(int, int)[2] 3} and {@code add(int, int) custom name}: the method prefix is removed, leaving
     *   {@code [2] 3} and {@code custom name}</li>
     *   <li>with a {@code @DisplayName} on the method the prefix is that display name, which is unknown here, so the
     *   name is cut at the index of the invocation: {@code My name[2] 3} gives {@code [2] 3}</li>
     *   <li>otherwise the whole name is kept</li>
     * </ul>
     */
    static String labelOf(WrappedReportEntry entry, String reportName) {
        ParameterizedInvocations invocation = parse(entry);
        if (invocation == null || reportName == null) {
            return reportName;
        }
        if (reportName.startsWith(invocation.method)) {
            String rest = reportName.substring(invocation.method.length()).trim();
            if (!rest.isEmpty()) {
                return rest;
            }
        }
        Matcher lastIndex = LAST_INDEX.matcher(invocation.indexes);
        if (lastIndex.find()) {
            int at = reportName.indexOf(lastIndex.group());
            if (at > 0) {
                return reportName.substring(at);
            }
        }
        return reportName;
    }

    /**
     * @return the elapsed time of all the entries, formatted like surefire formats the one of a single entry
     */
    static String elapsedTimeAsString(List<WrappedReportEntry> entries) {
        int total = 0;
        for (WrappedReportEntry entry : entries) {
            Integer elapsed = entry.getElapsed();
            total += elapsed == null ? 0 : elapsed;
        }
        try {
            // Let surefire do the formatting: the format differs between its versions
            return newEntryElapsing(entries.get(0), total).elapsedTimeAsString();
        } catch (ReflectiveOperationException | RuntimeException e) {
            return String.format(Locale.ROOT, "%.3f s", total / 1000.0);
        }
    }

    private static WrappedReportEntry newEntryElapsing(WrappedReportEntry model, int elapsed)
            throws ReflectiveOperationException {
        for (Constructor<?> constructor : WrappedReportEntry.class.getConstructors()) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length < 5 || types[0] != ReportEntry.class || types[1] != ReportEntryType.class) {
                continue;
            }
            // 3.6.0 added the start time before the elapsed time, and there is a variant with the system properties
            boolean hasStartTime = types[2] == long.class;
            int streams = hasStartTime ? 4 : 3;
            if (types.length != streams + 2 || types[streams] != Utf8RecodingDeferredFileOutputStream.class) {
                continue;
            }
            Object[] args = hasStartTime
                    ? new Object[]{model, ReportEntryType.SUCCESS, 0L, elapsed, null, null}
                    : new Object[]{model, ReportEntryType.SUCCESS, elapsed, null, null};
            return (WrappedReportEntry) constructor.newInstance(args);
        }
        throw new NoSuchMethodException("WrappedReportEntry constructor");
    }

    private static ParameterizedInvocations parse(WrappedReportEntry entry) {
        String name = entry.getName();
        if (name == null) {
            return null;
        }
        Matcher matcher = INVOCATION.matcher(name);
        return matcher.matches() ? new ParameterizedInvocations(matcher.group(1), matcher.group(2)) : null;
    }
}
