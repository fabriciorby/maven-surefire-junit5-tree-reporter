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
    private static final Tokens tokens = Tokens.ASCII;

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
        List<String> sourceNames = getSourceNames(testSetStats);
        for (WrappedReportEntry testResult : testSetStats.getReportEntries()) {
            MessageBuilder builder = createMessageBuilderWithPrefix(testResult, sourceNames);
            if (testResult.isErrorOrFailure()) {
                printFailure(testResult, builder);
            } else if (testResult.isSkipped()) {
                printSkipped(testResult, builder);
            } else if (testResult.isSucceeded()) {
                printSuccess(testResult, builder);
            }
        }
    }

    private List<String> getSourceNames(TestSetStats testSetStats) {
        return testSetStats.getReportEntries()
                .stream()
                .map(WrappedReportEntry::getSourceName)
                .collect(toList());
    }

    private void printSuccess(WrappedReportEntry testResult, MessageBuilder builder) {
        println(builder.success(tokens.successful() + testResult.getReportName())
                .a(" - " + testResult.elapsedTimeAsString() + "s")
                .toString());
    }

    private void printSkipped(WrappedReportEntry testResult, MessageBuilder builder) {
        if (!isBlank(testResult.getReportName())) {
            builder.warning(tokens.skipped() + testResult.getReportName());
        } else {
            builder.warning(tokens.skipped() + testResult.getReportSourceName());
        }
        if (!isBlank(testResult.getMessage())) {
            builder.warning(" (" + testResult.getMessage() + ")");
        }
        println(builder
                .a(" - " + testResult.elapsedTimeAsString() + "s")
                .toString());
    }

    private void printFailure(WrappedReportEntry testResult, MessageBuilder builder) {
        println(builder.failure(tokens.failed() + testResult.getReportName())
                .a(" - " + testResult.elapsedTimeAsString() + "s")
                .toString());
    }

    private MessageBuilder createMessageBuilderWithPrefix(WrappedReportEntry testResult, List<String> sourceNames) {
        if(testBuffer.containsKey(testResult.getSourceName())) {
            printClassPrefix(testResult, sourceNames);
            testBuffer.remove(testResult.getSourceName());
        }
        return printTestPrefix(testResult, sourceNames);
    }

    private MessageBuilder printTestPrefix(WrappedReportEntry testResult, List<String> sourceNames) {
        MessageBuilder builder = buffer().a(tokens.pipe());
        if (getTreeLength(testResult) > 0) {
            LongStream.rangeClosed(0, getTreeLength(testResult) - 2)
                    .forEach(i -> builder.a(tokens.blank()));
            if (sourceNames.stream().distinct().count() > 1) {
                builder.a(tokens.pipe());
            } else {
                builder.a(tokens.blank());
            }
        }
        sourceNames.remove(testResult.getSourceName());
        if (sourceNames.contains(testResult.getSourceName())) {
            builder.a(tokens.entry());
        } else {
            builder.a(tokens.end());
        }
        return builder;
    }

    private void printClassPrefix(WrappedReportEntry testResult, List<String> sourceNames) {
        MessageBuilder builder = buffer();
        if (getTreeLength(testResult) > 0) {
            if (getTreeLength(testResult) > 1) {
                builder.a(tokens.pipe());
                LongStream.rangeClosed(0, getTreeLength(testResult) - 3)
                        .forEach(i -> builder.a(tokens.blank()));
                builder.a(tokens.end());
            } else {
                builder.a(tokens.entry());
            }
            if (sourceNames.stream().distinct().count() > 1) {
                builder.a(tokens.down());
            } else {
                builder.a(tokens.dash());
            }
        } else {
            builder.a(tokens.entry());
        }
        String runningTestCase =
                concatenateWithTestGroup(builder, testResult, !isBlank(testResult.getReportNameWithGroup()));
        println(runningTestCase);
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
