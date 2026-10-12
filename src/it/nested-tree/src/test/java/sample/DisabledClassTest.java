package sample;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@Disabled("whole class disabled")
@DisplayName("Disabled Sample")
class DisabledClassTest {

    @Test
    void test() {
    }

    @Nested
    class InnerTest {

        @Test
        void test() {
        }
    }
}
