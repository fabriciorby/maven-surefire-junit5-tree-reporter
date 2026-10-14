package sample;
import org.junit.jupiter.api.*;
class StdoutPar3Test {
    @AfterAll static void together() { Barrier.await(StdoutPar3Test.class); }
    @Test void prints() {
        for (int line = 1; line <= 10; line++) System.out.println("StdoutPar3Test out " + line);
    }
}
