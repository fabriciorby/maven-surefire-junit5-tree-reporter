package sample;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

// The threshold is 250ms and the slow tests sleep for 1s, so a loaded CI machine does not make the instant tests slow
// nor the sleeping tests fast.
@DisplayName("Slow Sample")
class SlowTest {

    @Test
    @DisplayName("Should be instant")
    void test1() {
    }

    @Test
    @DisplayName("Should be slow")
    void test2() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Test
    @DisplayName("Should be slow and fail")
    void test3() throws InterruptedException {
        Thread.sleep(1000);
        fail("expected failure");
    }

    @Test
    @Disabled("not run, so never slow")
    @DisplayName("Should be skipped")
    void test4() {
    }

    @Nested
    @DisplayName("Inner Test")
    class InnerTest {

        @Test
        @DisplayName("Should be slow in a nested class")
        void test1() throws InterruptedException {
            Thread.sleep(1000);
        }

        @Test
        @DisplayName("Should be instant in a nested class")
        void test2() {
        }
    }
}
