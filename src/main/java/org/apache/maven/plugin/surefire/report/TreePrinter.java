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

package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;
import org.apache.maven.surefire.shared.lang3.StringUtils;
import org.apache.maven.surefire.shared.utils.logging.MessageBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.maven.plugin.surefire.report.TestSetStats.concatenateWithTestGroup;
import static org.apache.maven.plugin.surefire.report.TextFormatter.abbreviateName;
import static org.apache.maven.surefire.shared.utils.StringUtils.isBlank;
import static org.apache.maven.surefire.shared.utils.logging.MessageUtils.buffer;

/**
 * Tree view printer.
 *
 * @author <a href="mailto:fabriciorby@hotmail.com">Fabrício Yamamoto</a>
 */
public class TreePrinter {

    private final ConsoleLogger consoleLogger;
    private final List<WrappedReportEntry> classResults;
    private final List<WrappedReportEntry> testSetStats;
    private final List<String> sourceNames;
    private final Set<String> distinctSourceName;
    private final ReporterOptions options;
    private static final int $ = 36;

    public TreePrinter(ConsoleLogger consoleLogger, List<WrappedReportEntry> classResults, List<WrappedReportEntry> testSetStats, ReporterOptions options) {
        this.consoleLogger = consoleLogger;
        this.classResults = classResults;
        this.testSetStats = testSetStats;
        this.sourceNames = getSourceNames();
        this.distinctSourceName = getDistinctSourceNames();
        this.options = options;
    }

    private List<String> getSourceNames() {
        return testSetStats
                .stream()
                .map(WrappedReportEntry::getSourceName)
                .collect(toList());
    }

    private Set<String> getDistinctSourceNames() {
        return testSetStats
                .stream()
                .map(WrappedReportEntry::getSourceName)
                .collect(toSet());
    }

    public void printTests() {
        testSetStats
                .stream()
                .map(TestPrinter::new)
                .forEach(printer -> {
                    printer.printTest();
                    printer.printDetails();
                });
    }

    private class TestPrinter {

        private final WrappedReportEntry testResult;
        private final int treeLength;
        private final Theme theme;

        public TestPrinter(WrappedReportEntry testResult) {
            this.testResult = testResult;
            this.treeLength = getTreeLength();
            this.theme = options.getTheme();
        }

        private void printDetails() {
            boolean isSuccess = testResult.getReportEntryType() == ReportEntryType.SUCCESS;
            boolean isError = testResult.getReportEntryType() == ReportEntryType.ERROR;
            boolean isFailure = testResult.getReportEntryType() == ReportEntryType.FAILURE;

            boolean printStackTrace = options.isPrintStacktraceOnError() && isError
                    || options.isPrintStacktraceOnFailure() && isFailure;
            boolean printStdOut = options.isPrintStdoutOnSuccess() && isSuccess
                    || options.isPrintStdoutOnError() && isError
                    || options.isPrintStdoutOnFailure() && isFailure;
            boolean printStdErr = options.isPrintStderrOnSuccess() && isSuccess
                    || options.isPrintStderrOnError() && isError
                    || options.isPrintStderrOnFailure() && isFailure;

            if (printStackTrace || printStdOut || printStdErr) {
                printPreambleDetails();
                if (printStackTrace) {
                    printStackTrace();
                }
                if (printStdOut) {
                    printStdOut();
                }
                if (printStdErr) {
                    printStdErr();
                }
            }
        }

        private void printTest() {
            printClass();
            if (testResult.isErrorOrFailure()) {
                printFailure();
            } else if (testResult.isSkipped()) {
                printSkipped();
            } else if (testResult.isSucceeded()) {
                printSuccess();
            }
        }

        private void printSuccess() {
            println(buffer()
                    .success(theme.successful() + abbreviateName(testResult.getReportName())));
        }

        private void printSkipped() {
            println(buffer()
                    .warning(theme.skipped() + getSkippedReport())
                    .warning(getSkippedMessage()));
        }

        private String getSkippedReport() {
            if (!isBlank(testResult.getReportName())) {
                return abbreviateName(testResult.getReportName());
            } else {
                return testResult.getReportSourceName();
            }
        }

        private String getSkippedMessage() {
            if (!isBlank(testResult.getMessage())) {
                return " (" + testResult.getMessage() + ")";
            } else {
                return "";
            }
        }

        private void printPreambleDetails() {
            println("");
            if (testResult.isSucceeded()) {
                println(buffer().success(theme.details()).success(abbreviateName(testResult.getReportName())).toString());
            } else {
                println(buffer().failure(theme.details()).failure(abbreviateName(testResult.getReportName())).toString());
            }
        }

        private void printStdOut() {
            println("");
            println(buffer().strong("Standard out").toString());
            try {
                testResult.getStdout().writeTo(System.out);
            } catch (final IOException ignored) {
            }
        }

        private void printStdErr() {
            println("");
            println(buffer().strong("Standard error").toString());
            try {
                testResult.getStdErr().writeTo(System.err);
            } catch (final IOException ignored) {
            }
        }

        private void printStackTrace() {
            println("");
            println(buffer().strong("Stack trace").toString());
            String stackTrace = testResult.getStackTrace(false);
            if (stackTrace != null && !StringUtils.isBlank(stackTrace)) {
                println(testResult.getStackTrace(false));
            } else {
                println("[No stack trace available]");
            }
        }

        private void printFailure() {
            println(buffer()
                    .failure(theme.failed() + abbreviateName(testResult.getReportName())));
        }

        private void printClass() {
            if (!distinctSourceName.contains(testResult.getSourceName())) return;
            distinctSourceName.remove(testResult.getSourceName());

            MessageBuilder builder = buffer();
            if (treeLength > 0) {
                if (treeLength > 1) {
                    builder.a(theme.pipe());
                    LongStream.rangeClosed(0, treeLength - 3)
                            .forEach(i -> builder.a(theme.blank()));
                    builder.a(theme.end());
                } else {
                    builder.a(theme.entry());
                }
                if (sourceNames.stream().distinct().count() > 1) {
                    builder.a(theme.down());
                } else {
                    builder.a(theme.dash());
                }
            } else {
                builder.a(theme.entry());
            }

            concatenateWithTestGroup(builder, testResult, !isBlank(testResult.getReportNameWithGroup()));
            builder.a(" - " + testResult.elapsedTimeAsString());

            println(builder.toString());
        }

        private MessageBuilder getTestPrefix() {
            MessageBuilder builder = buffer().a(theme.pipe());
            if (treeLength > 0) {
                LongStream.rangeClosed(0, treeLength - 2)
                        .forEach(i -> builder.a(theme.blank()));
                if (sourceNames.stream().distinct().count() > 1) {
                    builder.a(theme.pipe());
                } else {
                    builder.a(theme.blank());
                }
            }
            sourceNames.remove(testResult.getSourceName());
            if (sourceNames.contains(testResult.getSourceName())) {
                builder.a(theme.entry());
            } else {
                builder.a(theme.end());
            }
            return builder;
        }

        private int getTreeLength() {
            return (int) testResult.getSourceName()
                    .chars()
                    .filter(c -> c == $)
                    .count();
        }

        private void println(MessageBuilder builder) {
            println(getTestPrefix()
                    .a(builder)
                    .a(" - " + testResult.elapsedTimeAsString())
                    .toString());
        }

        private void println(String message) {
            consoleLogger.info(message);
        }

    }

}
