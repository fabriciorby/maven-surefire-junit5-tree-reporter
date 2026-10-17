package sample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Fail In Middle")
class FailInMiddleTest {

    @Test
    @DisplayName("Top passes")
    void test1() {
    }

    @Nested
    @DisplayName("Green Before")
    class Inner1 {

        @Test
        @DisplayName("Before passes")
        void test() {
        }
    }

    @Nested
    @DisplayName("Red Middle")
    class Inner2 {

        @Test
        @DisplayName("Middle passes")
        void test1() {
        }

        @Test
        @DisplayName("Middle fails")
        void test2() {
            fail("expected failure");
        }

        @Nested
        @DisplayName("Deep Green")
        class DeepGreen {

            @Test
            @DisplayName("Deep green passes")
            void test() {
            }
        }

        @Nested
        @DisplayName("Deep Red")
        class DeepRed {

            @Test
            @DisplayName("Deep red errors")
            void test() {
                throw new IllegalStateException("expected error");
            }

            @Test
            @DisplayName("Deep red passes")
            void test2() {
            }
        }
    }

    @Nested
    @DisplayName("Green After")
    class Inner3 {

        @Test
        @DisplayName("After passes")
        void test() {
        }
    }
}
