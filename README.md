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

## Output Theme

The output can be printed using Themes:
- ASCII (default).
- UNICODE
- EMOJI

### EMOJI
```xml
<statelessTestsetInfoReporter
        implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
    <theme>EMOJI</theme>
</statelessTestsetInfoReporter>
```
![Imgur](https://i.imgur.com/8JuxGRO.png "EMOJI Output")

### UNICODE
```xml
<statelessTestsetInfoReporter
        implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
    <theme>UNICODE</theme>
</statelessTestsetInfoReporter>
```
![Imgur](https://i.imgur.com/wBSQnin.png "UNICODE Output")

### ASCII
```xml
<statelessTestsetInfoReporter
        implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
    <theme>ASCII</theme>
</statelessTestsetInfoReporter>
```
![Imgur](https://i.imgur.com/g6OkbqU.png "ASCII Output")

## Blank line between tests
```xml
<statelessTestsetInfoReporter
        implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
    <printBlankLineBetweenTests>true</printBlankLineBetweenTests>
</statelessTestsetInfoReporter>
```


## Reduce verbosity

Output in large projects could become too verbose, making failures or errors difficult to find inside the printed trees.
To reduce verbosity, you can a _hide successful results_ option can be configured as follows:

```xml
<statelessTestsetInfoReporter
        implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
    <hideResultsOnSuccess>true</hideResultsOnSuccess>
</statelessTestsetInfoReporter>
```

## Failure details

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

The tree is compared with golden files in two places:

* Unit tests (`mvn test`): `SurefireEmulator` sends the same events maven-surefire-plugin 3.6.0 sends, and
  `TreeSnapshotTest` compares the printed lines with [src/test/resources/snapshots](src/test/resources/snapshots).
* Integration tests (`mvn integration-test`): the [maven-invoker-plugin](https://maven.apache.org/plugins/maven-invoker-plugin/)
  installs the reporter just built and runs every project in [src/it](src/it) with maven-surefire-plugin 3.5.3 and
  3.6.0. [verify-tree.groovy](src/it/verify-tree.groovy) compares the tree in each `build.log` with the project's
  `expected-output.txt`, or with `expected-output-<surefire version>.txt` when the tree differs between versions.
  Run a single project with `mvn integration-test -Dinvoker.test=nested-tree`.

If the output changes on purpose, run the same command with `-Dsnapshot.update=true` to rewrite the golden files
and review the change with `git diff`.
