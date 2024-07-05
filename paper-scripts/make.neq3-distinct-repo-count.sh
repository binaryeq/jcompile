#!/bin/bash
printf "%'d" $(cd $DATASET_ROOT && cut -f 10 NEQ3.tsv|sort -u|wc -l|perl -lne 'print $_ - 1')
