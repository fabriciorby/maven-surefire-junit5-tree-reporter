package sample;
import org.junit.jupiter.api.*;
class Par6Test {
    static void nap() { try { Thread.sleep((long) (Math.random() * 30)); } catch (InterruptedException e) { } }
    @AfterAll static void together() { Barrier.await(Par6Test.class); }
    @Test void a() { nap(); }
    @Test void b() { nap(); }
    @Nested class Inner {
        @Test void c() { nap(); }
        @Nested class Deeper {
            @Test void d() { nap(); }
            @Test void e() { nap(); Assertions.fail("boom"); }
        }
    }
    @Nested class Other { @Test void f() { nap(); } }
}
