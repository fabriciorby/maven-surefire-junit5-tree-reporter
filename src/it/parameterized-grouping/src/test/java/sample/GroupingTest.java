package sample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@DisplayName("Grouping Sample")
class GroupingTest {

    @Test
    @DisplayName("A plain test")
    void aPlain() {
    }

    @ParameterizedTest
    @CsvSource({"1, 2, 3", "2, 3, 6", "49, 51, 100"})
    void bAdd(int a, int b, int sum) {
        assertEquals(sum, a + b);
    }

    @ParameterizedTest(name = "{0} is positive")
    @ValueSource(ints = {1, 2})
    void cOverloaded(int n) {
    }

    @ParameterizedTest(name = "{0} is not empty")
    @ValueSource(strings = {"a", "b"})
    void cOverloaded(String s) {
    }

    @DisplayName("Displayed method")
    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void dDisplayed(int n) {
        assumeTrue(n != 2, "two is skipped");
    }

    @RepeatedTest(2)
    void eRepeated() {
    }

    @RepeatedTest(2)
    void fRepeatedWithInfo(RepetitionInfo info) {
    }

    @TestFactory
    Collection<DynamicTest> gFactory() {
        return Arrays.asList(
                DynamicTest.dynamicTest("first dynamic test", () -> {
                }),
                DynamicTest.dynamicTest("second dynamic test", () -> {
                }));
    }

    @Test
    @DisplayName("foo[1]")
    void hLooksLikeAnInvocation() {
    }

    @Test
    @DisplayName("Last plain test")
    void iPlainAtTheEnd() {
    }

    @Nested
    @DisplayName("Inner")
    class InnerTest {

        @ParameterizedTest
        @ValueSource(ints = {1, 2})
        void innerParameterized(int n) {
        }

        @Test
        @DisplayName("Inner plain test")
        void innerPlain() {
        }

        @ParameterizedTest
        @ValueSource(strings = {"x", "y"})
        void zInnerParameterizedAtTheEnd(String s) {
            assertEquals("x", s);
        }
    }
}
