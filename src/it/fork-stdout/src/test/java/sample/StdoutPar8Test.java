package sample;
import org.junit.jupiter.api.*;
class StdoutPar8Test {
    @AfterAll static void together() { Barrier.await(StdoutPar8Test.class); }
    @Test void prints() {
        for (int line = 1; line <= 10; line++) System.out.println("StdoutPar8Test out " + line);
    }
}
