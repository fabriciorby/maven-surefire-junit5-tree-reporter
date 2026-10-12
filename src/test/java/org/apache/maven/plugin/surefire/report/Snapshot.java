package org.apache.maven.plugin.surefire.report;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Compares the printed tree with a golden file in {@code src/test/resources/snapshots}.
 * <p>
 * Run with {@code -Dsnapshot.update=true} to write the current output as the new golden file,
 * then review the change with {@code git diff}.
 */
final class Snapshot {

    static final String UPDATE_PROPERTY = "snapshot.update";

    private static final Pattern ANSI = Pattern.compile("\u001B\\[[;\\d]*m");
    private static final Pattern ELAPSED_TIME = Pattern.compile(" - [\\d.,]+ (s|ms|min)$", Pattern.MULTILINE);

    private Snapshot() {
    }

    static void assertMatches(String name, List<String> lines) {
        String actual = normalize(lines.stream().collect(joining("\n", "", "\n")));
        Path golden = Paths.get(System.getProperty("basedir", "."), "src", "test", "resources", "snapshots", name + ".txt");
        try {
            if (Boolean.getBoolean(UPDATE_PROPERTY)) {
                Files.createDirectories(golden.getParent());
                Files.write(golden, actual.getBytes(UTF_8));
            }
            assertThat(golden)
                    .as("golden file missing, run with -D%s=true to create it", UPDATE_PROPERTY)
                    .exists();
            String expected = new String(Files.readAllBytes(golden), UTF_8).replace("\r\n", "\n");
            assertThat(actual)
                    .as("tree differs from %s, run with -D%s=true to update it", golden, UPDATE_PROPERTY)
                    .isEqualTo(expected);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static String normalize(String output) {
        String withoutColors = ANSI.matcher(output.replace("\r\n", "\n")).replaceAll("");
        return ELAPSED_TIME.matcher(withoutColors).replaceAll(" - <time>");
    }
}
