package org.apache.maven.plugin.surefire;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Nested classes with siblings below the second level, to check the connectors that go down along them.
 */
@DisplayName("Siblings Sample")
public class SiblingsExampleTest {

    @Test
    @DisplayName("Top test")
    void test() {
    }

    @Nested
    @DisplayName("Left")
    class First {

        @Test
        @DisplayName("First test")
        void test() {
        }

        @Nested
        @DisplayName("Apple")
        class FirstA {

            @Test
            @DisplayName("First A test")
            void test() {
            }

            @Nested
            @DisplayName("Cherry")
            class FirstA1 {

                @Test
                @DisplayName("First A1 test")
                void test() {
                }
            }

            @Nested
            @DisplayName("Plum")
            class FirstA2 {

                @Test
                @DisplayName("First A2 test")
                void test() {
                }
            }
        }

        @Nested
        @DisplayName("Banana")
        class FirstB {

            @Test
            @DisplayName("First B test")
            void test() {
            }
        }
    }

    @Nested
    @DisplayName("Right")
    class Second {

        @Test
        @DisplayName("Second test")
        void test() {
        }
    }
}
