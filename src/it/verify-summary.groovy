// Shared by the scenarios that print the failure summary and write the GitHub job summary when the run ends. Run
// after verify-tree.groovy, which only looks at the lines up to "Results:".
//
// Checks the "Failed tests" block of build.log (after the "Tests run:" totals) against expected-summary.txt, and the
// file the reporter appended to (github-step-summary.md, set by GITHUB_STEP_SUMMARY in invoker.properties) against
// expected-github-summary.md. Both must have been written once, not once per fork.
//
// A scenario that runs the tests more than once in the same build sets executions: the summary must then be printed
// and written once per execution, each equal to the golden files, as the state of one execution must not leak into
// the next one.
//
// A scenario that prints the same summary as another one sets summaryScenario to use that scenario's golden files.
//
// Variables set by the maven-invoker-plugin: basedir, itSourceDir, surefireVersion, snapshotUpdate.
// A scenario whose classes complete in any order sets sortSummary: the failed tests, the classes with failures and
// the classes without are compared in name order.
// Run with -Dsnapshot.update=true to write the current output as the new golden files.

def sorted = binding.hasVariable('sortSummary') && binding.getVariable('sortSummary')
def expectedRuns = binding.hasVariable('executions') ? binding.getVariable('executions') as int : 1
def scenarioDir = new File(itSourceDir, binding.hasVariable('summaryScenario') ? binding.getVariable('summaryScenario') : basedir.name)

def stripTimes = { String line -> line.replaceAll(/ - [\d.,]+( (s|ms|min))?$/, ' - <time>') }

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

// ---- the markdown appended to the GitHub job summary
def github = new File(basedir, 'github-step-summary.md')
assert github.exists(): "the reporter did not write ${github}"
def all = github.getText('UTF-8').readLines().collect(stripTimes).join('\n') + '\n'
def sections = all.split(/(?m)^(?=### Test results$)/).findAll { it }
assert sections.size() == expectedRuns: "expected ${expectedRuns} sections in the GitHub job summary, found ${sections.size()}"
sections = sections.collect { md ->
    if (sorted) {
        def details = (md =~ /(?s)<details>.*?<\/details>\n\n/).collect { it }.sort()
        def passed = (md =~ /(?m)^Classes without failures: (.*)$/).collect { it[1].split(/(?<=`), (?=`)/).sort().join(', ') }
        def header = md.substring(0, md.indexOf('<details>'))
        md = header + details.join('') + passed.collect { "Classes without failures: ${it}\n\n" }.join('')
    }
    md
}
compare(new File(scenarioDir, 'expected-github-summary.md'), sections[0])
sections.each { assert it == sections[0]: "the sections of the GitHub job summary differ:\n${sections.join('\n')}" }

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
