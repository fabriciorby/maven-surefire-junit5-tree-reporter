package sample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Parameterized Sample")
class ParameterizedSampleTest {

    // int arguments instead of @CsvSource: JUnit 6 quotes text arguments in display names, ints print the same on 5 and 6
    @ParameterizedTest(name = "{0} + {1} = {2}")
    @MethodSource("additions")
    void add(int first, int second, int expected) {
        assertEquals(expected, first + second);
    }

    static Stream<Arguments> additions() {
        return Stream.of(Arguments.of(0, 1, 1), Arguments.of(1, 2, 3), Arguments.of(49, 51, 100));
    }

    @TestFactory
    @DisplayName("Dynamic")
    Collection<DynamicTest> dynamic() {
        return Arrays.asList(
                DynamicTest.dynamicTest("Add test", () -> assertEquals(2, 1 + 1)),
                DynamicTest.dynamicTest("Multiply test", () -> assertEquals(4, 2 * 2)));
    }
}
