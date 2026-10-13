package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.NestedExampleTest;
import org.apache.maven.plugin.surefire.ParameterizedExampleTest;
import org.apache.maven.plugin.surefire.VeryNestedExampleTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * With forkCount above 1, surefire reports the events of each fork from its own thread, each fork with its own
 * {@link ConsoleTreeReporter}, all printing to the same console (issue #83).
 */
class ParallelForksTest {

    private static final List<Class<?>> CLASSES =
            Arrays.asList(NestedExampleTest.class, VeryNestedExampleTest.class, ParameterizedExampleTest.class);
    private static final int RUNS_PER_FORK = 200;

    @BeforeEach
    void cleanNode() {
        Node.clearTree();
    }

    @Test
    void printsEachClassInOnePieceWhenForksReportAtTheSameTime() throws Exception {
        Map<String, List<String>> expectedTrees = CLASSES.stream()
                .map(testClass -> normalize(new SurefireEmulator(testClass).run()))
                .collect(toMap(tree -> tree.get(0), Function.identity()));

        CapturingLogger console = new CapturingLogger();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService forks = Executors.newFixedThreadPool(CLASSES.size());
        try {
            List<Future<?>> results = new ArrayList<>();
            for (Class<?> testClass : CLASSES) {
                results.add(forks.submit(() -> {
                    start.await();
                    for (int i = 0; i < RUNS_PER_FORK; i++) {
                        new SurefireEmulator(ReporterOptions.builder().theme(Theme.UNICODE).build(), testClass, console).run();
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> result : results) {
                result.get();
            }
        } finally {
            forks.shutdownNow();
        }

        List<List<String>> printedTrees = splitByTopLevelClass(normalize(console.lines));
        assertThat(printedTrees).hasSize(CLASSES.size() * RUNS_PER_FORK);
        for (List<String> tree : printedTrees) {
            assertThat(tree).isEqualTo(expectedTrees.get(tree.get(0)));
        }
        assertThat(Node.getRoot().branches).isEmpty();
    }

    private static List<String> normalize(List<String> lines) {
        return Arrays.asList(Snapshot.normalize(String.join("\n", lines)).split("\n"));
    }

    private static List<List<String>> splitByTopLevelClass(List<String> lines) {
        List<List<String>> trees = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith("┬─ ") || line.startsWith("── ") || trees.isEmpty()) trees.add(new ArrayList<>());
            trees.get(trees.size() - 1).add(line);
        }
        return trees;
    }
}
