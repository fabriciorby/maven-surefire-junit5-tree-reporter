package org.apache.maven.plugin.surefire.loader;

import org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import static org.assertj.core.api.Assertions.assertThat;

class SurefireClassLoaderModifierTest {

    private static final String REPORTER_CLASS_FILE =
            JUnit5StatelessTestsetInfoTreeReporter.class.getName().replace('.', '/') + ".class";

    @Test
    void addsTheJarTheReporterWasLoadedFrom() throws Exception {
        URL reporterLocation = JUnit5StatelessTestsetInfoTreeReporter.class.getProtectionDomain().getCodeSource().getLocation();
        // Every jar of a local repository inside a folder named after the reporter matches the name, issue #82
        URL decoy = new File("maven-surefire-junit5-tree-reporter/target/local-repo/org/junit/junit.jar").toURI().toURL();

        Thread thread = Thread.currentThread();
        ClassLoader contextClassLoader = thread.getContextClassLoader();
        try (URLClassLoader context = new URLClassLoader(new URL[]{decoy}, contextClassLoader);
             TargetClassLoader target = new TargetClassLoader()) {
            thread.setContextClassLoader(context);

            new SurefireClassLoaderModifier(target).addThisToSurefireClassLoader();

            assertThat(target.getURLs()).containsExactly(reporterLocation);
            assertThat(target.findResource(REPORTER_CLASS_FILE)).isNotNull();
        } finally {
            thread.setContextClassLoader(contextClassLoader);
        }
    }
}
