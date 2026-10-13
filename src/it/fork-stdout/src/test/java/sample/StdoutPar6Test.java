package sample;
import org.junit.jupiter.api.*;
class StdoutPar6Test {
    @AfterAll static void together() { Barrier.await(StdoutPar6Test.class); }
    @Test void prints() {
        for (int line = 1; line <= 10; line++) System.out.println("StdoutPar6Test out " + line);
    }
}
