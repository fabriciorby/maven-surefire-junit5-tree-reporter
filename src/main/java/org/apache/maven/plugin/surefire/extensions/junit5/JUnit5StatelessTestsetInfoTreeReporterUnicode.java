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
import org.apache.maven.plugin.surefire.report.*;
import org.apache.maven.surefire.extensions.StatelessTestsetInfoConsoleReportEventListener;

/**
 * Extension of {@link JUnit5StatelessTestsetInfoReporter file and console reporter of test-set} for JUnit5.
 *
 * @author <a href="mailto:fabriciorby@hotmail.com">Fabrício Yamamoto (fabriciorby)</a>
 */
public class JUnit5StatelessTestsetInfoTreeReporterUnicode extends JUnit5StatelessTestsetInfoTreeReporter
{

    @Override
    public StatelessTestsetInfoConsoleReportEventListener<WrappedReportEntry, TestSetStats> createListener(
            ConsoleLogger logger )
    {
        return new ConsoleTreeReporterUnicode( logger, isUsePhrasedClassNameInRunning(),
                isUsePhrasedClassNameInTestCaseSummary() );
    }

    @Override
    public String toString()
    {
        return "JUnit5StatelessTestsetInfoTreeReporterUnicode{"
                + "disable=" + isDisable()
                + ", usePhrasedFileName=" + isUsePhrasedFileName()
                + ", usePhrasedClassNameInRunning=" + isUsePhrasedClassNameInRunning()
                + ", usePhrasedClassNameInTestCaseSummary=" + isUsePhrasedClassNameInTestCaseSummary()
                + "}";
    }
}
