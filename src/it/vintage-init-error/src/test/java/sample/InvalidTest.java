package sample;

import org.junit.Test;

// JUnit4 rejects the class with an InvalidTestClassError, reported as the initializationError test
public class InvalidTest {

    @Test
    public void shouldBeOk(int parameter) {
    }
}
