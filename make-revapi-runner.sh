#!/bin/sh

# Input on stdin should consist of the logging output of AdjacentVersionSameArtifactAndCompilerClassOracle (lines of the form "analysing XYZ.jar vs ABC.jar").
# Outputs a shell script to stdout that will run revapi on each mentioned pair of jars and store the JSON output in a file that mentions both jars' versions.
# Optionally supply a minimum severity (EQUIVALENT, NON_BREAKING, POTENTIALLY_BREAKING or BREAKING (the default)) on the command line.

MINSEVERITY="${1:-BREAKING}"

perl -lne 'if (m|^analysing: (\S+)\.jar vs (\S+/([^/]+))\.jar|) { print "revapi.sh --extensions=org.revapi:revapi-java:0.28.1,org.revapi:revapi-reporter-json:0.5.0 --old=$1.jar --new=$2.jar -Drevapi.reporter.json.minSeverity=$MINSEVERITY -Drevapi.reporter.json.output=$1__vs__$3.revapi.$MINSEVERITY.json"; }' > run_revapi_on_all_relevant_pairs.sh
