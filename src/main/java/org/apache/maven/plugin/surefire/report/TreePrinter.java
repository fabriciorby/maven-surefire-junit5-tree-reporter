package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;
import org.apache.maven.surefire.api.report.TestSetReportEntry;
import org.apache.maven.surefire.shared.utils.logging.MessageBuilder;

import java.util.List;
import java.util.stream.LongStream;

import static org.apache.maven.plugin.surefire.report.TestSetStats.concatenateWithTestGroup;
import static org.apache.maven.surefire.shared.utils.StringUtils.isBlank;
import static org.apache.maven.surefire.shared.utils.logging.MessageUtils.buffer;

public class TreePrinter {

    private final ConsoleLogger consoleLogger;
    private final List<String> sourceNames;
    private final Tokens tokens = Tokens.ASCII;
    private static final int $ = 36;

    public TreePrinter(ConsoleLogger consoleLogger, List<String> sourceNames) {
        this.consoleLogger = consoleLogger;
        this.sourceNames = sourceNames;
    }

    public void printTest(WrappedReportEntry testResult) {
        if (testResult.isErrorOrFailure()) {
            printFailure(testResult);
        } else if (testResult.isSkipped()) {
            printSkipped(testResult);
        } else if (testResult.isSucceeded()) {
            printSuccess(testResult);
        }
    }

    private void printSuccess(WrappedReportEntry testResult) {
        println(testResult, buffer()
                .success(tokens.successful() + testResult.getReportName()));
    }

    private void printSkipped(WrappedReportEntry testResult) {
        println(testResult, buffer()
                .warning(tokens.skipped() + getSkippedReport(testResult))
                .warning(getSkippedMessage(testResult)));
    }

    private String getSkippedReport(WrappedReportEntry testResult) {
        if (!isBlank(testResult.getReportName())) {
            return testResult.getReportName();
        } else {
            return testResult.getReportSourceName();
        }
    }

    private String getSkippedMessage(WrappedReportEntry testResult) {
        if (!isBlank(testResult.getMessage())) {
            return " (" + testResult.getMessage() + ")";
        } else {
            return "";
        }
    }

    private void printFailure(WrappedReportEntry testResult) {
        println(testResult, buffer()
                .failure(tokens.failed() + testResult.getReportName()));
    }

    public void printClass(WrappedReportEntry testResult) {
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
        println(concatenateWithTestGroup(builder, testResult, !isBlank(testResult.getReportNameWithGroup())));
    }

    private MessageBuilder getPrefix(WrappedReportEntry testResult) {
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

    private long getTreeLength(TestSetReportEntry entry) {
        return entry.getSourceName()
                .chars()
                .filter(c -> c == $)
                .count();
    }

    private void println(WrappedReportEntry testResult, MessageBuilder builder) {
        println(getPrefix(testResult)
                .a(builder)
                .a(" - " + testResult.elapsedTimeAsString() + "s")
                .toString());
    }

    private void println(String message) {
        consoleLogger.info(message);
    }


}
