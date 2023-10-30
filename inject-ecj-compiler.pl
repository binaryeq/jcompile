#!/usr/bin/perl -i.bak

# Usage: inject-ecj-compiler <plexus-compiler-eclipse-version> pom.xml
#
# pom.xml will be *replaced*, with the old version backed up to pom.xml.bak.

use strict;
use warnings;

my $v = shift;
my $inBuild = 0;
my $success = 0;

while (<>) {
	print;

	if (m|\A\s*<(/?)build>\s*\z|) {
		$inBuild = ($1 ne '/');
	}

	if ($inBuild && /\A\s*<plugins>\s*\z/) {
		print <<THE_END;
      <!-- Following plugin added automatically by $0, based on https://stackoverflow.com/a/33165304/47984 -->
      <plugin>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.6.0</version>
        <configuration>
          <compilerId>eclipse</compilerId>
        </configuration>
        <dependencies>
          <dependency>
            <groupId>org.codehaus.plexus</groupId>
            <artifactId>plexus-compiler-eclipse</artifactId>
            <version>$v</version>
          </dependency>
        </dependencies>
      </plugin>

THE_END
		$success = 1;
	}
}

if (!$success) {
	print STDERR "Could not find <plugins>!";
	exit 1;
}

exit 0;
