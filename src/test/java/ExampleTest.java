import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("My main test class")
class ExampleTest {

    @Test
    @DisplayName("Should pass")
    void main() {
    }

    @Test
    @DisplayName("Should pass again")
    void main2() {
    }

    @Nested
    @DisplayName("My inner test class")
    class Teste {

        @Test
        @Disabled("supposed to fail")
        @DisplayName("My first inner test should not pass")
        void test() {
            assertTrue(false);
        }

        @Test
        @Disabled("for demonstration purposes")
        @DisplayName("My second inner test should be skipped")
        void test2() {

        }
    }

    @Test
    @DisplayName("Should pass for the 3rd time")
    void main3() {
    }

    @Test
    @DisplayName("Should pass for the 4th time")
    void main4() {
    }
}