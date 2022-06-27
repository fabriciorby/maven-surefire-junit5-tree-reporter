import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Nested Sample")
public class NestedExampleTest {

    @Test
    @DisplayName("Should pass")
    void test() {
    }

    @Nested
    @DisplayName("Inner Test")
    class InnerTest {

        @Test
        @DisplayName("Inner test should pass")
        void test() {
        }

        @Nested
        @DisplayName("Inner Inner Test")
        class InnerInnerTest {

            @Test
            @DisplayName("Inner Inner Test should pass")
            void test() {
            }

            @Nested
            @DisplayName("Inner Inner Inner Test")
            class InnerInnerInnerTest {

                @Test
                @DisplayName("Inner Inner Inner Test should pass")
                void test() {
                }

            }

        }

    }

    @Test
    @DisplayName("Should pass2")
    void test2() {
    }

}
