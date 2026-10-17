# Maven Surefire JUnit5 TreeView Extension

If you are a Maven Surefire user and ever wanted a fancy tree output for your tests instead of a bunch of logs, you absolutely should try this.

This is a dependency for [maven-surefire-plugin](https://maven.apache.org/surefire/maven-surefire-plugin/), it adds a tree view for the unit tests executed using JUnit5.

[![Maven Central](https://img.shields.io/maven-central/v/me.fabriciorby/maven-surefire-junit5-tree-reporter?style=for-the-badge)](https://search.maven.org/artifact/me.fabriciorby/maven-surefire-junit5-tree-reporter)
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License&style=for-the-badge)](http://www.apache.org/licenses/LICENSE-2.0)

## Installation

The Maven Repository can be found [here](https://mvnrepository.com/artifact/me.fabriciorby/maven-surefire-junit5-tree-reporter).

Configure your POM like the following

```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.6.0</version>
    <dependencies>
        <dependency>
            <groupId>me.fabriciorby</groupId>
            <artifactId>maven-surefire-junit5-tree-reporter</artifactId>
            <version>2.0.0</version>
        </dependency>
    </dependencies>
    <configuration>
        <reportFormat>plain</reportFormat>
        <consoleOutputReporter>
            <disable>true</disable>
        </consoleOutputReporter>
        <statelessTestsetInfoReporter
                implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
        </statelessTestsetInfoReporter>
    </configuration>
</plugin>
```

### Compatibility

| `maven-surefire-plugin` | `maven-surefire-junit5-tree-reporter` |
|-------------------------|---------------------------------------|
| `3.6.0` and newer       | `2.0.0` and newer                     |
| `3.5.4` to `3.5.6`      | Not supported                         |
| `3.5.3` and older       | `1.5.1` or `2.0.0` and newer          |

`maven-surefire-plugin` versions `3.5.4` to `3.5.6` stopped sending the nested class of JUnit5 `@Nested` tests,
so the tree cannot be built ([apache/maven-surefire#3203](https://github.com/apache/maven-surefire/issues/3203)).
This was fixed in `3.6.0`.

### Maven Failsafe

The extension also works with [maven-failsafe-plugin](https://maven.apache.org/surefire/maven-failsafe-plugin/), which
shares the same reporter extension API. Add the same `<dependencies>` and `<configuration>` shown above to
`maven-failsafe-plugin` too. The compatibility table above applies to `maven-failsafe-plugin` versions as well.

## Features

The examples use the `EMOJI` theme. Click a feature to see how to turn it on and what it prints.

<details>
<summary><b>Output theme</b></summary>

The output can be printed using Themes:
- ASCII (default).
- UNICODE
- EMOJI

`EMOJI`:
```xml
<statelessTestsetInfoReporter
        implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
    <theme>EMOJI</theme>
</statelessTestsetInfoReporter>
```
![Imgur](https://i.imgur.com/8JuxGRO.png "EMOJI Output")

`UNICODE`:
```xml
<statelessTestsetInfoReporter
        implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
    <theme>UNICODE</theme>
</statelessTestsetInfoReporter>
```
![Imgur](https://i.imgur.com/wBSQnin.png "UNICODE Output")

`ASCII`:
```xml
<statelessTestsetInfoReporter
        implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
    <theme>ASCII</theme>
</statelessTestsetInfoReporter>
```
![Imgur](https://i.imgur.com/g6OkbqU.png "ASCII Output")

</details>

<details>
<summary><b>Blank line between tests</b></summary>

```xml
<statelessTestsetInfoReporter
        implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
    <printBlankLineBetweenTests>true</printBlankLineBetweenTests>
</statelessTestsetInfoReporter>
```

The result prints a blank line between the trees of two classes:

```
┬─ All Green - 0.012 s
│  ├─ 🟢 Passes one - 0.004 s
│  └─ 🟢 Passes two - 0.009 s
└─ ── Green Inner - 0.021 s
      └─ 🟢 Inner passes - 0.006 s

┬─ Fail In Middle - 0.015 s
│  └─ 🟢 Top passes - 0.003 s
├─ ── Green Before - 0.011 s
│     └─ 🟢 Before passes - 0.012 s
├─ ┬─ Red Middle - 0.004 s
│  │  ├─ 🟢 Middle passes - 0.009 s
│  │  └─ 🔴 Middle fails - 0.021 s
│  ├─ ── Deep Green - 0.006 s
│  │     └─ 🟢 Deep green passes - 0.015 s
│  └─ ── Deep Red - 0.003 s
│        ├─ 🔴 Deep red errors - 0.011 s
│        └─ 🟢 Deep red passes - 0.012 s
└─ ── Green After - 0.004 s
      └─ 🟢 After passes - 0.009 s
```

</details>

<details>
<summary><b>Hide successful results (<code>hideResultsOnSuccess</code>)</b></summary>

Output in large projects could become too verbose, making failures or errors difficult to find inside the printed trees.
To reduce verbosity, the _hide successful results_ option prints only the tests that did not pass:

```xml
<statelessTestsetInfoReporter
        implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
    <hideResultsOnSuccess>true</hideResultsOnSuccess>
</statelessTestsetInfoReporter>
```

Without the option:

```
┬─ All Green - 0.012 s
│  ├─ 🟢 Passes one - 0.004 s
│  └─ 🟢 Passes two - 0.009 s
└─ ── Green Inner - 0.021 s
      └─ 🟢 Inner passes - 0.006 s
┬─ Fail In Middle - 0.015 s
│  └─ 🟢 Top passes - 0.003 s
├─ ── Green Before - 0.011 s
│     └─ 🟢 Before passes - 0.012 s
├─ ┬─ Red Middle - 0.004 s
│  │  ├─ 🟢 Middle passes - 0.009 s
│  │  └─ 🔴 Middle fails - 0.021 s
│  ├─ ── Deep Green - 0.006 s
│  │     └─ 🟢 Deep green passes - 0.015 s
│  └─ ── Deep Red - 0.003 s
│        ├─ 🔴 Deep red errors - 0.011 s
│        └─ 🟢 Deep red passes - 0.012 s
└─ ── Green After - 0.004 s
      └─ 🟢 After passes - 0.009 s
```

With the option:

```
┬─ All Green - 0.012 s
└─ ── Green Inner - 0.004 s
┬─ Fail In Middle - 0.009 s
├─ ── Green Before - 0.021 s
├─ ┬─ Red Middle - 0.006 s
│  │  └─ 🔴 Middle fails - 0.015 s
│  ├─ ── Deep Green - 0.003 s
│  └─ ── Deep Red - 0.011 s
│        └─ 🔴 Deep red errors - 0.012 s
└─ ── Green After - 0.004 s
```

</details>

<details>
<summary><b>Test counts (<code>printTestCounts</code>)</b></summary>

Adds the number of tests to the line of each class, including the `@Nested` ones, counting everything below it.
A failed count includes the errors, and categories with zero are left out. Default: `false`.

```xml
<statelessTestsetInfoReporter
        implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
    <printTestCounts>true</printTestCounts>
</statelessTestsetInfoReporter>
```

Without the option:

```
┬─ All Green - 0.012 s
│  ├─ 🟢 Passes one - 0.004 s
│  └─ 🟢 Passes two - 0.009 s
└─ ── Green Inner - 0.021 s
      └─ 🟢 Inner passes - 0.006 s
┬─ Fail In Middle - 0.015 s
│  └─ 🟢 Top passes - 0.003 s
├─ ── Green Before - 0.011 s
│     └─ 🟢 Before passes - 0.012 s
├─ ┬─ Red Middle - 0.004 s
│  │  ├─ 🟢 Middle passes - 0.009 s
│  │  └─ 🔴 Middle fails - 0.021 s
│  ├─ ── Deep Green - 0.006 s
│  │     └─ 🟢 Deep green passes - 0.015 s
│  └─ ── Deep Red - 0.003 s
│        ├─ 🔴 Deep red errors - 0.011 s
│        └─ 🟢 Deep red passes - 0.012 s
└─ ── Green After - 0.004 s
      └─ 🟢 After passes - 0.009 s
```

With the option:

```
┬─ All Green (3 passed) - 0.012 s
│  ├─ 🟢 Passes one - 0.004 s
│  └─ 🟢 Passes two - 0.009 s
└─ ── Green Inner (1 passed) - 0.021 s
      └─ 🟢 Inner passes - 0.006 s
┬─ Fail In Middle (6 passed, 2 failed) - 0.015 s
│  └─ 🟢 Top passes - 0.003 s
├─ ── Green Before (1 passed) - 0.011 s
│     └─ 🟢 Before passes - 0.012 s
├─ ┬─ Red Middle (3 passed, 2 failed) - 0.004 s
│  │  ├─ 🟢 Middle passes - 0.009 s
│  │  └─ 🔴 Middle fails - 0.021 s
│  ├─ ── Deep Green (1 passed) - 0.006 s
│  │     └─ 🟢 Deep green passes - 0.015 s
│  └─ ── Deep Red (1 passed, 1 failed) - 0.003 s
│        ├─ 🔴 Deep red errors - 0.011 s
│        └─ 🟢 Deep red passes - 0.012 s
└─ ── Green After (1 passed) - 0.004 s
      └─ 🟢 After passes - 0.009 s
```

</details>

<details>
<summary><b>Collapse successful classes (<code>collapseSuccessfulClasses</code>)</b></summary>

Prints a top-level class with no failure and no error in a single line with its counts. Classes with a failure are
printed in full. Default: `false`.

```xml
<statelessTestsetInfoReporter
        implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
    <collapseSuccessfulClasses>true</collapseSuccessfulClasses>
</statelessTestsetInfoReporter>
```

Without the option:

```
┬─ All Green - 0.012 s
│  ├─ 🟢 Passes one - 0.004 s
│  └─ 🟢 Passes two - 0.009 s
└─ ── Green Inner - 0.021 s
      └─ 🟢 Inner passes - 0.006 s
┬─ Fail In Middle - 0.015 s
│  └─ 🟢 Top passes - 0.003 s
├─ ── Green Before - 0.011 s
│     └─ 🟢 Before passes - 0.012 s
├─ ┬─ Red Middle - 0.004 s
│  │  ├─ 🟢 Middle passes - 0.009 s
│  │  └─ 🔴 Middle fails - 0.021 s
│  ├─ ── Deep Green - 0.006 s
│  │     └─ 🟢 Deep green passes - 0.015 s
│  └─ ── Deep Red - 0.003 s
│        ├─ 🔴 Deep red errors - 0.011 s
│        └─ 🟢 Deep red passes - 0.012 s
└─ ── Green After - 0.004 s
      └─ 🟢 After passes - 0.009 s
```

With the option:

```
── All Green (3 passed) - 0.012 s
┬─ Fail In Middle - 0.004 s
│  └─ 🟢 Top passes - 0.009 s
├─ ── Green Before - 0.021 s
│     └─ 🟢 Before passes - 0.006 s
├─ ┬─ Red Middle - 0.015 s
│  │  ├─ 🟢 Middle passes - 0.003 s
│  │  └─ 🔴 Middle fails - 0.011 s
│  ├─ ── Deep Green - 0.012 s
│  │     └─ 🟢 Deep green passes - 0.004 s
│  └─ ── Deep Red - 0.009 s
│        ├─ 🔴 Deep red errors - 0.021 s
│        └─ 🟢 Deep red passes - 0.006 s
└─ ── Green After - 0.015 s
      └─ 🟢 After passes - 0.003 s
```

`collapseSuccessfulClasses` is applied before `hideResultsOnSuccess`: a class with only passed or skipped tests becomes
one line (skipped tests do not prevent it), and its stdout and stderr are not printed, whatever the `printStdoutOnSuccess`
and `printStderrOnSuccess` options say.

</details>

<details>
<summary><b>Hide successful nested classes (<code>hideSuccessfulNestedClasses</code>)</b></summary>

Leaves out the `@Nested` classes that have no failure, error or skipped test, and keeps the path to the ones that are
left. Default: `false`.

```xml
<statelessTestsetInfoReporter
        implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
    <hideSuccessfulNestedClasses>true</hideSuccessfulNestedClasses>
</statelessTestsetInfoReporter>
```

Without the option:

```
┬─ All Green - 0.012 s
│  ├─ 🟢 Passes one - 0.004 s
│  └─ 🟢 Passes two - 0.009 s
└─ ── Green Inner - 0.021 s
      └─ 🟢 Inner passes - 0.006 s
┬─ Fail In Middle - 0.015 s
│  └─ 🟢 Top passes - 0.003 s
├─ ── Green Before - 0.011 s
│     └─ 🟢 Before passes - 0.012 s
├─ ┬─ Red Middle - 0.004 s
│  │  ├─ 🟢 Middle passes - 0.009 s
│  │  └─ 🔴 Middle fails - 0.021 s
│  ├─ ── Deep Green - 0.006 s
│  │     └─ 🟢 Deep green passes - 0.015 s
│  └─ ── Deep Red - 0.003 s
│        ├─ 🔴 Deep red errors - 0.011 s
│        └─ 🟢 Deep red passes - 0.012 s
└─ ── Green After - 0.004 s
      └─ 🟢 After passes - 0.009 s
```

With the option:

```
── All Green - 0.012 s
   ├─ 🟢 Passes one - 0.004 s
   └─ 🟢 Passes two - 0.009 s
┬─ Fail In Middle - 0.021 s
│  └─ 🟢 Top passes - 0.006 s
└─ ┬─ Red Middle - 0.015 s
   │  ├─ 🟢 Middle passes - 0.003 s
   │  └─ 🔴 Middle fails - 0.011 s
   └─ ── Deep Red - 0.012 s
         ├─ 🔴 Deep red errors - 0.004 s
         └─ 🟢 Deep red passes - 0.009 s
```

Use it together with `hideResultsOnSuccess` to see only what went wrong:

```xml
<statelessTestsetInfoReporter
        implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
    <hideSuccessfulNestedClasses>true</hideSuccessfulNestedClasses>
    <hideResultsOnSuccess>true</hideResultsOnSuccess>
</statelessTestsetInfoReporter>
```

```
── All Green - 0.012 s
┬─ Fail In Middle - 0.004 s
└─ ┬─ Red Middle - 0.009 s
   │  └─ 🔴 Middle fails - 0.021 s
   └─ ── Deep Red - 0.006 s
         └─ 🔴 Deep red errors - 0.015 s
```

</details>

<details>
<summary><b>Focus on failures: all the options together</b></summary>

```xml
<statelessTestsetInfoReporter
        implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
    <printTestCounts>true</printTestCounts>
    <collapseSuccessfulClasses>true</collapseSuccessfulClasses>
    <hideSuccessfulNestedClasses>true</hideSuccessfulNestedClasses>
    <hideResultsOnSuccess>true</hideResultsOnSuccess>
</statelessTestsetInfoReporter>
```

```
── All Green (3 passed) - 0.012 s
┬─ Fail In Middle (6 passed, 2 failed) - 0.004 s
└─ ┬─ Red Middle (3 passed, 2 failed) - 0.009 s
   │  └─ 🔴 Middle fails - 0.021 s
   └─ ── Deep Red (1 passed, 1 failed) - 0.006 s
         └─ 🔴 Deep red errors - 0.015 s
```

</details>

<details>
<summary><b>Failure details</b></summary>

By default, `<consoleOutputReporter><disable>true</disable></consoleOutputReporter>` disables all console output. To debug test failures, it may be useful to see the console output and stack traces when a test fails. To do so, you can configure this extension like this:

```xml
<statelessTestsetInfoReporter
        implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
    <printStacktraceOnError>true</printStacktraceOnError>
    <printStacktraceOnFailure>true</printStacktraceOnFailure>
    <printStdoutOnError>true</printStdoutOnError>
    <printStdoutOnFailure>true</printStdoutOnFailure>
    <printStdoutOnSuccess>false</printStdoutOnSuccess>
    <printStderrOnError>true</printStderrOnError>
    <printStderrOnFailure>true</printStderrOnFailure>
    <printStderrOnSuccess>false</printStderrOnSuccess>
</statelessTestsetInfoReporter>
```

</details>


## Contribute

You are welcome to contribute to the project, for this just open an issue or issue + PR to ``develop`` branch.

If you want to create your own output based on the [Theme](src/main/java/org/apache/maven/plugin/surefire/report/Theme.java) Enum, feel free to open a PR.

### Debugging

If you ever want to debug the code, please use the following command
```
mvnDebug test
```
Then attach a remote JVM debugger on port 8000

### Using SurefireEmulator

This SurefireEmulator class was developed so it's easier to debug the code.

```java
new SurefireEmulator(NestedExampleTest.class).run();
```

By running this command it's possible to debug the code almost as the actual
Surefire was running. And it also returns a List with all the lines that were
printed during the test execution.

### Testing the printed tree

The tree is compared with golden files in unit tests (`mvn test`) and in integration tests that run real Maven
builds with every supported maven-surefire-plugin version (`mvn integration-test`). Every bug fix should come with
such a test.

[docs/integration-tests.md](docs/integration-tests.md) explains how they work, why each piece is there, and how to
add a test for a bug.
