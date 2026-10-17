package sample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("All Green")
class AllGreenTest {

    @Test
    @DisplayName("Passes one")
    void test1() {
    }

    @Test
    @DisplayName("Passes two")
    void test2() {
    }

    @Nested
    @DisplayName("Green Inner")
    class Inner {

        @Test
        @DisplayName("Inner passes")
        void test() {
        }
    }
}
