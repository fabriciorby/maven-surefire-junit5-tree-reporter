package org.apache.maven.plugin.surefire.extensions.junit5;

import org.apache.maven.plugin.surefire.loader.TargetClassLoader;
import org.apache.maven.plugin.surefire.report.Theme;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class JUnit5StatelessTestsetInfoTreeReporterTest {

    @Test
    void cloneKeepsEveryOption() throws Exception {
        JUnit5StatelessTestsetInfoTreeReporter reporter = new JUnit5StatelessTestsetInfoTreeReporter();
        reporter.setPrintStacktraceOnError(true);
        reporter.setPrintStacktraceOnFailure(true);
        reporter.setPrintStderrOnError(true);
        reporter.setPrintStderrOnFailure(true);
        reporter.setPrintStderrOnSuccess(true);
        reporter.setPrintStdoutOnError(true);
        reporter.setPrintStdoutOnFailure(true);
        reporter.setHideResultsOnSuccess(true);
        reporter.setPrintBlankLineBetweenTests(true);
        reporter.setPrintTestCounts(true);
        reporter.setCollapseSuccessfulClasses(true);
        reporter.setHideSuccessfulNestedClasses(true);
        reporter.setSlowTestThreshold("2s");
        reporter.setPrintFailureSummary(true);
        reporter.setMaxFailuresInSummary(7);
        reporter.setGithubStepSummary(true);
        reporter.setTheme(Theme.EMOJI);

        // Like the class loader of a surefire fork: surefire's jars, and the reporter added by clone()
        try (TargetClassLoader target = new TargetClassLoader(getClassPathWithoutThisProject())) {
            Object clone = reporter.clone(target);

            assertThat(clone.getClass().getClassLoader()).isSameAs(target);
            assertThat(clone).hasFieldOrPropertyWithValue("printStacktraceOnError", true)
                    .hasFieldOrPropertyWithValue("printStacktraceOnFailure", true)
                    .hasFieldOrPropertyWithValue("printStderrOnError", true)
                    .hasFieldOrPropertyWithValue("printStderrOnFailure", true)
                    .hasFieldOrPropertyWithValue("printStderrOnSuccess", true)
                    .hasFieldOrPropertyWithValue("printStdoutOnError", true)
                    .hasFieldOrPropertyWithValue("printStdoutOnFailure", true)
                    .hasFieldOrPropertyWithValue("printStdoutOnSuccess", false)
                    .hasFieldOrPropertyWithValue("hideResultsOnSuccess", true)
                    .hasFieldOrPropertyWithValue("printBlankLineBetweenTests", true)
                    .hasFieldOrPropertyWithValue("printTestCounts", true)
                    .hasFieldOrPropertyWithValue("collapseSuccessfulClasses", true)
                    .hasFieldOrPropertyWithValue("hideSuccessfulNestedClasses", true)
                    .hasFieldOrPropertyWithValue("slowTestThreshold", "2s")
                    .hasFieldOrPropertyWithValue("printFailureSummary", true)
                    .hasFieldOrPropertyWithValue("maxFailuresInSummary", 7)
                    .hasFieldOrPropertyWithValue("githubStepSummary", true);
            assertThat(((Enum<?>) clone.getClass().getMethod("getTheme").invoke(clone)).name()).isEqualTo(Theme.EMOJI.name());
        }
    }

    @Test
    void cloneKeepsTheDefaultsOfTheSummaryOptions() throws Exception {
        try (TargetClassLoader target = new TargetClassLoader(getClassPathWithoutThisProject())) {
            Object clone = new JUnit5StatelessTestsetInfoTreeReporter().clone(target);

            assertThat(clone).hasFieldOrPropertyWithValue("printFailureSummary", false)
                    .hasFieldOrPropertyWithValue("maxFailuresInSummary", 50)
                    .hasFieldOrPropertyWithValue("githubStepSummary", false);
        }
    }

    @Test
    void consoleOutputReporterIsClonedIntoTheClassLoaderOfTheForks() throws Exception {
        JUnit5ConsoleOutputTreeReporter reporter = new JUnit5ConsoleOutputTreeReporter();
        reporter.setEncoding("UTF-16");

        try (TargetClassLoader target = new TargetClassLoader(getClassPathWithoutThisProject())) {
            Object clone = reporter.clone(target);

            assertThat(clone.getClass().getName()).isEqualTo(JUnit5ConsoleOutputTreeReporter.class.getName());
            assertThat(clone.getClass().getClassLoader()).isSameAs(target);
            assertThat(clone).hasFieldOrPropertyWithValue("disable", false)
                    .hasFieldOrPropertyWithValue("encoding", "UTF-16");
        }
    }

    private static URL[] getClassPathWithoutThisProject() {
        // surefire runs the tests from a manifest-only jar and passes the real class path in this property
        String classPath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        File target = new File("target").getAbsoluteFile();
        return Arrays.stream(classPath.split(File.pathSeparator))
                .map(File::new)
                .filter(file -> !file.getAbsoluteFile().toPath().startsWith(target.toPath()))
                .map(JUnit5StatelessTestsetInfoTreeReporterTest::toUrl)
                .toArray(URL[]::new);
    }

    private static URL toUrl(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }
}
