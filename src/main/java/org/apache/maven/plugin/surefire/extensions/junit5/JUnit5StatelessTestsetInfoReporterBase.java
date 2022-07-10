package org.apache.maven.plugin.surefire.extensions.junit5;

import org.apache.maven.plugin.surefire.loader.SurefireClassLoaderModifier;

/**
 * Extension of {@link JUnit5StatelessTestsetInfoReporter file and console reporter of test-set} for JUnit5.
 *
 * @author <a href="mailto:fabriciorby@hotmail.com">Fabrício Yamamoto (fabriciorby)</a>
 */
public abstract class JUnit5StatelessTestsetInfoReporterBase extends JUnit5StatelessTestsetInfoReporter {

    @Override
    public Object clone(ClassLoader target) {
        try {
            new SurefireClassLoaderModifier(target).addThisToSurefireClassLoader();
            return super.clone(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e.getLocalizedMessage());
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{"
                + "disable=" + isDisable()
                + ", usePhrasedFileName=" + isUsePhrasedFileName()
                + ", usePhrasedClassNameInRunning=" + isUsePhrasedClassNameInRunning()
                + ", usePhrasedClassNameInTestCaseSummary=" + isUsePhrasedClassNameInTestCaseSummary()
                + "}";
    }

}
