// Shared by every scenario's verify.groovy: compares the tree printed in build.log with the golden file.
//
// Variables set by the maven-invoker-plugin: basedir, itSourceDir, surefireVersion, junitVersion, snapshotUpdate.
// A scenario that reuses another scenario's tests sets goldenScenario to compare with that scenario's golden files.
// A scenario whose classes run in parallel sets sortClassBlocks, so the trees of its top-level classes are compared in
// name order instead of the order they completed in. Only the UNICODE theme is supported there.
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
        .collect { it.replaceFirst(/ - [\d.,]+( (s|ms|min))?$/, ' - <time>') }
        .collect { it.replaceFirst(/\s+$/, '') }
        .join('\n').trim() + '\n'
if (binding.hasVariable('sortClassBlocks') && sortClassBlocks) actual = sortedByClass(actual)

// expected-output-<surefire version>.txt wins over expected-output.txt when the tree differs between versions
def scenarioDir = new File(itSourceDir, binding.hasVariable('goldenScenario') ? goldenScenario : basedir.name)
def golden = new File(scenarioDir, "expected-output-${surefireVersion}.txt")
if (!golden.exists()) golden = new File(scenarioDir, 'expected-output.txt')

// With JUnit 6 the -junit6 variant of that file wins when it exists, e.g. JUnit 6 quotes the String arguments in the
// names of parameterized tests. A tree printed the same on JUnit 5 and 6 keeps a single file.
def junitMajor = junitVersion.tokenize('.')[0]
def junitGolden = junitMajor == '5' ? null : new File(scenarioDir, golden.name.replace('.txt', "-junit${junitMajor}.txt"))
if (junitGolden?.exists()) golden = junitGolden

if (Boolean.parseBoolean(snapshotUpdate)) {
    // a tree that differs on JUnit 6 goes to the -junit6 file, so the JUnit 5 golden file stays as it is
    if (junitGolden != null && golden != junitGolden && golden.exists() && golden.getText('UTF-8') != actual) {
        golden = junitGolden
    }
    golden.setText(actual, 'UTF-8')
    println "Updated ${golden}"
}
assert golden.exists(): "golden file missing, run with -Dsnapshot.update=true to create ${golden}"

def expected = golden.getText('UTF-8').replace('\r\n', '\n')
if (binding.hasVariable('sortClassBlocks') && sortClassBlocks) expected = sortedByClass(expected)
if (actual != expected) {
    throw new AssertionError("Tree printed with surefire ${surefireVersion} differs from ${golden}, " +
            "run with -Dsnapshot.update=true to accept it\n" + diff(expected.readLines(), actual.readLines()))
}

// Splits the tree in the blocks of its top-level classes, each starting with "┬─ " or "── ", and sorts them
static String sortedByClass(String tree) {
    def blocks = []
    tree.readLines().each { line ->
        if (line ==~ /^(┬─|──) .*/ || blocks.isEmpty()) blocks << []
        blocks.last() << line
    }
    blocks.collect { it.join('\n') }.sort().join('\n') + '\n'
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
