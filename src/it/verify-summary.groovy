// Shared by the scenarios that print the failure summary when the run ends. Run after verify-tree.groovy, which only
// looks at the lines up to "Results:".
//
// Checks the "Failed tests" block of build.log (after the "Tests run:" totals) against expected-summary.txt. It must
// have been printed once, not once per fork.
//
// A scenario that runs the tests more than once in the same build sets executions: the summary must then be printed
// once per execution, each equal to the golden file, as the state of one execution must not leak into the next one.
//
// A scenario that prints the same summary as another one sets summaryScenario to use that scenario's golden file.
//
// Variables set by the maven-invoker-plugin: basedir, itSourceDir, surefireVersion, snapshotUpdate.
// A scenario whose classes complete in any order sets sortSummary: the failed tests are compared in name order.
// Run with -Dsnapshot.update=true to write the current output as the new golden files.

def sorted = binding.hasVariable('sortSummary') && binding.getVariable('sortSummary')
def expectedRuns = binding.hasVariable('executions') ? binding.getVariable('executions') as int : 1
def scenarioDir = new File(itSourceDir, binding.hasVariable('summaryScenario') ? binding.getVariable('summaryScenario') : basedir.name)

// ---- the block printed to the console
def lines = new File(basedir, 'build.log').getText('UTF-8').readLines()
        .collect { it.replaceAll(/\u001B\[[;\d]*m/, '') }
        .collect { it.replaceFirst(/^\[(INFO|WARNING|ERROR)\] ?/, '') }
        .collect { it.replaceFirst(/\s+$/, '') }
def starts = lines.findIndexValues { it ==~ /Failed tests \(\d+\):/ }
assert starts.size() == expectedRuns: "expected ${expectedRuns} failure summaries in ${basedir}/build.log, found ${starts.size()}"
def blocks = starts.collect { start ->
    assert lines.findIndexOf { it.startsWith('Tests run:') } in 0..<start: "the failure summary is not after the 'Tests run:' totals"
    def block = lines.drop(start as int).takeWhile { it ==~ /Failed tests \(\d+\):/ || it.startsWith('  ') }
    if (sorted) block = [block[0]] + block.drop(1).sort()
    block.join('\n') + '\n'
}
compare(new File(scenarioDir, 'expected-summary.txt'), blocks[0])
blocks.each { assert it == blocks[0]: "the failure summaries of the executions differ:\n${blocks.join('\n')}" }

return true // the invoker takes any other value, like the list above, for a failed script

def compare(File golden, String actual) {
    if (Boolean.parseBoolean(snapshotUpdate)) {
        golden.setText(actual, 'UTF-8')
        println "Updated ${golden}"
    }
    assert golden.exists(): "golden file missing, run with -Dsnapshot.update=true to create ${golden}"
    def expected = golden.getText('UTF-8').replace('\r\n', '\n')
    if (actual != expected) {
        throw new AssertionError("${golden.name} differs from what surefire ${surefireVersion} printed, " +
                "run with -Dsnapshot.update=true to accept it\n" + diff(expected.readLines(), actual.readLines()))
    }
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
