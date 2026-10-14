package sample;
import org.junit.jupiter.api.*;
class StdoutPar2Test {
    @AfterAll static void together() { Barrier.await(StdoutPar2Test.class); }
    @Test void prints() {
        for (int line = 1; line <= 10; line++) System.out.println("StdoutPar2Test out " + line);
    }
}
