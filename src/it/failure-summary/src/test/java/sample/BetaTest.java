package sample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Beta, failures in the class and in a nested class")
class BetaTest {

    @Test
    @DisplayName("passes")
    void passes() {
    }

    @Test
    @DisplayName("fails in the class")
    void failsInTheClass() {
        assertEquals(1, 2);
    }

    @Nested
    @DisplayName("Inner")
    class Inner {

        @Test
        @DisplayName("passes in the nested class")
        void passes() {
        }

        @Test
        @DisplayName("fails in the nested class")
        void fails() {
            assertEquals("a", "b");
        }
    }
}
