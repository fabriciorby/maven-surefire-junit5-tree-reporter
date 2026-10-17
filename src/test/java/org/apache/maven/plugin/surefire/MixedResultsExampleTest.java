package org.apache.maven.plugin.surefire;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Every test passes here. The unit tests make some of them report another result with
 * {@code SurefireEmulator.withOutcome}.
 */
@DisplayName("Mixed Sample")
public class MixedResultsExampleTest {

    @Test
    @DisplayName("First")
    void test1() {
    }

    @Test
    @DisplayName("Second")
    void test2() {
    }

    @Test
    @DisplayName("Third")
    void test3() {
    }

    @Nested
    @DisplayName("Green Inner")
    class GreenInner {

        @Test
        @DisplayName("Green test")
        void test() {
        }

        @Nested
        @DisplayName("Green Deep")
        class GreenDeep {

            @Test
            @DisplayName("Green deep test")
            void test() {
            }
        }
    }

    @Nested
    @DisplayName("Red Inner")
    class RedInner {

        @Test
        @DisplayName("Red test")
        void test() {
        }

        @Nested
        @DisplayName("Red Deep")
        class RedDeep {

            @Test
            @DisplayName("Red deep test")
            void test() {
            }
        }
    }

    @Nested
    @DisplayName("Skipped Inner")
    class SkippedInner {

        @Test
        @DisplayName("Skipped test")
        void test() {
        }
    }
}
