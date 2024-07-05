#!/bin/bash
printf "%'d" $(cd $JMUTATOR_ROOT && grep '^verification failed ' jars/NEQ2/openjdk-11.0.19/*.stdout|wc -l)
