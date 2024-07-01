#!/usr/bin/perl

use strict;
use warnings;

my $currentClass;
my %has;
my $inFunction = 0;

sub outputClass() {
	print join("\t", $currentClass, sort grep { $has{$_} } keys %has), "\n";
}

open JAVAP, "-|", "javap", "-c", "-v", @ARGV or die;		# Open a pipe from javap, passing all other command-line args to it
while (<JAVAP>) {
	if (/^Classfile (.*)/) {
		outputClass() if defined $currentClass;
		my $newClass = $1;
		while (@ARGV && $newClass !~ /\Q$ARGV[0]\E$/) {
			print STDERR "Missing javap output for $ARGV[0] -- presumably it hit an error. Ignoring.\n";
			shift;
		}
		$currentClass = shift;
		%has = ();
		$inFunction = 0;
	} else {
		# JDK >= 11 uses "nests" to allow inner classes to access private members of outer classes directly, instead of creating synthetic methods in the outer class: https://openjdk.org/jeps/181
		# A positive detection (exit code 0) means the *old* (JDK < 11) behaviour, since this is easier to test for.
		if (/^  ([^ ].*);$/ && $1 =~ /static .* access\$[0-9]+\(/) {
			$inFunction = 1;
		} elsif (/^}?\s*$/) {
			$inFunction = 0;
		}

		$has{JEP181} = 1 if $inFunction && /ACC_SYNTHETIC|Synthetic: true/;

		# JDK >= 9 uses special invokedynamic calls to concatenate string literals instead of StringBuilder: https://openjdk.org/jeps/280, https://docs.oracle.com/javase/9/docs/api/java/lang/invoke/StringConcatFactory.html
		# A positive detection (exit code 0) means the *new* (JDK >= 9) behaviour, since this is easier to test for.
		$has{JEP280} = 1 if m!^ *#[0-9]+ = Class +#[0-9]+ +// java/lang/invoke/StringConcatFactory$!;
	}
}

outputClass() if defined $currentClass;
