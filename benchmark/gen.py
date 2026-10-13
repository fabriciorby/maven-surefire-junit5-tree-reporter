#!/usr/bin/env python3
"""Generate a Maven project with many JUnit 5 test classes to benchmark the tree reporter.

Classes can have @Nested inner classes, failing tests with long stack traces and messages,
stdout output, and can run with JUnit parallel execution. See benchmark/README.md.

usage: gen.py OUT_DIR [--classes N] [--tests M] [--nested-every K] [--packages P]
                      [--fail-every F] [--stack-depth D] [--msg-kb K] [--stdout-kb S]
                      [--parallel] [--surefire VERSION]
Reporter is switched with -Dtree.reporter.version=... ; run with -Pbaseline for the default
surefire console reporter (no tree reporter).
"""
import argparse, os

p = argparse.ArgumentParser()
p.add_argument("out", help="directory of the generated project")
p.add_argument("--classes", type=int, default=5000, help="number of top-level test classes")
p.add_argument("--tests", type=int, default=5, help="test methods per top-level class")
p.add_argument("--nested-every", type=int, default=5, help="every K-th class gets @Nested inner classes (0 = none)")
p.add_argument("--packages", type=int, default=50, help="classes are spread over this many packages")
p.add_argument("--fail-every", type=int, default=0, help="every F-th test method fails (0 = none)")
p.add_argument("--stack-depth", type=int, default=50, help="extra stack frames in each failure")
p.add_argument("--msg-kb", type=int, default=1, help="size of each failure message, in KB")
p.add_argument("--stdout-kb", type=int, default=0, help="KB printed to stdout by each test")
p.add_argument("--parallel", action="store_true", help="JUnit parallel, classes concurrent (as in the issue)")
p.add_argument("--surefire", default="3.6.0", help="maven-surefire-plugin version")
a = p.parse_args()

CONF = """
      <reportFormat>plain</reportFormat>
      <consoleOutputReporter><disable>true</disable></consoleOutputReporter>
      <statelessTestsetInfoReporter implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter"/>"""

POM = f"""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>bench</groupId><artifactId>tree-reporter-bench</artifactId><version>1</version>
  <properties>
    <maven.compiler.source>1.8</maven.compiler.source><maven.compiler.target>1.8</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <tree.reporter.version>2.0.0</tree.reporter.version>
  </properties>
  <dependencies>
    <dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter</artifactId><version>5.14.0</version><scope>test</scope></dependency>
  </dependencies>
  <build><plugins><plugin>
    <artifactId>maven-surefire-plugin</artifactId><version>{a.surefire}</version>
    <dependencies><dependency>
      <groupId>me.fabriciorby</groupId><artifactId>maven-surefire-junit5-tree-reporter</artifactId>
      <version>${{tree.reporter.version}}</version>
    </dependency></dependencies>
    <configuration>
      <testFailureIgnore>true</testFailureIgnore>
      <trimStackTrace>false</trimStackTrace>{CONF}
    </configuration>
  </plugin></plugins></build>
  <profiles><profile><id>baseline</id>
    <build><plugins><plugin>
      <artifactId>maven-surefire-plugin</artifactId>
      <configuration combine.self="override">
        <testFailureIgnore>true</testFailureIgnore>
        <trimStackTrace>false</trimStackTrace>
        <reportFormat>plain</reportFormat>
      </configuration>
    </plugin></plugins></build>
  </profile></profiles>
</project>
"""

HELPER = f"""package bench;

public final class Fail {{
    private static final String MSG = new String(new char[{a.msg_kb * 1024}]).replace('\\0', 'x');
    private static final String OUT = new String(new char[{max(a.stdout_kb, 0) * 1024}]).replace('\\0', 'o');

    public static void out() {{ if (!OUT.isEmpty()) System.out.println(OUT); }}

    public static void fail(int depth) {{
        if (depth > 0) {{ fail(depth - 1); return; }}
        throw new AssertionError("expected failure " + MSG);
    }}
}}
"""

counter = [0]
def method(i, indent):
    counter[0] += 1
    body = "bench.Fail.out(); "
    if a.fail_every and counter[0] % a.fail_every == 0:
        body += f"bench.Fail.fail({a.stack_depth});"
    else:
        body += f"assertEquals({i}, {i});"
    return f"{indent}@Test void test{i}() {{ {body} }}"

def methods(n, indent):
    return "\n".join(method(i, indent) for i in range(n))

os.makedirs(a.out, exist_ok=True)
open(os.path.join(a.out, "pom.xml"), "w").write(POM)
hd = os.path.join(a.out, "src/test/java/bench")
os.makedirs(hd, exist_ok=True)
open(os.path.join(hd, "Fail.java"), "w").write(HELPER)
if a.parallel:
    rd = os.path.join(a.out, "src/test/resources")
    os.makedirs(rd, exist_ok=True)
    open(os.path.join(rd, "junit-platform.properties"), "w").write(
        "junit.jupiter.execution.parallel.enabled=true\n"
        "junit.jupiter.execution.parallel.mode.default=same_thread\n"
        "junit.jupiter.execution.parallel.mode.classes.default=concurrent\n")
for c in range(a.classes):
    pkg = f"bench.p{c % a.packages}"
    d = os.path.join(a.out, "src/test/java", *pkg.split("."))
    os.makedirs(d, exist_ok=True)
    name = f"Generated{c}Test"
    nested = ""
    if a.nested_every and c % a.nested_every == 0:
        nested = f"""
    @Nested class Inner {{
{methods(2, "        ")}
        @Nested class Deeper {{
{methods(2, "            ")}
        }}
    }}"""
    src = f"""package {pkg};

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class {name} {{
{methods(a.tests, "    ")}{nested}
}}
"""
    open(os.path.join(d, name + ".java"), "w").write(src)
print(f"generated {a.classes} classes, {counter[0]} tests in {a.out}")
