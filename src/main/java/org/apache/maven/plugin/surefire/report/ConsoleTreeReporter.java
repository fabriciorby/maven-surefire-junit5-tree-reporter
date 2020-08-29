package org.apache.maven.plugin.surefire.report;

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
import org.apache.maven.surefire.api.report.TestSetReportEntry;
import org.apache.maven.surefire.shared.utils.logging.MessageBuilder;

import java.util.List;
import java.util.stream.LongStream;

import static org.apache.maven.plugin.surefire.report.TestSetStats.concatenateWithTestGroup;
import static org.apache.maven.surefire.shared.utils.StringUtils.isBlank;
import static org.apache.maven.surefire.shared.utils.logging.MessageUtils.buffer;

/**
 * Tree view class for console reporters.
 *
 * @author <a href="mailto:fabriciorby@hotmail.com">Fabrício Yamamoto</a>
 */
public class ConsoleTreeReporter extends ConsoleReporter
{
    private static final String TEST_SET_STARTING_PREFIX = "+-- ";

    public ConsoleTreeReporter(ConsoleLogger logger,
                               boolean usePhrasedClassNameInRunning, boolean usePhrasedClassNameInTestCaseSummary )
    {
        super( logger, usePhrasedClassNameInRunning, usePhrasedClassNameInTestCaseSummary );
    }

    @Override
    public void testSetStarting( TestSetReportEntry report )
    {
        StringBuilder stringBuilder = new StringBuilder();
        LongStream.rangeClosed( 0, getTreeLenght(report) - 1 ).forEach( i -> stringBuilder.append( "| " ));
        getConsoleLogger().info( stringBuilder.toString() );

        MessageBuilder builder = buffer();
        LongStream.rangeClosed( 0, getTreeLenght(report) - 1 ).forEach( i -> builder.a( "| " ));
        builder.a( TEST_SET_STARTING_PREFIX );

        String runningTestCase =
            concatenateWithTestGroup( builder, report, !isBlank( report.getReportNameWithGroup() ) );

        getConsoleLogger()
                .info( runningTestCase );
    }

    @Override
    public void testSetCompleted( WrappedReportEntry report, TestSetStats testSetStats, List<String> testResults )
    {

        for ( WrappedReportEntry testResult : testSetStats.getReportEntries() )
        {
            final  MessageBuilder builder = buffer().a( "| " );
            LongStream.rangeClosed( 0, getTreeLenght(testResult) - 1 )
                    .forEach( i -> builder.a( "| " ));
            builder.a( TEST_SET_STARTING_PREFIX );
            if ( testResult.isErrorOrFailure() )
            {
                println( builder.failure( "[XX] " + testResult.getReportName() )
                            .a( " - " + testResult.elapsedTimeAsString() + "s" )
                            .toString());
            }
            else if ( testResult.isSkipped() )
            {
                if ( !isBlank( testResult.getReportName() ) )
                {
                    builder.warning( "[??] " + testResult.getReportName() );
                }
                else
                {
                    builder.warning( "[??] " + testResult.getReportSourceName() );
                }

                if ( !isBlank( testResult.getMessage() ) )
                {
                    builder.warning( " (" + testResult.getMessage() + ")" );
                }

                println( builder
                            .a( " - " + testResult.elapsedTimeAsString() + "s" )
                            .toString());
            }
            else if ( testResult.isSucceeded() )
            {
                println( builder.success( "[OK] " + testResult.getReportName() )
                                .a( " - " + testResult.elapsedTimeAsString() + "s" )
                                .toString());
            }
        }

    }

    private long getTreeLenght(TestSetReportEntry entry) {
        return entry.getSourceName()
                .chars()
                .filter( c -> c == 36 )
                .count();
    }

    private void println(String message) {
                this.getConsoleLogger().info( message );
    }

}
