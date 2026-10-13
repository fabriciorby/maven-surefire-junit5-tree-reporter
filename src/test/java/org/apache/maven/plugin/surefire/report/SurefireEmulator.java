package org.apache.maven.plugin.surefire.report;

import org.apache.maven.surefire.api.report.RunMode;
import org.apache.maven.surefire.api.report.SimpleReportEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.platform.commons.util.StringUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.DisplayNameGenerator.getDisplayNameGenerator;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

/**
 * Sends the reporter the same events maven-surefire-plugin 3.6.0 sends for a JUnit5 test class and keeps the printed lines.
 * <p>
 * Every class is started before its {@code @Nested} classes, and completed after them with the tests that ran since the
 * last completed class. The source name is always the top level class, and the nested path goes in the qualified name.
 * Classes and methods are sorted by name, since the reflection order is not specified.
 */
public class SurefireEmulator {

    private final DisplayNameGenerator displayNameGenerator = getDisplayNameGenerator(DisplayNameGenerator.Standard.class);
    private final Utf8RecodingDeferredFileOutputStream stdout = new Utf8RecodingDeferredFileOutputStream("stdout");
    private final Utf8RecodingDeferredFileOutputStream stderr = new Utf8RecodingDeferredFileOutputStream("stderr");
    private final CapturingLogger logger;
    private final Class<?> clazz;
    private final ConsoleTreeReporter consoleTreeReporter;
    private TestSetStats pendingStats = newStats();

    public SurefireEmulator(Class<?> clazz) {
        this(ReporterOptions.builder().theme(Theme.UNICODE).build(), clazz);
    }

    public SurefireEmulator(ReporterOptions reporterOptions, Class<?> clazz) {
        this(reporterOptions, clazz, new CapturingLogger());
    }

    /**
     * Prints to the given logger, which emulators of other classes may share, like the forks of a surefire run.
     */
    public SurefireEmulator(ReporterOptions reporterOptions, Class<?> clazz, CapturingLogger logger) {
        this.clazz = clazz;
        this.logger = logger;
        this.consoleTreeReporter = new ConsoleTreeReporter(logger, reporterOptions);
    }

    public List<String> run() {
        run(clazz);
        return logger.lines;
    }

    private void run(Class<?> testClass) {
        WrappedReportEntry classEntry = wrap(classEntry(testClass));
        consoleTreeReporter.testSetStarting(classEntry);
        getTestMethods(testClass).forEach(method -> pendingStats.testSucceeded(wrap(testEntry(testClass, method))));
        getNestedClasses(testClass).forEach(this::run);
        TestSetStats stats = pendingStats;
        pendingStats = newStats();
        consoleTreeReporter.testSetCompleted(classEntry, stats, null);
    }

    private SimpleReportEntry classEntry(Class<?> testClass) {
        return new SimpleReportEntry(RunMode.NORMAL_RUN, 1L, clazz.getName(), getClassDisplayName(testClass),
                testClass.getName(), null, null, null, null, null, Collections.emptyMap());
    }

    private SimpleReportEntry testEntry(Class<?> testClass, Method method) {
        return new SimpleReportEntry(RunMode.NORMAL_RUN, 1L, clazz.getName(), null, testClass.getName(),
                method.getName(), getMethodDisplayName(testClass, method), null, 1, null, Collections.emptyMap());
    }

    private WrappedReportEntry wrap(SimpleReportEntry simpleReportEntry) {
        return new WrappedReportEntry(simpleReportEntry, ReportEntryType.SUCCESS, 0L, 1, stdout, stderr);
    }

    private static TestSetStats newStats() {
        return new TestSetStats(false, true);
    }

    private static List<Class<?>> getNestedClasses(Class<?> testClass) {
        return Arrays.stream(testClass.getDeclaredClasses())
                .filter(i -> i.isAnnotationPresent(Nested.class))
                .sorted(Comparator.comparing(Class::getName))
                .collect(toList());
    }

    private static List<Method> getTestMethods(Class<?> testClass) {
        return Arrays.stream(testClass.getDeclaredMethods())
                .filter(i -> i.isAnnotationPresent(Test.class)
                        || i.isAnnotationPresent(ParameterizedTest.class)
                        || i.isAnnotationPresent(TestFactory.class))
                .sorted(Comparator.comparing(Method::getName))
                .collect(toList());
    }

    private static List<Class<?>> getEnclosingClasses(Class<?> testClass) {
        List<Class<?>> enclosing = new ArrayList<>();
        for (Class<?> i = testClass.getEnclosingClass(); i != null; i = i.getEnclosingClass()) {
            enclosing.add(0, i);
        }
        return enclosing;
    }

    // Same rules as JUnit Jupiter DisplayNameUtils.java
    private static String getDisplayName(AnnotatedElement element, Supplier<String> displayNameSupplier) {
        Optional<DisplayName> displayNameAnnotation = findAnnotation(element, DisplayName.class);
        if (displayNameAnnotation.isPresent()) {
            String displayName = displayNameAnnotation.get().value().trim();
            if (!StringUtils.isBlank(displayName)) return displayName;
        }
        return displayNameSupplier.get();
    }

    private String getClassDisplayName(Class<?> testClass) {
        if (testClass.getEnclosingClass() == null) {
            return getDisplayName(testClass, () -> displayNameGenerator.generateDisplayNameForClass(testClass));
        }
        return getDisplayName(testClass, () -> displayNameGenerator.generateDisplayNameForNestedClass(getEnclosingClasses(testClass), testClass));
    }

    private String getMethodDisplayName(Class<?> testClass, Method method) {
        return getDisplayName(method, () -> displayNameGenerator.generateDisplayNameForMethod(getEnclosingClasses(testClass), testClass, method));
    }
}
