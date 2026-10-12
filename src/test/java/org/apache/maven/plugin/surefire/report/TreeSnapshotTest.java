package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.NestedExampleTest;
import org.apache.maven.plugin.surefire.ParameterizedExampleTest;
import org.apache.maven.plugin.surefire.VeryNestedExampleTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;

/**
 * Asserts the whole printed tree against the golden files in {@code src/test/resources/snapshots}.
 * Run with {@code -Dsnapshot.update=true} to accept an intended change in the output.
 */
class TreeSnapshotTest {

    @BeforeEach
    void cleanNode() {
        Node.clearTree();
    }

    @ParameterizedTest
    @ValueSource(classes = {NestedExampleTest.class, VeryNestedExampleTest.class, ParameterizedExampleTest.class})
    void printsExampleClass(Class<?> testClass) {
        Snapshot.assertMatches("emulator/" + testClass.getSimpleName(), new SurefireEmulator(testClass).run());
    }

    @ParameterizedTest
    @EnumSource(Theme.class)
    void printsWithTheme(Theme theme) {
        ReporterOptions options = ReporterOptions.builder().theme(theme).build();
        Snapshot.assertMatches("theme/" + theme.name().toLowerCase(Locale.ROOT),
                new SurefireEmulator(options, VeryNestedExampleTest.class).run());
    }

    @Test
    void printsBlankLineBetweenTests() {
        ReporterOptions options = ReporterOptions.builder().theme(Theme.UNICODE).printBlankLineBetweenTests(true).build();
        Snapshot.assertMatches("options/print-blank-line-between-tests",
                new SurefireEmulator(options, NestedExampleTest.class).run());
    }
}
