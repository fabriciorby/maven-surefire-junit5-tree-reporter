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

import java.util.*;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toList;
import static org.apache.maven.plugin.surefire.report.TestSetStats.concatenateWithTestGroup;
import static org.apache.maven.surefire.shared.utils.StringUtils.isBlank;
import static org.apache.maven.surefire.shared.utils.logging.MessageUtils.buffer;

/**
 * Tree view class for console reporters.
 *
 * @author <a href="mailto:fabriciorby@hotmail.com">Fabrício Yamamoto</a>
 */
public class ConsoleTreeReporter extends ConsoleReporter {
    private static final int $ = 36;
    public static final String PREFIX_MID = "├─ ";
    public static final String PREFIX_END = "└─ ";
    public static final String PREFIX_PIPE = "|  ";
    public static final String PREFIX_DOWN = "┬─ ";
    public static final String PREFIX_DASH = "── ";
    public static final String SPACE = "   ";
    private final Map<String, TestSetReportEntry> testBuffer = new HashMap<>();

    public ConsoleTreeReporter(ConsoleLogger logger,
                               boolean usePhrasedClassNameInRunning, boolean usePhrasedClassNameInTestCaseSummary) {
        super(logger, usePhrasedClassNameInRunning, usePhrasedClassNameInTestCaseSummary);
    }

    @Override
    public void testSetStarting(TestSetReportEntry report) {
        testBuffer.put(report.getSourceName(), report);
    }

    @Override
    public void testSetCompleted(WrappedReportEntry report, TestSetStats testSetStats, List<String> testResults) {

        List<String> sourceNames = testSetStats.getReportEntries()
                .stream()
                .map(WrappedReportEntry::getSourceName)
                .collect(toList());

        for (WrappedReportEntry testResult : testSetStats.getReportEntries()) {

            MessageBuilder builder = createMessageBuilderWithPrefix(testResult, sourceNames);

            if (testResult.isErrorOrFailure()) {
                println(builder.failure("[XX] " + testResult.getReportName())
                        .a(" - " + testResult.elapsedTimeAsString() + "s")
                        .toString());
            } else if (testResult.isSkipped()) {
                if (!isBlank(testResult.getReportName())) {
                    builder.warning("[??] " + testResult.getReportName());
                } else {
                    builder.warning("[??] " + testResult.getReportSourceName());
                }

                if (!isBlank(testResult.getMessage())) {
                    builder.warning(" (" + testResult.getMessage() + ")");
                }

                println(builder
                        .a(" - " + testResult.elapsedTimeAsString() + "s")
                        .toString());

            } else if (testResult.isSucceeded()) {
                println(builder.success("[OK] " + testResult.getReportName())
                        .a(" - " + testResult.elapsedTimeAsString() + "s")
                        .toString());
            }
        }

    }

    private MessageBuilder createMessageBuilderWithPrefix(WrappedReportEntry testResult, List<String> sourceNames) {

        if(testBuffer.containsKey(testResult.getSourceName())) {
            printClassPrefix(testResult, sourceNames);
            testBuffer.remove(testResult.getSourceName());
        }

        MessageBuilder builder = buffer().a(PREFIX_PIPE);

        if (getTreeLength(testResult) > 0) {
            LongStream.rangeClosed(0, getTreeLength(testResult) - 2)
                    .forEach(i -> builder.a(SPACE));
            if (sourceNames.stream().distinct().count() > 1) {
                builder.a(PREFIX_PIPE);
            } else {
                builder.a(SPACE);
            }
        }

        sourceNames.remove(testResult.getSourceName());
        if (sourceNames.contains(testResult.getSourceName())) {
            builder.a(PREFIX_MID);
        } else {
            builder.a(PREFIX_END);
        }
        return builder;
    }

    private void printClassPrefix(WrappedReportEntry testResult, List<String> sourceNames) {
        MessageBuilder builder = buffer();

        if (getTreeLength(testResult) > 0) {

            if (getTreeLength(testResult) > 1) {
                builder.a(PREFIX_PIPE);
                LongStream.rangeClosed(0, getTreeLength(testResult) - 3)
                        .forEach(i -> builder.a(SPACE));
                builder.a(PREFIX_END);
            } else {
                builder.a(PREFIX_MID);
            }

            if (sourceNames.stream().distinct().count() > 1) {
                builder.a(PREFIX_DOWN);
            } else {
                builder.a(PREFIX_DASH);
            }
        } else {
            builder.a(PREFIX_MID);
        }
        String runningTestCase =
                concatenateWithTestGroup(builder, testResult, !isBlank(testResult.getReportNameWithGroup()));
        getConsoleLogger()
                .info(runningTestCase);
    }

    private long getTreeLength(TestSetReportEntry entry) {
        return entry.getSourceName()
                .chars()
                .filter(c -> c == $)
                .count();
    }

    private void println(String message) {
        this.getConsoleLogger().info(message);
    }

}
