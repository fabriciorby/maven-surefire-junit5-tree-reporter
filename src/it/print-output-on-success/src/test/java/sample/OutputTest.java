package sample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// printStdoutOnSuccess and printStderrOnSuccess must only show the output of the tests that wrote something (#48)
@DisplayName("Output Sample")
class OutputTest {

    @Test
    @DisplayName("Should print nothing")
    void silent() {
    }

    @Test
    @DisplayName("Should print to stdout")
    void stdout() {
        System.out.println("hello from stdout");
    }
}
