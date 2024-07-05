#!/bin/bash
printf "%'d" $(cd $DATASET_ROOT && wc -l NEQ2.tsv | perl -lne 'print $_ - 1')
