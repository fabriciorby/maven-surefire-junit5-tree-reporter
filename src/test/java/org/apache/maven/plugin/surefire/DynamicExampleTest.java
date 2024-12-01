package org.apache.maven.plugin.surefire;

import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DynamicExampleTest {

    Collection<DynamicTest> tests = Arrays.asList(
            DynamicTest.dynamicTest("Add test",
                    () -> assertEquals(2, Math.addExact(1, 1))),
            DynamicTest.dynamicTest("Multiply Test",
                    () -> assertEquals(4, Math.multiplyExact(2, 2))));

    @TestFactory
    Collection<DynamicTest> dynamicTestsWithCollection() {
        return tests;
    }

    @TestFactory
    @DisplayName("Calculating")
    Collection<DynamicTest> dynamicTestsWithCollectionWithDisplayName() {
        return tests;
    }

}
