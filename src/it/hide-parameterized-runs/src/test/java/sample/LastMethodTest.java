package sample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Last Method Sample")
class LastMethodTest {

    @Test
    @DisplayName("Only plain test before")
    void aPlain() {
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void bParameterizedAtTheEnd(int n) {
    }
}
