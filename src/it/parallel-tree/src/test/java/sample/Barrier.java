package sample;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Makes test classes that run in different JVMs (forks) complete at the same time, which is when their trees can get
 * mixed. A class calls {@link #await} in {@code @AfterAll} and returns once {@code barrier.parties} classes of the
 * same build arrived (each leaves a file in {@code target/barrier}). Without {@code barrier.parties} it returns at once.
 * <p>
 * It fails the class when the others do not arrive, instead of passing without the overlap it exists to create.
 */
final class Barrier {

    private static final long TIMEOUT_SECONDS = 20;

    private Barrier() {
    }

    static void await(Class<?> testClass) {
        int parties = Integer.getInteger("barrier.parties", 1);
        if (parties <= 1) return;
        String run = System.getProperty("barrier.run", "") + "-";
        File dir = new File(System.getProperty("basedir", "."), "target/barrier");
        try {
            dir.mkdirs();
            new File(dir, run + testClass.getSimpleName()).createNewFile();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS);
            while (arrived(dir, run) < parties) {
                if (System.nanoTime() > deadline) {
                    throw new AssertionError("only " + arrived(dir, run) + " of " + parties + " classes arrived");
                }
                Thread.sleep(5);
            }
        } catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    private static int arrived(File dir, String run) {
        String[] names = dir.list((d, name) -> name.startsWith(run));
        return names == null ? 0 : names.length;
    }
}
