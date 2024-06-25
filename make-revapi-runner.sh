#!/bin/sh

# Input on stdin should consist of the logging output of AdjacentVersionSameArtifactAndCompilerJarOracle (lines of the form "analysing XYZ.jar vs ABC.jar", or "XYZ.jar\tABC.jar").
# Outputs a Makefile to stdout that will run revapi on each mentioned pair of jars and store the JSON output in a file that mentions both jars' versions.
# Of the possible minimum severities (EQUIVALENT, NON_BREAKING, POTENTIALLY_BREAKING or BREAKING (the default)), we only care about BREAKING and POTENTIALLY_BREAKING.

REVAPI_BASE_CMD="revapi.sh --extensions=org.revapi:revapi-java:0.28.1,org.revapi:revapi-reporter-json:0.5.0 -Drevapi.reporter.json.minSeverity"		# Must be followed by, e.g., "=BREAKING"

echo "all: all.BREAKING all.POTENTIALLY_BREAKING"		# Make defaults to first goal
echo
echo "JCOMPILE_ROOT := $(git rev-parse --show-toplevel)"
echo
echo "%.tsv: %.json"
/bin/echo -e "\\t\$(JCOMPILE_ROOT)/summarise-revapi-json-to-tsv.sh < \$< > \$@"
echo
perl -lne 'sub f($) { my ($MINSEVERITY) = @_; if (m%^(?:analysing: )?(\S+)\.jar(?: vs |\t)(\S+/([^/]+))\.jar%) { my $bn = "$1__vs__$3.revapi.$MINSEVERITY"; print "$bn.json:\n\t'"$REVAPI_BASE_CMD"'=$MINSEVERITY --old=$1.jar --new=$2.jar -Drevapi.reporter.json.output=\$\@\nall.$MINSEVERITY: $bn.tsv\n"; } } f("BREAKING"); f("POTENTIALLY_BREAKING");'
