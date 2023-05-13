package org.apache.maven.plugin.surefire.extensions.junit5;

import org.apache.maven.plugin.surefire.report.Theme;

/**
 * Extension of {@link JUnit5StatelessTestsetInfoReporter file and console reporter of test-set} for JUnit5.
 *
 * @deprecated Use {@link JUnit5StatelessTestsetInfoTreeReporter} and set the parameter {@code theme} to {@code UNICODE}.
 * @author <a href="mailto:fabriciorby@hotmail.com">Fabrício Yamamoto (fabriciorby)</a>
 */
@Deprecated
public class JUnit5StatelessTestsetInfoTreeReporterUnicode extends JUnit5StatelessTestsetInfoTreeReporter {
    @Override
    public Theme getTheme() {
        return Theme.UNICODE;
    }
}
