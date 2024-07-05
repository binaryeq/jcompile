#!/bin/sh
find jars/EQ -name '*.jar' | grep -v -- '-test\.jar$' | wc -l
