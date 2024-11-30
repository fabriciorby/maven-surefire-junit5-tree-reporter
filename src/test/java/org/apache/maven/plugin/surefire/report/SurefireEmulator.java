package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.log.PluginConsoleLogger;
import org.apache.maven.surefire.api.report.RunMode;
import org.apache.maven.surefire.api.report.SimpleReportEntry;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.logging.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.platform.commons.util.StringUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

public class SurefireEmulator {

    private final ReporterOptions reportOptions;

    public SurefireEmulator() {
        this.reportOptions = ReporterOptions.builder().build();
    }
    public SurefireEmulator(ReporterOptions reporterOptions) {
        this.reportOptions = reporterOptions;
    }

    Utf8RecodingDeferredFileOutputStream stdout = new Utf8RecodingDeferredFileOutputStream("stdout");
    Utf8RecodingDeferredFileOutputStream stderr = new Utf8RecodingDeferredFileOutputStream("stderr");

    static DefaultPlexusContainer container;
    static Logger logger;
    static DisplayNameGenerator displayNameGenerator;

    static {
        try {
            container = new DefaultPlexusContainer();
            logger = container.getLogger();
            displayNameGenerator = DisplayNameGenerator.getDisplayNameGenerator(DisplayNameGenerator.Standard.class);
        } catch (PlexusContainerException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void run(Class<T> clazz) {
        TestSetStats testSetStats = new TestSetStats(false, true);
        getAllMethods(getAllInnerClasses(clazz))
                .entrySet().stream()
                .flatMap((k) -> k.getValue().stream()
                        .map(i -> this.simpleReportEntryGenerator(k.getKey(), i))
                        .map(this::wrappedReportEntryGenerator))
                .forEachOrdered(testSetStats::testSucceeded);

        TestSetStats testSetStatsForClass = new TestSetStats(false, true);
        ConsoleTreeReporter consoleTreeReporter = new ConsoleTreeReporter(new PluginConsoleLogger(logger), reportOptions);
        getAllInnerClasses(clazz).stream()
                .map(this::simpleReportEntryGenerator)
                .forEachOrdered(consoleTreeReporter::testSetStarting);

        List<WrappedReportEntry> completedWrappedEntries =
                getAllInnerClasses(clazz).stream()
                        .map(this::simpleReportEntryGenerator)
                        .map(this::wrappedReportEntryGenerator)
                        .collect(toList());

        completedWrappedEntries.stream()
                .findFirst()
                .ifPresent(i -> consoleTreeReporter.testSetCompleted(i, testSetStats, null));
        completedWrappedEntries.stream()
                .skip(1)
                .forEachOrdered(i -> consoleTreeReporter.testSetCompleted(i, testSetStatsForClass, null));
    }

    private <T> SimpleReportEntry simpleReportEntryGenerator(Class<T> clazz) {
        return new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, clazz.getName(), getClassDisplayName(clazz), null, null);
    }

    private <T> SimpleReportEntry simpleReportEntryGenerator(Class<T> clazz, Method method) {
        return new SimpleReportEntry(RunMode.NORMAL_RUN, 123L, clazz.getName(), getClassDisplayName(clazz), method.getName(), getMethodDisplayName(clazz, method));
    }

    private WrappedReportEntry wrappedReportEntryGenerator(SimpleReportEntry simpleReportEntry) {
        return new WrappedReportEntry(simpleReportEntry, ReportEntryType.SUCCESS, 1, stdout, stderr);
    }

    private List<Class<?>> getAllInnerClasses(Class<?> clazz) {
        return getAllInnerClasses(clazz, new ArrayList<>());
    }

    private List<Class<?>> getAllInnerClasses(Class<?> clazz, List<Class<?>> acc) {
        if (clazz.getDeclaredClasses().length == 0) {
            acc.add(clazz);
            return acc;
        }
        acc.add(clazz);
        acc.addAll(Arrays.stream(clazz.getDeclaredClasses())
                .flatMap(i -> getAllInnerClasses(i, new ArrayList<>()).stream())
                .collect(toList()));
        return acc;
    }

    private Map<Class<?>, List<Method>> getAllMethods(List<Class<?>> classes) {
        return classes.stream()
                .collect(Collectors.toMap(Function.identity(),
                        i -> Arrays.asList(i.getDeclaredMethods()),
                        (x, y) -> y, LinkedHashMap::new));
    }

    private String getDisplayName(AnnotatedElement element, Supplier<String> displayNameSupplier) {
        Optional<DisplayName> displayNameAnnotation = findAnnotation(element, DisplayName.class);
        if (displayNameAnnotation.isPresent()) {
            String displayName = displayNameAnnotation.get().value().trim();
            if (!StringUtils.isBlank(displayName)) return displayName;
        }
        return displayNameSupplier.get();
    }

    private <T> String getClassDisplayName(Class<T> clazz) {
        if (clazz.getEnclosingClass() == null) {
            return getDisplayName(clazz, () -> displayNameGenerator.generateDisplayNameForClass(clazz));
        } else {
            return getDisplayName(clazz, () -> displayNameGenerator.generateDisplayNameForNestedClass(clazz));
        }
    }

    private <T> String getMethodDisplayName(Class<T> clazz, Method method) {
        return getDisplayName(method, () -> displayNameGenerator.generateDisplayNameForMethod(clazz, method));
    }
}
