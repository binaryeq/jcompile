#!/bin/bash
printf "%'d" $(find jars/EQ -name '*-tests.jar' | wc -l)
