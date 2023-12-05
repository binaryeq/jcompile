#!/bin/sh

perl -lne 'if (m|^analysing: (\S+)\.jar vs (\S+/([^/]+))\.jar|) { print "revapi.sh --extensions=org.revapi:revapi-java:0.28.1,org.revapi:revapi-reporter-json:0.5.0 --old=$1.jar --new=$2.jar -Drevapi.reporter.json.minSeverity=BREAKING -Drevapi.reporter.json.output=$1__vs__$3.revapi.json"; }' > run_revapi_on_all_relevant_pairs.sh
