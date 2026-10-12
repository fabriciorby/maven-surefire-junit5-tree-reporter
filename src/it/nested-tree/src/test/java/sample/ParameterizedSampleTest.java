package sample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Parameterized Sample")
class ParameterizedSampleTest {

    @ParameterizedTest(name = "{0} + {1} = {2}")
    @CsvSource({"0, 1, 1", "1, 2, 3", "49, 51, 100"})
    void add(int first, int second, int expected) {
        assertEquals(expected, first + second);
    }

    @TestFactory
    @DisplayName("Dynamic")
    Collection<DynamicTest> dynamic() {
        return Arrays.asList(
                DynamicTest.dynamicTest("Add test", () -> assertEquals(2, 1 + 1)),
                DynamicTest.dynamicTest("Multiply test", () -> assertEquals(4, 2 * 2)));
    }
}
