#!/bin/sh

case "$1" in
	JEP181)
		javap -c -v "$2" | grep -E -A 2 'static .* access\$[0-9]+\(' | grep -q -E -w 'ACC_SYNTHETIC'
		;;

	JEP280)
		javap -c -v "$2" | grep -q -E '^ *#[0-9]+ = Class +#[0-9]+ +// java/lang/invoke/StringConcatFactory$'
		;;
esac
