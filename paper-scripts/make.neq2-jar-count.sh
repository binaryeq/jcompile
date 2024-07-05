#!/bin/bash
printf "%'d" $(ls $JMUTATOR_ROOT/jars/EQ/openjdk-11.0.19/|wc -l)
