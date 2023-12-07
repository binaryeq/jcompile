#!/bin/sh

# Input on stdin should consist of the logging output of AdjacentVersionSameArtifactAndCompilerClassOracle (lines of the form "analysing XYZ.jar vs ABC.jar").
# Outputs a shell script to stdout that will run revapi on each mentioned pair of jars and store the JSON output in a file that mentions both jars' versions.
# Optionally supply a minimum severity (EQUIVALENT, NON_BREAKING, POTENTIALLY_BREAKING or BREAKING (the default)) on the command line.
# If the first argument is --output-makefile, a Makefile will be written instead of a shell script. This can make continuing incomplete runs and running in parallel easier.

if [ "$1" = "--output-makefile" ]
then
	MODE=make
	shift
else
	MODE=script
fi

MINSEVERITY="${1:-BREAKING}"
REVAPI_BASE_CMD="revapi.sh --extensions=org.revapi:revapi-java:0.28.1,org.revapi:revapi-reporter-json:0.5.0 -Drevapi.reporter.json.minSeverity"		# Must be followed by, e.g., "=BREAKING"

if [ $MODE = make ]
then
	echo "all:"		# Make defaults to first goal
	echo
	echo "MINSEVERITY := $MINSEVERITY"		# So the value passed to this script becomes the default, but it can be overridden at make time
	echo "JCOMPILE_ROOT := $(git rev-parse --show-toplevel)"
	echo
	echo "%.tsv: %.json"
	/bin/echo -e "\\t\$(JCOMPILE_ROOT)/summarise-revapi-json-to-tsv.sh < \$< > \$@"
	echo
	perl -lne 'if (m%^(?:analysing: )?(\S+)\.jar(?: vs |\t)(\S+/([^/]+))\.jar%) { my $bn = "$1__vs__$3.revapi.\$(MINSEVERITY)"; print "$bn.json:\n\t'"$REVAPI_BASE_CMD"'=\$(MINSEVERITY) --old=$1.jar --new=$2.jar -Drevapi.reporter.json.output=\$\@\nall: $bn.tsv\n"; }'
else
	perl -lne 'if (m%^(?:analysing: )?(\S+)\.jar(?: vs |\t)(\S+/([^/]+))\.jar%) { print "'"$REVAPI_BASE_CMD=$MINSEVERITY"' --old=$1.jar --new=$2.jar -Drevapi.reporter.json.output=$1__vs__$3.revapi.'$MINSEVERITY'.json"; }'
fi
