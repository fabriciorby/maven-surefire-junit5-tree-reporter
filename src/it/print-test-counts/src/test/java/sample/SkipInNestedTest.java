package sample;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Skip In Nested")
class SkipInNestedTest {

    @Test
    @DisplayName("Passes")
    void test1() {
    }

    @Test
    @DisplayName("Fails")
    void test2() {
        fail("expected failure");
    }

    @Nested
    @DisplayName("Green Nested")
    class GreenNested {

        @Test
        @DisplayName("Green passes")
        void test() {
        }
    }

    @Nested
    @DisplayName("Skipped Nested")
    class SkippedNested {

        @Test
        @Disabled("not today")
        @DisplayName("Is skipped")
        void test() {
        }
    }
}
