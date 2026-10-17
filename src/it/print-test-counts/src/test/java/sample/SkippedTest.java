package sample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Skipped")
class SkippedTest {

    @Test
    @DisplayName("Passes")
    void test1() {
    }

    @Test
    @Disabled("not today")
    @DisplayName("Is skipped")
    void test2() {
    }

    @Nested
    @DisplayName("Quiet Inner")
    class Inner {

        @Test
        @DisplayName("Inner passes")
        void test() {
        }
    }
}
