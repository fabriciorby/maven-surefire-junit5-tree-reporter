package sample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Fail First")
class FailFirstTest {

    @Test
    @DisplayName("Fails first")
    void test1() {
        fail("expected failure");
    }

    @Test
    @DisplayName("Passes after")
    void test2() {
    }

    @Test
    @DisplayName("Passes last")
    void test3() {
    }
}
