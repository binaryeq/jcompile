#!/bin/bash
printf "%'d" $(cd $DATASET_ROOT && zcat EQ.tsv.gz | wc -l - NEQ[123].tsv | perl -lne 'print $1 - 4 if /(\d+) total/')
