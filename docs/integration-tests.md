# Testing the printed tree

The reporter's only output is the tree it prints to the console, so the tests assert on that tree, line by line,
against golden files (also called snapshots): text files with the expected tree. When a change alters the output,
a test fails and shows the difference. When the change is intended, one command rewrites the golden files and
`git diff` shows exactly what changed.

There are two layers, and a bug fix should usually get a test in both:

| Layer             | Command                  | What runs                                                                   | Speed        |
|-------------------|--------------------------|-----------------------------------------------------------------------------|--------------|
| Unit tests        | `mvn test`               | The reporter classes, fed with events built in the test                     | milliseconds |
| Integration tests | `mvn integration-test`   | Real Maven builds of sample projects with the reporter just built, per surefire version | seconds per project |

The unit tests are fast and easy to debug, but they only see the events the test assumes surefire sends. The integration
tests see what surefire really sends, which changes between versions (see issue #68 and the Compatibility section of
the [README](../README.md)).

* [Integration tests](#integration-tests)
  * [Layout](#layout)
  * [How a run works](#how-a-run-works)
  * [Why each piece is there](#why-each-piece-is-there)
  * [Golden files per surefire version](#golden-files-per-surefire-version)
* [Unit tests](#unit-tests)
* [Adding a test for a bug](#adding-a-test-for-a-bug)
* [Commands](#commands)
* [Troubleshooting](#troubleshooting)

## Integration tests

### Layout

```
src/it/
├── settings.xml                  Maven settings used by every sample build
├── verify-tree.groovy            Shared check: extracts the tree from build.log and compares it
├── nested-tree/                  One sample project ("scenario") per directory
│   ├── pom.xml                   Uses the reporter with the UNICODE theme
│   ├── src/test/java/sample/     The JUnit5 tests whose tree is asserted
│   ├── src/test/resources/junit-platform.properties
│   ├── verify.groovy             One line that runs ../verify-tree.groovy
│   ├── expected-output.txt       Golden file, the expected tree
│   └── expected-output-3.5.3.txt Golden file only for surefire 3.5.3, which prints a different tree
├── hide-results-on-success/      EMOJI theme, hideResultsOnSuccess and printBlankLineBetweenTests
│   └── ...
├── failsafe-nested-tree/         nested-tree's tests run with maven-failsafe-plugin instead of surefire
│   ├── pom.xml                   Same configuration on maven-failsafe-plugin, reads ../nested-tree/src/test
│   ├── invoker.properties        Runs mvn verify, failsafe runs in the integration-test phase
│   └── verify.groovy             Compares with nested-tree's golden files
├── failure-focus/                UNICODE theme, printTestCounts, collapseSuccessfulClasses, hideSuccessfulNestedClasses and hideResultsOnSuccess
│   └── ...                       Green, failing and skipped classes, with the failure first, in the middle and last
├── print-test-counts/            The same classes with only printTestCounts
├── parallel-tree/                nested-tree's samples and 8 more classes, run by JUnit5 in parallel
│   └── verify.groovy             Sets sortClassBlocks, the classes complete in any order
├── fork-count/                   parallel-tree's tests in 8 forks, compared with parallel-tree's golden files
└── fork-stdout/                  8 classes that print 10 lines each, in 8 forks, with printStdoutOnSuccess
```

Each scenario is a small but complete Maven project. Its tests can be read side by side with its
`expected-output.txt` without any knowledge of the reporter internals.

### How a run works

`mvn integration-test` goes through the normal lifecycle first (`compile`, `test`, `package`), so the unit tests run
and the reporter jar is built. Then the [maven-invoker-plugin](https://maven.apache.org/plugins/maven-invoker-plugin/)
takes over, in two executions configured in the root `pom.xml`:

1. **`install-for-it`** (`invoker:install`, phase `pre-integration-test`) installs the jar just built into
   `target/local-repo`, a Maven repository used only by the integration tests.
2. **`run-it`** (`invoker:run`, phase `integration-test`), for each directory in `src/it` that has a `pom.xml`:
   1. Copies the scenario to `target/it/surefire-<version>/<scenario>` and replaces the `@...@` placeholders in its
      `pom.xml` with values from the root pom: `@project.version@` becomes the reporter version just built,
      `@junit-bom.version@` the JUnit version, and so on.
   2. Runs `mvn test -Dsurefire.version=<version>` on the copy, using `target/local-repo` and `src/it/settings.xml`.
      A scenario's `invoker.properties` can change the goals, like `mvn verify` for `failsafe-nested-tree`.
      Everything Maven prints goes to `build.log` in the copy.
   3. Runs the scenario's `verify.groovy`, which runs `src/it/verify-tree.groovy`:
      1. Takes the lines between `T E S T S` and `Results:` from `build.log`. That is where the tree is printed.
      2. Normalizes them: drops `Picked up ...` notices from the JVM, ANSI colors, the `[INFO] ` prefix and trailing
         spaces, and replaces every elapsed time (`- 0.021 s`, or `- 0.021` on surefire 3.0.0) with `- <time>`.
      3. Compares the result with `expected-output-<version>.txt` if that file exists, otherwise with
         `expected-output.txt`. If they differ, the scenario fails with a line diff (`-` only in the golden file,
         `+` only in the build).
3. The build fails if any scenario failed. Each failure shows the diff in the console.

The surefire version is `-Dit.surefire.version`, `3.6.0` (`maven.surefire.version` in the root pom) by default. CI
runs the whole build once per version in `.github/workflows/maven-test-suite.yml`. A first job, `surefire-versions`,
builds the list: the older versions in `OLDER_VERSIONS` plus `maven.surefire.version` read from the root pom.

```yaml
env:
  OLDER_VERSIONS: '["3.0.0", "3.2.5", "3.5.3"]'
```

The `build` job then runs once per version in that list, with `fail-fast: false`, so every version finishes and the
red jobs (named `surefire 3.0.0`, ..., `surefire 3.6.0`) show exactly which versions broke.

Moving the project to a new surefire version takes two changes:

1. `maven.surefire.version` in the root `pom.xml`. The compile dependencies, the project's own surefire and the CI
   matrix follow it.
2. The Compatibility table of the README.

If the previous version stays supported, it is added to `OLDER_VERSIONS`. The dependency graph upload runs in its own
`dependency-graph` job, so it does not depend on the matrix.

CI runs `mvn integration-test` and not `mvn verify`, because the `verify` phase also runs the gpg signing used for
releases. The release workflow runs `mvn deploy`, which passes through `integration-test`, so a release runs these
tests too.

### Why each piece is there

**maven-invoker-plugin.** It is the standard way to test Maven plugins and extensions: it runs real Maven builds and
checks them with a script. A real build is needed because the tree is built from the events surefire sends from its
forked JVM, and those events changed between versions. The plugin is the only new build dependency. It brings its
own Groovy for the scripts, so the project itself does not depend on Groovy.

**`invoker:install` and `target/local-repo`.** Each sample project is built by a separate Maven process. Inside the
root build, Maven resolves the reporter from the build itself (`target/classes`), which is why the root `pom.xml` can
use the reporter in its own surefire configuration and print the tree of its unit tests with the code that was just
changed. A separate build has no such link to the root project and can only resolve the reporter from a repository.
Since the version in `pom.xml` (for example `2.0.0`) may already exist in `~/.m2` or in Maven Central with older code,
the jar just built is installed into `target/local-repo`, a repository used only by the sample builds. That way they
always run the code under test, and `~/.m2` is left untouched.

**`src/it/settings.xml`.** It points the sample builds at the regular local repository (`@localRepositoryUrl@`) as a
remote repository. Artifacts already available there are copied instead of downloaded again into
`target/local-repo`. `mergeUserSettings` keeps the user's `~/.m2/settings.xml` (mirrors, proxies) in effect. The line
`!src/it/settings.xml` in `.gitignore` is needed because `.gitignore` ignores every `settings.xml`.

**`cloneProjectsTo` per version.** The scenarios run on copies, so the build never writes into `src/it`, and runs with
different versions keep their logs side by side in `target/it/surefire-<version>`.

**`<properties><surefire.version>`.** Passes `-Dsurefire.version` to the sample builds. Their `pom.xml` declares
`<version>${surefire.version}</version>` for the surefire plugin, so the same scenario runs with any version.

**`failsafe-nested-tree`.** The README says the reporter works with maven-failsafe-plugin, and this scenario is what
checks it. Failsafe is released together with surefire under the same version, so it uses `${surefire.version}` too
and runs with every version in the CI matrix. It reuses nested-tree's tests through `testSourceDirectory` instead of
copying them, and its `verify.groovy` sets `goldenScenario` so the tree is compared with nested-tree's golden files: the
reporter must print the same tree under both plugins. Surefire is skipped in this scenario, otherwise it would run the
same tests first and its output would be the one compared.

**`failure-focus`.** The options that cut the tree down to what failed, all on at once. The connectors (`├─`, `└─`,
`│`) have to be worked out on what is printed and not on the whole tree, so the classes put the failure where it breaks
them: first and last in its class, in the middle of green nested classes, deep in a nested class whose sibling is
hidden, and a skipped test in a nested class. `AllGreenTest` and `SkippedTest` have no failure, so they collapse into
one line. `print-test-counts` runs the same classes with just `printTestCounts`, which is also the full tree the
other scenario is cut from.

**`parallel-tree`.** JUnit5 runs its classes in parallel (`junit-platform.properties`), so surefire reports a class
while others are still running, and the classes complete in a different order on each run. Its `verify.groovy` sets
`sortClassBlocks`: the trees of the top-level classes, each starting with `┬─ ` or `── ` (UNICODE theme), are sorted
before the comparison, so the order does not matter but every line of every class does. `Par1Test` to `Par8Test` sleep
for a random time on purpose, so the classes overlap. Run without the fix for issue #82, it fails on every run.

**`fork-count`.** With `forkCount` above 1 surefire clones the reporter into the class loader of the forks, which is
where issue #82 failed before any test ran, and reports each fork from its own thread, which mixed the trees of classes
completing at the same time (issue #83). It runs parallel-tree's tests in 8 forks, one class at a time in each fork
(nested-tree's `junit-platform.properties`), and compares them with parallel-tree's golden files, with
`sortClassBlocks`. `Par1Test` to `Par8Test` wait for each other in `@AfterAll` (`Barrier`: each class leaves a file in `target/barrier` and
waits for the others, and fails the class if they do not arrive in 20 s), so the 8 forks complete their classes at the
same time, which is when the trees get mixed. Run without the fix for issue #83, it fails on most runs (9 of 10 when measured). Since it reads the tests of
other scenarios, running it alone takes them too: `-Dinvoker.test=fork-count,nested-tree,parallel-tree` (the same goes
for `failsafe-nested-tree` and nested-tree).

**`fork-stdout`.** Same idea as `fork-count`, for the output of the tests. `printStdoutOnSuccess` makes the reporter print
what each test wrote to stdout, and that goes straight to `System.out` (`ActualTreePrinter.printStdOut`), not through
the logger the tree lines use. 8 classes print 10 lines each and wait for each other in `@AfterAll`, so the 8 forks
report at the same time. Its golden file is compared with `sortClassBlocks`, so every class must print its tree line,
its test, `Standard out` and its 10 lines as one block. Run without the fix for issue #83, it fails on every run.

**`<postBuildHookScript>verify</postBuildHookScript>`.** The invoker looks for `postbuild.groovy` by default. This makes
it run `verify.groovy`, the name most Maven projects use.

**`<scriptVariables>`.** Values the Groovy script receives: `itSourceDir` to find the golden files in `src/it` (the
script runs in the copy under `target`), `surefireVersion` to pick the per-version golden file, and `snapshotUpdate`
for `-Dsnapshot.update=true`.

**`<mavenOpts>-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8</mavenOpts>`.** The UNICODE and EMOJI themes print
characters outside ASCII. Without this, a machine with a non UTF-8 default (like a CI runner with the `POSIX` locale)
would print `?` instead, and the comparison would be meaningless.

**`<runOrder>alphabetical</runOrder>` in the sample poms.** Surefire runs test classes in file system order by
default, which differs between machines. Alphabetical order makes the tree the same everywhere.

**`junit-platform.properties` in each scenario.** Same reason, for JUnit: methods are ordered by name and `@Nested`
classes by class name. Without it, JUnit uses an order that is deterministic but hard to predict.

**`<testFailureIgnore>true</testFailureIgnore>` in the sample poms.** Scenarios have tests that fail on purpose, to
assert how failures and errors are printed. This keeps the sample build successful, so the invoker goes on to the
verify script.

**Groovy in the verify script.** It runs inside the invoker with no setup, and reads a log and compares text in a few
lines. Every scenario's `verify.groovy` is one line that runs the shared `verify-tree.groovy`, so the comparison
logic lives in one place.

### Golden files per surefire version

`expected-output.txt` is the tree most versions print. A version only gets its own `expected-output-<version>.txt`
when surefire itself sends different events, not when the reporter has a bug. Today the only one is
`nested-tree/expected-output-3.5.3.txt`: surefire 3.5.3 does not report the `@Nested` class of a `@Disabled` class
at all, so the tree has one line less.

Before adding a per-version file, check whether the reporter can print the same tree from what that version sends.
That is how `sample.DisabledClassTest$InnerTest` on 3.6.0 became `InnerTest`, like the other versions.

## Unit tests

The unit tests feed `ConsoleTreeReporter` with surefire's report objects directly, with no Maven and no fork. The
pieces in `src/test/java/org/apache/maven/plugin/surefire/report`:

* **`CapturingLogger`** keeps every line the reporter prints, so a test can assert on them.
* **`SurefireEmulator`** reads a test class by reflection and sends the reporter the same events maven-surefire-plugin
  3.6.0 sends: each class is started before its `@Nested` classes and completed after them, with the results
  collected since the last completed class. It returns the printed lines. Classes and methods are sorted by name,
  because Java does not specify the order reflection returns them in. Every test passes unless the unit test says otherwise with
  `withOutcome("SimpleClassName.methodName", ReportEntryType.FAILURE)` (also `ERROR` and `SKIPPED`), which reports
  that method with that result. The integration tests are still the ones that see what surefire sends for a real
  failure, a skipped test or a stack trace.
* **`Snapshot.assertMatches(name, lines)`** normalizes the lines the same way as `verify-tree.groovy` and compares them
  with `src/test/resources/snapshots/<name>.txt`.
* **`TreeSnapshotTest`** uses all of the above for the sample classes in `org.apache.maven.plugin.surefire`, every
  theme and some options.
* **`ParallelForksTest`** runs several `SurefireEmulator`s at the same time on one shared `CapturingLogger`, the way
  surefire reports the forks of `forkCount` above 1, and checks each class is printed in one piece.

When the exact events matter (an event order or a field only one surefire version sends), build them by hand like
`QualifiedSourceNameTest` does, and assert on `CapturingLogger.lines`.

These tests only use what the project already depended on: JUnit Jupiter (including `junit-jupiter-params`), AssertJ
and the surefire APIs.

## Adding a test for a bug

Every bug fix comes with a test that fails before the fix and passes after it. In the workflow below the expected tree
is written by hand first, so the golden file states what the output *should* be, not what the faulty code prints.

1. **Reproducing the bug in a scenario.** The test classes from the bug report go into the scenario whose
   configuration matches. A bug that needs a different configuration (a theme or an option) gets a new scenario,
   usually a copy of an existing one:

   ```bash
   cp -r src/it/nested-tree src/it/issue-123-short-description
   ```

   In the copy, the `artifactId` and the `statelessTestsetInfoReporter` configuration of the `pom.xml` are adjusted,
   the test classes in `src/test/java/sample` are replaced, and the copied `expected-output*.txt` files are deleted.
   The tests are kept as small as the bug allows, carry `@DisplayName`s, and do not call `Thread.sleep`, since the
   elapsed times are replaced anyway.

2. **Writing the expected tree.** The scenario's `expected-output.txt` holds the tree as it should be printed, with
   `- <time>` in place of every elapsed time. The quickest way to get there is to generate the current, wrong tree and
   then correct it by hand:

   ```bash
   mvn integration-test -Dinvoker.test=issue-123-short-description -Dsnapshot.update=true
   # then src/it/issue-123-short-description/expected-output.txt is edited into the correct tree
   ```

   For a bug in an existing scenario, its existing golden file is edited the same way.

3. **Confirming the failure.** Without `-Dsnapshot.update`, the run fails and the diff shows the bug:

   ```bash
   mvn integration-test -Dinvoker.test=issue-123-short-description
   ```

4. **Adding a unit test where possible.** When the bug also shows up through `SurefireEmulator`, the test class is added
   to `TreeSnapshotTest`. When it depends on specific events, the events are built by hand as in
   `QualifiedSourceNameTest`. The new unit test must fail as well before the fix.

5. **Fixing the code** in `src/main` until both tests pass.

6. **Checking the other surefire versions.** CI runs every version on the pull request, and the same check runs
   locally with:

   ```bash
   for v in 3.0.0 3.2.5 3.5.3 3.6.0; do mvn integration-test -Dit.surefire.version=$v; done
   ```

   When one version prints a different tree, the first question is whether the reporter can print the same tree from
   what that version sends (see [Golden files per surefire version](#golden-files-per-surefire-version)). Only when
   surefire really sends something different does that version get its own golden file:

   ```bash
   touch src/it/issue-123-short-description/expected-output-3.5.3.txt
   mvn integration-test -Dit.surefire.version=3.5.3 -Dinvoker.test=issue-123-short-description -Dsnapshot.update=true
   ```

   `-Dsnapshot.update=true` writes to `expected-output-<version>.txt` only if that file already exists, and to
   `expected-output.txt` otherwise, hence the `touch` first.

7. **Reviewing the golden files in `git diff`** before committing. A golden file is the test's assertion, so an
   unreviewed golden file asserts nothing. The scenario, its golden files and the fix are committed together.

## Commands

| What                                              | Command                                                                       |
|---------------------------------------------------|-------------------------------------------------------------------------------|
| Unit tests only                                   | `mvn test`                                                                    |
| Unit and integration tests, surefire 3.6.0        | `mvn integration-test`                                                        |
| Integration tests with another surefire version   | `mvn integration-test -Dit.surefire.version=3.5.3`                            |
| One scenario                                      | `mvn integration-test -Dinvoker.test=nested-tree`                             |
| Skip the unit tests                               | add `-DskipTests` (the sample builds still run their own tests)               |
| Skip the integration tests                        | add `-Dinvoker.skip=true`                                                     |
| Accept an intended change in the output           | add `-Dsnapshot.update=true` to any of the above, then review `git diff`      |

## Troubleshooting

**Where is the output of a sample build?** In `target/it/surefire-<version>/<scenario>/build.log`. It has the whole
Maven output, including the tree and the stack traces of the tests that fail on purpose.

**A scenario fails but the diff looks the same.** The difference is usually invisible: trailing spaces are removed,
but other whitespace and characters that look alike (`─` and `-`) are not. A diff tool comparing the golden
file with the tree in `build.log` shows it.

**`?` instead of the tree characters in the console.** That is only the terminal's encoding. `build.log` and the golden
files are UTF-8, and the comparison uses those.

**`golden file missing`.** The scenario has no `expected-output.txt` yet. It is created as described in
[Adding a test for a bug](#adding-a-test-for-a-bug).

**The sample build fails before running the tests.** The cause is in `build.log`. A version that doesn't exist in Maven
Central, or a version not yet in the local repository while offline, fails during dependency resolution.

**Debugging the reporter inside a sample build.** The reporter runs in the sample build's Maven process. That build
can be started by hand from its copy, with the same local repository, and a debugger attached on port 8000:

```bash
cd target/it/surefire-3.6.0/nested-tree
mvnDebug test -Dsurefire.version=3.6.0 -Dmaven.repo.local=../../../local-repo
```
