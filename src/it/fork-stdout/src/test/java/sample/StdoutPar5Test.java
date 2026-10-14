package sample;
import org.junit.jupiter.api.*;
class StdoutPar5Test {
    @AfterAll static void together() { Barrier.await(StdoutPar5Test.class); }
    @Test void prints() {
        for (int line = 1; line <= 10; line++) System.out.println("StdoutPar5Test out " + line);
    }
}
