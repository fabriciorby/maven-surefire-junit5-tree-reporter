package org.apache.maven.plugin.surefire;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Very Nested Sample")
public class VeryNestedExampleTest {

    @Nested
    @DisplayName("Inner Test")
    class InnerTest {

        @Test
        @DisplayName("Inner test should pass")
        void test() throws InterruptedException {
            Thread.sleep(200);
        }

        @Nested
        @DisplayName("Inner Inner Test")
        class InnerInnerTest {

            @Test
            @DisplayName("Inner Inner Test should pass")
            void test() throws InterruptedException {
                Thread.sleep(300);
            }

            @Nested
            @DisplayName("Inner Inner Inner Test")
            class InnerInnerInnerTest {

                @Test
                @DisplayName("Inner Inner Inner Test should pass")
                void test() throws InterruptedException {
                    Thread.sleep(400);
                }

            }

        }
    }

    @Test
    @DisplayName("Should pass")
    void test() throws InterruptedException {
        Thread.sleep(100);
    }

    @Nested
    @DisplayName("First Inner Test")
    class FirstInnerTest {
        @Test
        @DisplayName("FirstInnerTest should show up")
        void test() throws InterruptedException {
            Thread.sleep(600);
        }
    }

    @Nested
    @DisplayName("Second Inner Test")
    class SecondInnerTest {

        @Test
        @DisplayName("Inner test should pass")
        void test() throws InterruptedException {
            Thread.sleep(200);
        }

        @Nested
        @DisplayName("Inner Inner Test")
        class InnerInnerTest {

            @Test
            @DisplayName("Inner Inner Test should pass")
            void test() throws InterruptedException {
                Thread.sleep(300);
            }

            @Nested
            @DisplayName("Inner Inner Inner Test")
            class InnerInnerInnerTest {

                @Test
                @DisplayName("Inner Inner Inner Test should pass")
                void test() throws InterruptedException {
                    Thread.sleep(400);
                }

            }

            @Nested
            @DisplayName("Sibling Inner Inner Inner Test")
            class SiblingInnerInnerInnerTest {

                @Test
                @DisplayName("Sibling Inner Inner Inner Test should pass")
                void test() throws InterruptedException {
                    Thread.sleep(300);
                }

            }

        }

        @Nested
        @DisplayName("Sibling Inner Inner Test")
        class SiblingInnerInnerTest {

            @Test
            @DisplayName("Sibling Inner Inner Test should pass")
            void test() throws InterruptedException {
                Thread.sleep(200);
            }

        }

        @Test
        @DisplayName("Second Inner test should pass")
        void test2() throws InterruptedException {
            Thread.sleep(200);
        }

    }

    @Test
    @DisplayName("Should pass2")
    void test2() throws InterruptedException {
        Thread.sleep(500);
    }

}
