#!/bin/sh
find jars/EQ -name '*.jar' | grep -v -- '-tests\.jar$' | wc -l
