#!/bin/sh

if [ $# != 1 ]; then
	echo "Must specify path to EQ.tsv file." >&2
	exit 1
fi

DB=bineq.sqlite

# These env vars need to be seen by the SQL script's .import statement
export EQTSV="$1"
export RUNID=$(basename $(dirname "$EQTSV"))

echo "EQTSV=<$EQTSV>"
echo "RUNID=<$RUNID>"

sqlite3 $DB < import_EQ.sql
