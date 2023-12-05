#!/bin/sh

if [ -z "$1" -o -z "$2" ]
then
	echo "Syntax: $0 injar.jar outjar.jar"
	exit 1
fi

INJAR="$1"
OUTJAR="$2"
ABSINJAR=`realpath "$INJAR"`
ABSOUTJAR=`realpath "$OUTJAR"`
DIR=/tmp/keep-only-classes-in-jar-$$

mkdir -p "$DIR"
(
	cd "$DIR"
	unzip "$ABSINJAR"
	find . -name '*.class' > list_of_all_classes.txt
	jar --create --file "$ABSOUTJAR" @list_of_all_classes.txt
)

rm -r "$DIR"
