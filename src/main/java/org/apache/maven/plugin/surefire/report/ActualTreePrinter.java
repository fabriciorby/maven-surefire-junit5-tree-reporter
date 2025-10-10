package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;
import org.apache.maven.surefire.shared.lang3.StringUtils;
import org.apache.maven.surefire.shared.utils.logging.MessageBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.LongStream;

import static org.apache.maven.plugin.surefire.report.TextFormatter.abbreviateName;
import static org.apache.maven.surefire.shared.utils.StringUtils.isBlank;
import static org.apache.maven.surefire.shared.utils.logging.MessageUtils.buffer;

public class ActualTreePrinter {
    private final Theme theme;
    private final Node tree;
    private final ConsoleLogger consoleLogger;
    private final ReporterOptions options;

    public ActualTreePrinter(Node node, ConsoleLogger consoleLogger, ReporterOptions options) {
        this.tree = node;
        this.consoleLogger = consoleLogger;
        this.options = options;
        this.theme = options.getTheme();
    }

    public void print() {
        if (options.isPrintBlankLineBetweenTests()) println("");
        print(tree.branches.get(0));
        Node.clearTree();
    }

    private void print(Node node) {
        printClass(node);
        node.wrappedReportEntries.forEach(i -> printTestFormated(node, i));
        node.branches.forEach(this::print);
    }

    private void printTestFormated(Node node, WrappedReportEntry testResult) {
        if (testResult.isErrorOrFailure()) {
            printFailure(node, testResult);
        } else if (testResult.isSkipped()) {
            printSkipped(node, testResult);
        } else if (isSuccessPrintAllowed() && testResult.isSucceeded()) {
            printSuccess(node, testResult);
        }
        printDetails(testResult);
    }

    private boolean isSuccessPrintAllowed() {
        return !options.isHideResultsOnSuccess();
    }

    private void printSuccess(Node node, WrappedReportEntry testResult) {
        printTestResult(buffer().success(theme.successful() + abbreviateName(testResult.getReportName())), node, testResult);
    }

    private void printTestResult(MessageBuilder builder, Node node, WrappedReportEntry testResult) {
        println(getTestPrefix(node, testResult)
                .a(builder)
                .a(" - " + testResult.elapsedTimeAsString())
                .toString());
    }

    private void println(String message) {
        consoleLogger.info(message);
    }

    private boolean isLastMissingBranch(Node node) {
        Node rootChild = tree.branches.get(0); // first after ROOT
        if (rootChild.hasBranches()) {
            Node rootChildLastChild = getLastItem(rootChild.branches); // last branch in root child
            return node.getParent(rootChildLastChild.getName()).isPresent() || node == rootChildLastChild;
        } else {
            return true;
        }
    }

    private static <T> T getLastItem(List<T> list) {
        return list.get(list.size() - 1);
    }

    private MessageBuilder getTestPrefix(Node node, WrappedReportEntry testResult) {
        MessageBuilder builder = buffer();
        if (isLastMissingBranch(node))
            builder.a(theme.blank());
        else
            builder.a(theme.pipe());
        if (node.getDepth() > 1) {
            LongStream.rangeClosed(0, node.getDepth() - 3)
                    .forEach(i -> builder.a(theme.blank()));
            if (node.getParent().hasBranches() && node.hasBranches()
            ) {
                builder.a(theme.pipe());
            } else {
                builder.a(theme.blank());
            }
        }
        if (isLastTestToBeEval(node, testResult)) {
            builder.a(theme.entry());
        } else {
            builder.a(theme.end());
        }
        return builder;
    }

    private static boolean isLastTestToBeEval(Node node, WrappedReportEntry testResult) {
        return node.wrappedReportEntries.indexOf(testResult) + 1 != node.wrappedReportEntries.size();
    }

    private void printClass(Node node) {
        MessageBuilder builder = buffer();
        if (node.getDepth() > 1) {
            if (node.getDepth() > 2) {
                if (isLastMissingBranch(node)) builder.a(theme.blank());
                else builder.a(theme.pipe());
                LongStream.rangeClosed(0, node.getDepth() - 4)
                        .forEach(i -> builder.a(theme.blank()));
                builder.a(theme.end());
            } else {
                if (isLastMissingBranch(node)) builder.a(theme.end());
                else builder.a(theme.entry());
            }
        }
        if (node.hasBranches()) {
            builder.a(theme.down());
        } else {
            builder.a(theme.dash());
        }

        builder.strong(cleanReportName(node));
        builder.a(" - " + node.getClassReportEntry().elapsedTimeAsString());

        println(builder.toString());
    }

    private String cleanReportName(Node node) {
        if (node.getParent().getClassReportEntry() != null) {
            int stringSizeToRemove = node.getParent().getClassReportEntry().getReportNameWithGroup().length() + 1;
            return node.getClassReportEntry().getReportNameWithGroup().substring(stringSizeToRemove);
        } else {
            return node.getClassReportEntry().getReportNameWithGroup();
        }
    }

    private void printDetails(WrappedReportEntry testResult) {
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
            printPreambleDetails(testResult);
            if (printStackTrace) printStackTrace(testResult);
            if (printStdOut) printStdOut(testResult);
            if (printStdErr) printStdErr(testResult);
        }
    }

    private void printSkipped(Node node, WrappedReportEntry testResult) {
        printTestResult(buffer()
                .warning(theme.skipped() + getSkippedReport(testResult))
                .warning(getSkippedMessage(testResult)), node, testResult);
    }

    private String getSkippedReport(WrappedReportEntry testResult) {
        if (!isBlank(testResult.getReportName())) {
            return abbreviateName(testResult.getReportName());
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

    private void printPreambleDetails(WrappedReportEntry testResult) {
        if (testResult.isSucceeded()) {
            println(buffer().success(theme.details()).success(abbreviateName(testResult.getReportName())).toString());
        } else {
            println(buffer().failure(theme.details()).failure(abbreviateName(testResult.getReportName())).toString());
        }
    }

    private void printStdOut(WrappedReportEntry testResult) {
        println("");
        println(buffer().strong("Standard out").toString());
        try {
            testResult.getStdout().writeTo(System.out);
        } catch (final IOException ignored) {
        }
    }

    private void printStdErr(WrappedReportEntry testResult) {
        println("");
        println(buffer().strong("Standard error").toString());
        try {
            testResult.getStdErr().writeTo(System.err);
        } catch (final IOException ignored) {
        }
    }

    private void printStackTrace(WrappedReportEntry testResult) {
        println("");
        println(buffer().strong("Stack trace").toString());
        String stackTrace = testResult.getStackTrace(false);
        if (stackTrace != null && !StringUtils.isBlank(stackTrace)) {
            println(testResult.getStackTrace(false));
        } else {
            println("[No stack trace available]");
        }
    }

    private void printFailure(Node node, WrappedReportEntry testResult) {
        printTestResult(buffer()
                .failure(theme.failed() + abbreviateName(testResult.getReportName())), node, testResult);
    }
}
