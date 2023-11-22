#!/bin/sh

# For each pathname of the form path/to/ecj-*.jar on the command line, report the eclipse release (filename part after '-'), ecj version and maximum supported JDK version in tab-separated form.

/usr/bin/echo -e "eclipse_release\tecj_version\tmax_supported_jdk"

for ecj in "$@"
do
	bn=${ecj##*/ecj-}
	eclipse_release=${bn%%.jar}
	ecj_version=$(java -jar $ecj -version | sed -E 's/^Eclipse Compiler for Java.*, (3\.[0-9]+\.[0-9]+).*/\1/')
	max_supported_jdk=$(java -jar $ecj -help | grep -- '^ *-[0-9]' | tail -1 | sed -E 's/^ *(-1\.[0-9]+ *)?-([0-9]+).*/\2/')
	/usr/bin/echo -e "$eclipse_release\t$ecj_version\t$max_supported_jdk"
done
