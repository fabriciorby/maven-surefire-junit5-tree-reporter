package sample;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Results Sample")
class ResultsTest {

    @Test
    @DisplayName("Should pass")
    void test1() {
    }

    @Test
    @DisplayName("Should fail")
    void test2() {
        fail("expected failure");
    }

    @Test
    @DisplayName("Should be an error")
    void test3() {
        throw new IllegalStateException("expected error");
    }

    @Test
    @Disabled("for demonstration purposes")
    @DisplayName("Should be skipped")
    void test4() {
    }

    @Nested
    @DisplayName("Inner Results")
    class InnerTest {

        @Test
        @DisplayName("Inner test should fail")
        void test() {
            fail("expected failure");
        }
    }
}
