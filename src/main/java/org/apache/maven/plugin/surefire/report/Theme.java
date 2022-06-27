package org.apache.maven.plugin.surefire.report;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

//Thanks to https://github.com/junit-team/junit5/blob/main/junit-platform-console/src/main/java/org/junit/platform/console/options/Theme.java

public enum Theme {

    /**
     * ASCII 7-bit characters form the tree branch.
     *
     * <p>Example test plan execution tree:
     * <pre class="code">
     * [INFO] +--Nested Sample
     * [INFO] |  +-- [OK] Should pass - 0.03s
     * [INFO] |  '-- [OK] Should pass2 - 0.002s
     * [INFO] +--,--Inner Test
     * [INFO] |  |  '-- [OK] Inner test should pass - 0.001s
     * [INFO] |  '--,--Inner Inner Test
     * [INFO] |     |  '-- [OK] Inner Inner Test should pass - 0.002s
     * [INFO] |     '-----Inner Inner Inner Test
     * [INFO] |           '-- [OK] Inner Inner Inner Test should pass - 0.002s
     * [INFO] +--My main test class
     * [INFO] |  +-- [OK] Should pass - 0.001s
     * [INFO] |  +-- [OK] Should pass again - 0.001s
     * [INFO] |  +-- [OK] Should pass for the 3rd time - 0.001s
     * [INFO] |  '-- [OK] Should pass for the 4th time - 0s
     * </pre>
     */
    ASCII("|  ", "+--", "'--", ".--", "---", " [OK] ", " [XX] ", " [??] "),

    /**
     * Unicode (extended ASCII) characters are used to display the test execution tree.
     *
     * <p>Example test plan execution tree:
     * <pre class="code">
     * [INFO] ├─ Nested Sample
     * [INFO] │  ├─ ✔ Should pass - 0.013s
     * [INFO] │  └─ ✔ Should pass2 - 0.001s
     * [INFO] ├─ ┬─ Inner Test
     * [INFO] │  │  └─ ✔ Inner test should pass - 0.001s
     * [INFO] │  └─ ┬─ Inner Inner Test
     * [INFO] │     │  └─ ✔ Inner Inner Test should pass - 0.001s
     * [INFO] │     └─ ── Inner Inner Inner Test
     * [INFO] │           └─ ✔ Inner Inner Inner Test should pass - 0s
     * [INFO] ├─ My main test class
     * [INFO] │  ├─ ✔ Should pass - 0.001s
     * [INFO] │  ├─ ✔ Should pass again - 0.001s
     * [INFO] │  ├─ ✔ Should pass for the 3rd time - 0s
     * [INFO] │  └─ ✔ Should pass for the 4th time - 0s
     * </pre>
     */
    UNICODE("│  ", "├─ ", "└─ ", "┬─ ", "── ", "✔ ", "✘ ", "↷ ");

    public static Theme valueOf(Charset charset) {
        if (StandardCharsets.UTF_8.equals(charset)) {
            return UNICODE;
        }
        return ASCII;
    }

    private final String[] tiles;

    Theme(String... tiles) {
        this.tiles = tiles;
    }

    public final String blank() {
        return "   ";
    }

    public final String pipe() {
        return tiles[0];
    }

    public final String entry() {
        return tiles[1];
    }

    public final String end() {
        return tiles[2];
    }

    public final String down() {
        return tiles[3];
    }

    public final String dash() {
        return tiles[4];
    }

    public final String successful() {
        return tiles[5];
    }

    public final String failed() {
        return tiles[6];
    }

    public final String skipped() {
        return tiles[7];
    }

    /**
     * Return lower case {@link #name()} for easier usage in help text for
     * available options.
     */
    @Override
    public final String toString() {
        return name().toLowerCase();
    }
}
