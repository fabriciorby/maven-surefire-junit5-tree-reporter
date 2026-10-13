#!/bin/bash
# Runs the tests of a generated project and takes a class histogram of the Maven JVM
# (jcmd GC.class_histogram) every few seconds, to see which objects stay alive.
#
# usage: histo.sh PROJECT_DIR LABEL [extra maven args...]
#   INTERVAL=8 histo.sh ...   seconds between histograms (default 8)
# Histograms go to ./logs/LABEL-histo-N.txt, the last one is taken close to the end of the run.
set -u
proj=$1; label=$2; shift 2
interval=${INTERVAL:-8}
logs=$PWD/logs
mkdir -p "$logs"
rm -f "$logs/$label"-histo-*.txt

mvn -B -f "$proj/pom.xml" surefire:test "$@" > "$logs/$label-histo.out" 2>&1 &
mvn_pid=$!
pid=""
while [ -z "$pid" ] && kill -0 $mvn_pid 2>/dev/null; do
  sleep 1
  pid=$(jcmd -l | grep -i 'plexus.classworlds' | awk '{print $1}' | head -1)
done
i=0
while [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; do
  sleep "$interval"; i=$((i+1))
  jcmd "$pid" GC.class_histogram > "$logs/$label-histo-$i.txt" 2>/dev/null || rm -f "$logs/$label-histo-$i.txt"
done
wait $mvn_pid
echo "$(ls "$logs/$label"-histo-*.txt 2>/dev/null | wc -l) histograms in $logs"
