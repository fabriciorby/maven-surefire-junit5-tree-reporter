package sample;
import org.junit.jupiter.api.*;
class StdoutPar4Test {
    @AfterAll static void together() { Barrier.await(StdoutPar4Test.class); }
    @Test void prints() {
        for (int line = 1; line <= 10; line++) System.out.println("StdoutPar4Test out " + line);
    }
}
