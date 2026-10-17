package sample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Fail Last")
class FailLastTest {

    @Test
    @DisplayName("Passes first")
    void test1() {
    }

    @Test
    @DisplayName("Passes second")
    void test2() {
    }

    @Test
    @DisplayName("Fails last")
    void test3() {
        fail("expected failure");
    }

    @Nested
    @DisplayName("Inner Last")
    class Inner {

        @Test
        @DisplayName("Inner passes")
        void test() {
        }
    }
}
