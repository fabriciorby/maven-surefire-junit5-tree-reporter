package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;
import org.apache.maven.surefire.api.report.SafeThrowable;
import org.apache.maven.surefire.api.report.StackTraceWriter;
import org.apache.maven.surefire.shared.lang3.StringUtils;
import org.apache.maven.surefire.shared.utils.logging.MessageBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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

    public void printAndRemoveChild() {
        if (options.isPrintBlankLineBetweenTests()) println("");
        print();
        removeChild();
    }

    public void print() {
        print(tree);
    }

    public void removeChild() {
        tree.getParent().branches.remove(tree);
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

    private boolean isShown(WrappedReportEntry testResult) {
        return isSuccessPrintAllowed() || !testResult.isSucceeded();
    }

    private void printSuccess(Node node, WrappedReportEntry testResult) {
        printTestResult(buffer().success(theme.successful() + getTestName(testResult)), node, testResult);
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

    /**
     * @return whether the node is the last branch of its parent. A top-level class has no siblings that matter here:
     * with parallel test classes ROOT also holds the classes still running, in any order.
     */
    private static boolean isLastBranch(Node node) {
        List<Node> siblings = node.getParent().branches;
        return siblings.get(siblings.size() - 1) == node;
    }

    /**
     * The columns of the nested classes the node is in, from the second level down to the node itself if asked. Each
     * is the line going down along that class while it has siblings still to be printed.
     */
    private String getBranchColumns(Node node, boolean includeNode) {
        if (node.getDepth() <= 1) return "";
        String columns = getBranchColumns(node.getParent(), true);
        return includeNode ? columns + (isLastBranch(node) ? theme.blank() : theme.pipe()) : columns;
    }

    private MessageBuilder getTestPrefix(Node node, WrappedReportEntry testResult) {
        MessageBuilder builder = buffer();
        builder.a(getBranchColumns(node, true));
        builder.a(node.hasBranches() ? theme.pipe() : theme.blank());
        if (isLastTestToBeEval(node, testResult)) {
            builder.a(theme.entry());
        } else {
            builder.a(theme.end());
        }
        return builder;
    }

    private boolean isLastTestToBeEval(Node node, WrappedReportEntry testResult) {
        // Only the tests that are printed count, a hidden success after the last printed test does not make it
        // a middle one.
        List<WrappedReportEntry> shown = node.wrappedReportEntries.stream()
                .filter(this::isShown)
                .collect(Collectors.toList());
        return shown.indexOf(testResult) + 1 != shown.size();
    }

    private void printClass(Node node) {
        MessageBuilder builder = buffer();
        if (node.getDepth() > 1) {
            builder.a(getBranchColumns(node, false));
            builder.a(isLastBranch(node) ? theme.end() : theme.entry());
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
        if (node.getParent().getClassReportEntry() != null &&
                node.getClassReportEntry().getReportNameWithGroup().startsWith(node.getParent().getClassReportEntry().getReportNameWithGroup())
        ) {
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
        boolean printStdOut = (options.isPrintStdoutOnSuccess() && isSuccess
                || options.isPrintStdoutOnError() && isError
                || options.isPrintStdoutOnFailure() && isFailure)
                && hasOutput(testResult.getStdout());
        boolean printStdErr = (options.isPrintStderrOnSuccess() && isSuccess
                || options.isPrintStderrOnError() && isError
                || options.isPrintStderrOnFailure() && isFailure)
                && hasOutput(testResult.getStdErr());

        if (printStackTrace || printStdOut || printStdErr) {
            printPreambleDetails(testResult);
            if (printStackTrace) printStackTrace(testResult);
            if (printStdOut) printStdOut(testResult);
            if (printStdErr) printStdErr(testResult);
        }
    }

    private static boolean hasOutput(Utf8RecodingDeferredFileOutputStream output) {
        return output != null && output.getByteCount() > 0;
    }

    private void printSkipped(Node node, WrappedReportEntry testResult) {
        printTestResult(buffer()
                .warning(theme.skipped() + getTestName(testResult))
                .warning(getSkippedMessage(testResult)), node, testResult);
    }

    private String getTestName(WrappedReportEntry testResult) {
        if (!isBlank(testResult.getReportName())) {
            return abbreviateName(testResult.getReportName());
        }
        String reportSourceName = testResult.getReportSourceName();
        if (reportSourceName != null && reportSourceName.equals(SourceName.of(testResult))) {
            // A class that never started has no display name, from 3.6.0 on it comes as the binary class name,
            // so print the simple name as JUnit5 and maven-surefire-plugin 3.5.3 and older do.
            return reportSourceName.substring(Math.max(reportSourceName.lastIndexOf('$'), reportSourceName.lastIndexOf('.')) + 1);
        }
        return reportSourceName;
    }

    private String getSkippedMessage(WrappedReportEntry testResult) {
        String message = testResult.getMessage();
        if (isBlank(message)) {
            // An aborted test (failed assumption, TestAbortedException) is reported as skipped without a message,
            // the reason is only on its throwable.
            message = getThrowableMessage(testResult);
        }
        return isBlank(message) ? "" : " (" + message + ")";
    }

    private static String getThrowableMessage(WrappedReportEntry testResult) {
        StackTraceWriter stackTraceWriter = testResult.getStackTraceWriter();
        if (stackTraceWriter == null) {
            return null;
        }
        SafeThrowable throwable = stackTraceWriter.getThrowable();
        return throwable == null ? null : throwable.getLocalizedMessage();
    }

    private void printPreambleDetails(WrappedReportEntry testResult) {
        if (testResult.isSucceeded()) {
            println(buffer().success(theme.details()).success(getTestName(testResult)).toString());
        } else {
            println(buffer().failure(theme.details()).failure(getTestName(testResult)).toString());
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
                .failure(theme.failed() + getTestName(testResult)), node, testResult);
    }
}
