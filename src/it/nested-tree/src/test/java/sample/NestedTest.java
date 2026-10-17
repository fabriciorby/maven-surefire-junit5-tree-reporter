package sample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Nested Sample")
class NestedTest {

    @Test
    @DisplayName("Should pass")
    void test1() {
    }

    @Test
    @DisplayName("Should pass again")
    void test2() {
    }

    @Nested
    @DisplayName("First Inner Test")
    class FirstInnerTest {

        @Test
        @DisplayName("First inner test should pass")
        void test() {
        }
    }

    @Nested
    @DisplayName("Second Inner Test")
    class SecondInnerTest {

        @Test
        @DisplayName("Second inner test should pass")
        void test() {
        }

        @Nested
        @DisplayName("Inner Inner Test")
        class InnerInnerTest {

            @Test
            @DisplayName("Inner inner test should pass")
            void test() {
            }

            @Nested
            @DisplayName("Inner Inner Inner Test")
            class InnerInnerInnerTest {

                @Test
                @DisplayName("Inner inner inner test should pass")
                void test() {
                }
            }

            @Nested
            @DisplayName("Sibling Inner Inner Inner Test")
            class SiblingInnerInnerInnerTest {

                @Test
                @DisplayName("Sibling inner inner inner test should pass")
                void test() {
                }
            }
        }

        @Nested
        @DisplayName("Sibling Inner Inner Test")
        class SiblingInnerInnerTest {

            @Test
            @DisplayName("Sibling inner inner test should pass")
            void test() {
            }
        }
    }
}
