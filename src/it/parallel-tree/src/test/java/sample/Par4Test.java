package sample;
import org.junit.jupiter.api.*;
class Par4Test {
    static void nap() { try { Thread.sleep((long) (Math.random() * 30)); } catch (InterruptedException e) { } }
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
