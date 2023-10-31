#!/bin/sh

case "$1" in
	JEP181)
		# JDK >= 11 uses "nests" to allow inner classes to access private members of outer classes directly, instead of creating synthetic methods in the outer class: https://openjdk.org/jeps/181
		# A positive detection (exit code 0) means the *old* (JDK < 11) behaviour, since this is easier to test for.
		javap -c -v "$2" | grep -E -A 2 'static .* access\$[0-9]+\(' | grep -q -E -w 'ACC_SYNTHETIC'
		;;

	JEP280)
		# JDK >= 9 uses special invokedynamic calls to concatenate string literals instead of StringBuilder: https://openjdk.org/jeps/280, https://docs.oracle.com/javase/9/docs/api/java/lang/invoke/StringConcatFactory.html
		# A positive detection (exit code 0) means the *new* (JDK >= 9) behaviour, since this is easier to test for.
		javap -c -v "$2" | grep -q -E '^ *#[0-9]+ = Class +#[0-9]+ +// java/lang/invoke/StringConcatFactory$'
		;;

	*)
		echo "Unrecognised feature '$1', aborting."
		exit 2
		;;
esac
