#!/bin/sh

case "$1" in
	JEP181)
		javap -c -v "$2" | grep -E -A 2 'static .* access\$[0-9]+\(' | grep -q -E -w 'ACC_SYNTHETIC'
		;;

	JEP280)
		javap -c -v "$2" | grep -q -E '// InvokeDynamic #0:makeConcatWithConstants:\(Ljava/lang/String;Ljava/lang/String;\)Ljava/lang/String;$'
		;;
esac
