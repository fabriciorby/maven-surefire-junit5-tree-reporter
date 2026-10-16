package sample;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Gamma, an error, a failure and a skip")
class GammaTest {

    @Test
    @DisplayName("throws an error")
    void error() {
        throw new IllegalStateException("boom");
    }

    @Test
    @DisplayName("fails")
    void fails() {
        fail("on purpose");
    }

    @Test
    @Disabled
    @DisplayName("is skipped")
    void skipped() {
    }
}
