#!/bin/sh

case "$1" in
	JEP181)
		javap -c -v "$2" | grep -E 'static .* access\$[0-9]+\('
		;;

	JEP280)
		javap -c -v "$2" | grep -E '// InvokeDynamic #0:makeConcatWithConstants:\(Ljava/lang/String;Ljava/lang/String;\)Ljava/lang/String;$'
		;;
esac
