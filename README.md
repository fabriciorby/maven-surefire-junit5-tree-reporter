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
    <version>3.5.3</version>
    <dependencies>
        <dependency>
            <groupId>me.fabriciorby</groupId>
            <artifactId>maven-surefire-junit5-tree-reporter</artifactId>
            <version>1.5.0</version>
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

### Important
Until `maven-surefire-plugin:3.5.3` you should use `maven-surefire-junit5-tree-reporter:1.5.0`.

There was a breaking change in `maven-surefire-plugin:3.5.4`, and this plugin won't work anymore.

For that version on I plan to continue with `maven-surefire-junit5-tree-reporter:2.x.x`.

## Output Theme

The output can be printed using two Themes: UNICODE and ASCII (by default).

### UNICODE
```xml
<statelessTestsetInfoReporter
        implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
    <theme>UNICODE</theme>
</statelessTestsetInfoReporter>
```
![Imgur](https://i.imgur.com/JdrP2QN.png "UNICODE Output")


### ASCII
```xml
<statelessTestsetInfoReporter
        implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
    <theme>ASCII</theme>
</statelessTestsetInfoReporter>
```
![Imgur](https://i.imgur.com/FzcIWwe.png "ASCII Output")

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
