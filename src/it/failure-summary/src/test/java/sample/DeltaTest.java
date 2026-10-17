package sample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DeltaTest {

    @Test
    @DisplayName("passes")
    void passes() {
    }

    @Nested
    @DisplayName("Inner")
    class Inner {

        @Test
        @DisplayName("passes in the nested class")
        void passes() {
        }
    }
}
