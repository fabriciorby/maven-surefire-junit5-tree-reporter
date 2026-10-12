// Shared by every scenario's verify.groovy: compares the tree printed in build.log with the golden file.
//
// Variables set by the maven-invoker-plugin: basedir, itSourceDir, surefireVersion, snapshotUpdate.
// Run with -Dsnapshot.update=true to write the current tree as the new golden file.

def lines = new File(basedir, 'build.log').getText('UTF-8').readLines()

def start = lines.findIndexOf { it.contains('T E S T S') }
assert start >= 0: "no surefire test output in ${basedir}/build.log"
def end = lines.findIndexOf(start) { it ==~ /\[(INFO|WARNING|ERROR)\] Results:/ }
assert end > start: "no surefire results in ${basedir}/build.log"

def actual = lines.subList(start + 2, end) // skips the dashes below T E S T S
        .findAll { !it.startsWith('Picked up ') } // JVM notice about JAVA_TOOL_OPTIONS and friends
        .collect { it.replaceAll(/\u001B\[[;\d]*m/, '') }
        .collect { it.replaceFirst(/^\[(INFO|WARNING|ERROR)\] ?/, '') }
        .collect { it.replaceFirst(/ - [\d.,]+ (s|ms|min)$/, ' - <time>') }
        .collect { it.replaceFirst(/\s+$/, '') }
        .join('\n').trim() + '\n'

// expected-output-<surefire version>.txt wins over expected-output.txt when the tree differs between versions
def scenarioDir = new File(itSourceDir, basedir.name)
def golden = new File(scenarioDir, "expected-output-${surefireVersion}.txt")
if (!golden.exists()) golden = new File(scenarioDir, 'expected-output.txt')

if (Boolean.parseBoolean(snapshotUpdate)) {
    golden.setText(actual, 'UTF-8')
    println "Updated ${golden}"
}
assert golden.exists(): "golden file missing, run with -Dsnapshot.update=true to create ${golden}"

def expected = golden.getText('UTF-8').replace('\r\n', '\n')
if (actual != expected) {
    throw new AssertionError("Tree printed with surefire ${surefireVersion} differs from ${golden}, " +
            "run with -Dsnapshot.update=true to accept it\n" + diff(expected.readLines(), actual.readLines()))
}

// Line diff based on the longest common subsequence, "-" is only in the golden file and "+" only in the build
static String diff(List<String> expected, List<String> actual) {
    int[][] lcs = new int[expected.size() + 1][actual.size() + 1]
    for (int i = expected.size() - 1; i >= 0; i--) {
        for (int j = actual.size() - 1; j >= 0; j--) {
            lcs[i][j] = expected[i] == actual[j] ? lcs[i + 1][j + 1] + 1 : Math.max(lcs[i + 1][j], lcs[i][j + 1])
        }
    }
    def out = []
    int i = 0, j = 0
    while (i < expected.size() || j < actual.size()) {
        if (i < expected.size() && j < actual.size() && expected[i] == actual[j]) {
            out << "  ${expected[i++]}"; j++
        } else if (j < actual.size() && (i == expected.size() || lcs[i][j + 1] >= lcs[i + 1][j])) {
            out << "+ ${actual[j++]}"
        } else {
            out << "- ${expected[i++]}"
        }
    }
    out.join('\n')
}
