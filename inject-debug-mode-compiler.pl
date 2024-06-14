#!/usr/bin/perl -i.bak

# Usage: inject-debug-mode-compiler path/to/pom.xml
#        inject-debug-mode-compiler path/to/dir-with-pom
#
# In the second case, where the argument is a directory, "/pom.xml" will be auto-appended.
# pom.xml will be *replaced*, with the old version backed up to pom.xml.bak.

use strict;
use warnings;

my $inBuild = 0;
my $success = 0;

print STDERR "ARGV: ", join(',', @ARGV), "\n";
die "Must specify pom.xml or path to it!" if !@ARGV;
if (-d $ARGV[0]) {
	print STDERR "Auto-appending /pom.xml to dir path $ARGV[0]\n";
	$ARGV[0] .= '/pom.xml';		# Simplifies running from build-docker-project.sh
}

while (<>) {
	print;

	if (m|\A\s*<(/?)build>\s*\z|) {
		$inBuild = ($1 ne '/');
	}

	if ($inBuild && /\A(\s*)<plugins>\s*\z/) {
		# Some projects will refuse to build if the indentation is wrong!
		print <<THE_END;
$1  <!-- Following plugin added automatically by $0, based on https://stackoverflow.com/a/33165304/47984 -->
$1  <plugin>
$1    <artifactId>maven-compiler-plugin</artifactId>
$1    <version>3.6.0</version>
$1    <configuration>
$1      <compilerArgs>
$1        <arg>-g</arg>
$1      </compilerArgs>
$1    </configuration>
$1  </plugin>

THE_END
		$success = 1;
	}
}

if (!$success) {
	print STDERR "Could not find <plugins>!";
	exit 1;
}

exit 0;
