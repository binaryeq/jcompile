#!/usr/bin/perl

use strict;
use warnings;

my $currentClass;
my %has;
my $inFunction = 0;

my $initialDigitRegex = '';		# Will find both (as was the case originally)

if ($ARGV[0] eq '--first-digit-zero') {
	$initialDigitRegex = '0';	# We think this indicates "forward direction" accesses (from inner class to outer class private method/member) that will become NestMembers in JDK >= 11
	shift;
} elsif ($ARGV[0] eq '--first-digit-nonzero') {
	$initialDigitRegex = '[1-9]';	# We think this indicates "reverse direction" accesses (from outer class to inner class) that will never change
	shift;
}

print STDERR "Initial digit regex: <$initialDigitRegex>\n";

sub outputClass() {
	print join("\t", $currentClass, sort grep { $has{$_} } keys %has), "\n";
}

sub getClassesInJar($) {
	my ($jarFName) = @_;
	my @classes = map { m|202\d-\d\d-\d\d \d\d:\d\d (.*)\.class$| ? $1 : () } `unzip -l "$jarFName"`;	# Exclude '.class' from the end, since that's what javap wants with -cp jarfile.jar
}

my $jarFName;
my @classes = @ARGV;
if (@ARGV == 1) {
	if ($ARGV[0] =~ /\.jar$/) {
		# Jar mode
		$jarFName = $ARGV[0];
		@classes = getClassesInJar($ARGV[0]);
		print STDERR "Using jar mode, found " . scalar(@classes) . " classes in $jarFName.\n";
	}
}

open JAVAP, "-|", "javap", "-c", "-v", (defined $jarFName ? ("-cp", $jarFName) : ()), @classes or die;		# Open a pipe from javap, passing all other command-line args to it
while (<JAVAP>) {
	if (/^Classfile (.*)/) {
		outputClass() if defined $currentClass;
		my $newClass = $1;
		while (@classes && $newClass !~ /\Q$classes[0]\E$/) {
			print STDERR "Missing javap output for $classes[0] -- presumably it hit an error. Ignoring.\n";
			shift;
		}
		$currentClass = shift;
		%has = ();
		$inFunction = 0;
	} else {
		# JDK >= 11 uses "nests" to allow inner classes to access private members of outer classes directly, instead of creating synthetic methods in the outer class: https://openjdk.org/jeps/181
		# A positive detection (exit code 0) means the *old* (JDK < 11) behaviour, since this is easier to test for.
		if (/^  ([^ ].*);$/ && $1 =~ /static .* access\$$initialDigitRegex[0-9]+\(/) {
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
