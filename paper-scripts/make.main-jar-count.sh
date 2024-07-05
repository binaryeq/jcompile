#!/bin/bash
printf "%'d" $(find jars/EQ -name '*.jar' | grep -v -- '-tests\.jar$' | wc -l)
