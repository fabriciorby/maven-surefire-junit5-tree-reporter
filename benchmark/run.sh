#!/bin/bash
# Runs the tests of a generated project once and prints the wall time and the peak heap
# after GC of the Maven JVM (where the reporter runs) and of the forked test JVM.
#
# usage: run.sh PROJECT_DIR LABEL [extra maven args...]
#   MAVEN_XMX=128m run.sh ...   also limits the Maven JVM heap (a heap dump is written on OOME)
# Logs go to ./logs/LABEL*.
set -u
proj=$1; label=$2; shift 2
logs=$PWD/logs
mkdir -p "$logs"
rm -f "$logs/$label"-*.gc "$logs/$label"-fork-*.gc "$logs/$label".hprof

start=$(date +%s.%N)
MAVEN_OPTS="${MAVEN_XMX:+-Xmx$MAVEN_XMX} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$logs/$label.hprof \
-XX:+UseSerialGC -Xlog:gc:file=$logs/$label-maven.gc" \
  mvn -B -f "$proj/pom.xml" surefire:test \
  -DargLine="-XX:+UseSerialGC -Xlog:gc:file=$logs/$label-fork-%p.gc" "$@" > "$logs/$label.out" 2>&1
rc=$?
end=$(date +%s.%N)

peak() { cat $1 2>/dev/null | grep -oE '[0-9]+M->[0-9]+M' | sed -E 's/.*->([0-9]+)M/\1/' | sort -n | tail -1; }
oome=$(grep -c OutOfMemoryError "$logs/$label.out")
printf "%-24s rc=%s time=%6.1fs mavenPeakAfterGC=%sM forkPeakAfterGC=%sM oome=%s\n" \
  "$label" "$rc" "$(awk "BEGIN{print $end-$start}")" "$(peak "$logs/$label-maven.gc")" "$(peak "$logs/$label-fork-*.gc")" "$oome"
