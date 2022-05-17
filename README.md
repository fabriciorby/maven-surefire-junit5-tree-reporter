# Maven Surefire JUnit5 TreeView Extension

This is a dependency for [maven-surefire-plugin](https://maven.apache.org/surefire/maven-surefire-plugin/), it adds a tree view for the unit tests executed using JUnit5.

## Output

It's in WIP yet, but the output is something like this:

![alt text](https://i.imgur.com/qMb4eoC.png "output")

## Installation

The Maven Repository can be found [here](https://mvnrepository.com/artifact/me.fabriciorby/maven-surefire-junit5-tree-reporter).

Just let your pom.xml be like this.

```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.0.0-M6</version>
    <dependencies>
        <dependency>
            <groupId>me.fabriciorby</groupId>
            <artifactId>maven-surefire-junit5-tree-reporter</artifactId>
            <version>0.1.1</version>
        </dependency>
    </dependencies>
    <configuration>
        <reportFormat>plain</reportFormat>
        <consoleOutputReporter>
            <disable>true</disable>
        </consoleOutputReporter>
        <statelessTestsetInfoReporter implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter">
        </statelessTestsetInfoReporter>
    </configuration>
</plugin>
```

The important thing here is to set ``reportFormat`` as ``plain``, disable the console output and use our new class ``JUnit5StatelessTestsetInfoTreeReporter`` to print the results.

## Known bugs

The maven-sunfire-plugin doesn't handle ``@Nested`` tests in the desired order so our result tree is not perfect yet. I've forked the maven-sunfire project and debugged it but still couldn't figure out how to solve this my myself...
