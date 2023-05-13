# Maven Surefire JUnit5 TreeView Extension

If you are a Maven Surefire user and ever wanted a fancy tree output for your tests instead of a bunch of logs, you absolutely should try this.

This is a dependency for [maven-surefire-plugin](https://maven.apache.org/surefire/maven-surefire-plugin/), it adds a tree view for the unit tests executed using JUnit5.

[![Maven Central](https://img.shields.io/maven-central/v/me.fabriciorby/maven-surefire-junit5-tree-reporter?style=for-the-badge)](https://search.maven.org/artifact/me.fabriciorby/maven-surefire-junit5-tree-reporter)
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License&style=for-the-badge)](http://www.apache.org/licenses/LICENSE-2.0)

## Output

The output can be printed in two ways.

### UNICODE
![Imgur](https://i.imgur.com/JdrP2QN.png "UNICODE Output")
``org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporterUnicode``

### ASCII
![Imgur](https://i.imgur.com/FzcIWwe.png "ASCII Output")
``org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter``

## Installation

The Maven Repository can be found [here](https://mvnrepository.com/artifact/me.fabriciorby/maven-surefire-junit5-tree-reporter).

Configure your POM like the following

### UNICODE Output

```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.0.0-M8</version>
    <dependencies>
        <dependency>
            <groupId>me.fabriciorby</groupId>
            <artifactId>maven-surefire-junit5-tree-reporter</artifactId>
            <version>1.1.0</version>
        </dependency>
    </dependencies>
    <configuration>
        <reportFormat>plain</reportFormat>
        <consoleOutputReporter>
            <disable>true</disable>
        </consoleOutputReporter>
        <statelessTestsetInfoReporter
                implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporterUnicode">
        </statelessTestsetInfoReporter>
    </configuration>
</plugin>
```

### ASCII Output

```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.0.0-M8</version>
    <dependencies>
        <dependency>
            <groupId>me.fabriciorby</groupId>
            <artifactId>maven-surefire-junit5-tree-reporter</artifactId>
            <version>1.1.0</version>
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

## Contribute

You are welcome to contribute to the project, for this just open an issue or issue + PR to ``develop`` branch.

If you want to create your own output based on the [Theme](src/main/java/org/apache/maven/plugin/surefire/report/Theme.java) Enum, feel free to open a PR.

### Debugging

If you ever want to debug the code, please use the following command
```
mvnDebug test
```
Then attach a remote JVM debugger on port 8000
