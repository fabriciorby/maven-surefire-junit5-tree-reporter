package sample;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

@DisplayName("Aborted Sample")
class AbortedTest {

    @Test
    @DisplayName("Should pass")
    void passes() {
    }

    @Test
    @DisplayName("Assumption without message")
    void assumptionWithoutMessage() {
        assumeTrue(false);
    }

    @Test
    @DisplayName("Assumption with message")
    void assumptionWithMessage() {
        assumeTrue(false, "not running on this OS");
    }

    @Test
    @DisplayName("Aborted exception")
    void abortedException() {
        throw new TestAbortedException("aborted by hand");
    }

    @Test
    @Disabled("not ready yet")
    @DisplayName("Disabled test")
    void disabled() {
    }
}
