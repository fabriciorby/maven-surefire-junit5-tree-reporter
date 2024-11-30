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

    private final Class<?> clazz;
    private final ConsoleTreeReporter consoleTreeReporter;

    public SurefireEmulator(Class<?> clazz) {
        this(ReporterOptions.builder().build(), clazz);
    }

    public SurefireEmulator(ReporterOptions reporterOptions, Class<?> clazz) {
        this.clazz = clazz;
        this.consoleTreeReporter = new ConsoleTreeReporter(new PluginConsoleLogger(logger), reporterOptions);
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

    public void run() {
        testsStarting();
        testsCompleted(testsSucceeded());
    }

    private void testsCompleted(TestSetStats testSetStats) {
        List<WrappedReportEntry> completedWrappedEntries =
                getAllInnerClasses(clazz).stream()
                        .map(this::simpleReportEntryGenerator)
                        .map(this::wrappedReportEntryGenerator)
                        .collect(toList());

        //List's head needs to be with complete testSetStats
        completedWrappedEntries.stream().findFirst()
                .ifPresent(i -> consoleTreeReporter.testSetCompleted(i, testSetStats, null));

        //List's tail goes with empty testSetStats
        completedWrappedEntries.stream().skip(1)
                .forEachOrdered(i -> consoleTreeReporter.testSetCompleted(i, new TestSetStats(false, false), null));
    }

    private TestSetStats testsSucceeded() {
        TestSetStats testSetStats = new TestSetStats(false, true);
        getAllMethods(getAllInnerClasses(clazz))
                .entrySet().stream()
                .flatMap((k) -> k.getValue().stream()
                        .map(i -> this.simpleReportEntryGenerator(k.getKey(), i))
                        .map(this::wrappedReportEntryGenerator))
                .forEachOrdered(testSetStats::testSucceeded);
        return testSetStats;
    }

    private void testsStarting() {
        getAllInnerClasses(clazz).stream()
                .map(this::simpleReportEntryGenerator)
                .peek(System.out::println)
                .forEachOrdered(consoleTreeReporter::testSetStarting);
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

    // Got the methods below from JUnit Jupiter codebase DisplayNameUtils.java
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
