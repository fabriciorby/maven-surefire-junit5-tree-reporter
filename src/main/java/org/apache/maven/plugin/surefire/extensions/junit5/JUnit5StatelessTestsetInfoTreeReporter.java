package org.apache.maven.plugin.surefire.extensions.junit5;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;
import org.apache.maven.plugin.surefire.report.ConsoleTreeReporter;
import org.apache.maven.plugin.surefire.report.FileReporter;
import org.apache.maven.plugin.surefire.report.TestSetStats;
import org.apache.maven.plugin.surefire.report.WrappedReportEntry;
import org.apache.maven.surefire.extensions.StatelessTestsetInfoConsoleReportEventListener;
import org.apache.maven.surefire.extensions.StatelessTestsetInfoFileReportEventListener;

import java.io.File;
import java.nio.charset.Charset;


/**
 * Extension of {@link JUnit5StatelessTestsetInfoReporter file and console reporter of test-set} for JUnit5.
 *
 * @author <a href="mailto:fabriciorby@hotmail.com">Fabrício Yamamoto (fabriciorby)</a>
 */
public class JUnit5StatelessTestsetInfoTreeReporter extends JUnit5StatelessTestsetInfoReporter
{

    @Override
    public StatelessTestsetInfoConsoleReportEventListener<WrappedReportEntry, TestSetStats> createListener(
            ConsoleLogger logger )
    {
        return new ConsoleTreeReporter( logger, isUsePhrasedClassNameInRunning(),
                isUsePhrasedClassNameInTestCaseSummary() );
    }

    @Override
    public StatelessTestsetInfoFileReportEventListener<WrappedReportEntry, TestSetStats> createListener(
            File reportsDirectory, String reportNameSuffix, Charset encoding )
    {
        return new FileReporter( reportsDirectory, reportNameSuffix, encoding, isUsePhrasedFileName(),
                isUsePhrasedClassNameInRunning(), isUsePhrasedClassNameInTestCaseSummary() );
    }

    @Override
    public Object clone( ClassLoader target )
    {
        try
        {
            Object clone = super.clone( target );

            Class<?> cls = clone.getClass();
            cls.getMethod( "setUsePhrasedFileName", boolean.class )
                    .invoke( clone, isUsePhrasedFileName() );
            cls.getMethod( "setUsePhrasedClassNameInTestCaseSummary", boolean.class )
                    .invoke( clone, isUsePhrasedFileName() );
            cls.getMethod( "setUsePhrasedClassNameInRunning", boolean.class )
                    .invoke( clone, isUsePhrasedFileName() );

            return clone;
        }
        catch ( ReflectiveOperationException e )
        {
            throw new IllegalStateException( e.getLocalizedMessage() );
        }
    }

    @Override
    public String toString()
    {
        return "JUnit5StatelessTestsetInfoTreeReporter{"
                + "disable=" + isDisable()
                + ", usePhrasedFileName=" + isUsePhrasedFileName()
                + ", usePhrasedClassNameInRunning=" + isUsePhrasedClassNameInRunning()
                + ", usePhrasedClassNameInTestCaseSummary=" + isUsePhrasedClassNameInTestCaseSummary()
                + "}";
    }
}
