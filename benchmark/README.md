# Benchmark

Scripts to measure the time and memory cost of the tree reporter on large test suites.

`gen.py` generates a standalone Maven project with as many JUnit 5 test classes as you want.
The same project can run with any build of the reporter, or with the default surefire console
reporter as a baseline, so the difference between the two runs is what the tree reporter costs.

Requirements: Python 3, Maven, a JDK (`jcmd` is only needed by `histo.sh`).

## Quick start

```sh
cd benchmark

# 1. install the reporter you want to measure with a version of its own
(cd .. && mvn versions:set -DnewVersion=bench-local -DgenerateBackupPoms=false \
       && mvn install -DskipTests -Dgpg.skip; git checkout pom.xml)

# 2. generate a project and compile its tests once
./gen.py work/bench --classes 5000
(cd work/bench && mvn test-compile)

# 3. run it with the tree reporter and with the baseline
./run.sh work/bench tree -Dtree.reporter.version=bench-local
./run.sh work/bench baseline -Pbaseline
```

`run.sh` prints one line per run:

```
tree                     rc=0 time=  31.5s mavenPeakAfterGC=182M forkPeakAfterGC=178M oome=0
baseline                 rc=0 time=  37.6s mavenPeakAfterGC=190M forkPeakAfterGC=178M oome=0
```

Run each scenario more than once and alternate them: on a small machine a single run is noisy.

## Generated project

Each top-level class `GeneratedNTest` has `--tests` test methods. Every `--nested-every`-th class also has
an `@Nested class Inner` with 2 tests and an `@Nested class Deeper` inside it with 2 more tests.

| option | default | what it does |
|---|---|---|
| `--classes N` | 5000 | number of top-level test classes |
| `--tests M` | 5 | test methods per top-level class |
| `--nested-every K` | 5 | every K-th class gets the `@Nested` classes, 0 for none |
| `--packages P` | 50 | classes are spread over P packages |
| `--fail-every F` | 0 | every F-th test fails with an `AssertionError`, 0 for none |
| `--stack-depth D` | 50 | extra stack frames in each failure |
| `--msg-kb K` | 1 | size of each failure message, in KB |
| `--stdout-kb S` | 0 | KB each test prints to stdout |
| `--parallel` | off | JUnit parallel execution with concurrent classes |
| `--fork-count N` | 1 | Maven `forkCount`: N test JVMs, each with its own reporter instance in the Maven JVM |
| `--surefire VERSION` | 3.6.0 | maven-surefire-plugin version |

The generated `pom.xml` has:

- the tree reporter configured as in the main README, with its version in the `tree.reporter.version`
  property (default `2.0.0`, the released version). Pass `-Dtree.reporter.version=...` to use another build.
- a `baseline` profile (`-Pbaseline`) that replaces the surefire configuration and uses the default
  surefire console reporter. Both setups keep `reportFormat=plain`, `trimStackTrace=false` and
  `testFailureIgnore=true`, so only the console reporter changes between them.

## Scripts

### run.sh

```sh
./run.sh PROJECT_DIR LABEL [extra maven args...]
```

Runs `mvn surefire:test` once, so the tests must already be compiled. Both the Maven JVM and the
forked test JVM run with SerialGC and GC logging, and the script reports the highest heap size left
after a GC in each of them, which is close to the live memory at its peak. The reporter runs in the
Maven JVM; the tests run in the fork.

Set `MAVEN_XMX` to limit the Maven heap, for example to find the smallest heap a run needs:

```sh
MAVEN_XMX=128m ./run.sh work/bench tree-128m -Dtree.reporter.version=bench-local
```

When the run fails with `OutOfMemoryError`, a heap dump is written to `logs/LABEL.hprof`.
The Maven output and the GC logs are in `logs/LABEL.out`, `logs/LABEL-maven.gc` and `logs/LABEL-fork.gc`.

### histo.sh

```sh
./histo.sh PROJECT_DIR LABEL [extra maven args...]
```

Runs the tests and takes a class histogram of the Maven JVM every 8 seconds (`INTERVAL` changes it)
into `logs/LABEL-histo-N.txt`. Comparing the counts of `org.apache.maven.plugin.surefire.report.Node`
and `WrappedReportEntry` between the tree run and the baseline shows whether the reporter keeps
results alive after printing them.

## Results

Past results are in [results/](results), one file per version and date.

## Scenarios

Many classes, as in the PR #74 benchmark:

```sh
./gen.py work/many --classes 5000
```

Many failures with JUnit in parallel, as in issue #49:

```sh
./gen.py work/failures --classes 5000 --fail-every 2 --parallel --stdout-kb 1
```

One very large class:

```sh
./gen.py work/big --classes 1 --tests 20000 --nested-every 0
```

Several forks (`forkCount`), alone and together with JUnit parallel:

```sh
./gen.py work/fork --classes 5000 --fork-count 4
./gen.py work/fork-parallel --classes 5000 --fail-every 2 --parallel --fork-count 4 --stdout-kb 1
```

`run.sh` reports the highest fork heap among all forks.

Older surefire versions:

```sh
./gen.py work/sf353 --classes 1000 --surefire 3.5.3
```
