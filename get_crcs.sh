#!/bin/sh

# A simple script to extract .class filenames and their CRC-32 fields from jar files.

for j in runs/$1/jars/*/*.jar
do
	echo $j
	unzip -l -v $j | perl -lane 'print join("\t", $F[7], $F[6]) if $F[7] =~ /\.class$/' | sort > $j.crc
done
