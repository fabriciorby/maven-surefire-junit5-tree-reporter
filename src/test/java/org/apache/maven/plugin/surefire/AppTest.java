package org.apache.maven.plugin.surefire;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Issue #46
 */
@DisplayName( "Outer" )
public class AppTest {

    @Test
    public void testOuter1() {}
    @Test
    public void testOuter2() {}
    @Test
    public void testOuter3() {}

    @Nested
    @DisplayName( "Inner 1" )
    public class FooTest {

        @Test
        public void testInner1() {}
        @Test
        public void testInner2() {}
        @Test
        public void testInner3() {}

    }

    @Nested
    @DisplayName( "Inner 2" )
    public class BarTest {

        @Test
        public void testInner1() {}
        @Test
        public void testInner2() {}
        @Test
        public void testInner3() {}

    }

    @Nested
    @DisplayName( "Inner 3" )
    public class BazTest {

        @Test
        public void testInner1() {}
        @Test
        public void testInner2() {}
        @Test
        public void testInner3() {}

        @Nested
        @DisplayName( "Inner-er 1" )
        public class InnerFooTest {

            @Test
            public void testInnerer1() {}
            @Test
            public void testInnerer2() {}
            @Test
            public void testInnerer3() {}

        }

        @Nested
        @DisplayName( "Inner-er 2" )
        public class InnerBarTest {

            @Test
            public void testInnerer1() {}
            @Test
            public void testInnerer2() {}
            @Test
            public void testInnerer3() {}

        }

        @Nested
        @DisplayName( "Inner-er 3" )
        public class InnerBazTest {

            @Test
            public void testInnerer1() {}
            @Test
            public void testInnerer2() {}
            @Test
            public void testInnerer3() {}

        }

    }

}
