#!/bin/bash
printf "%'d" $(wc -l NEQ1.tsv | perl -lne 'print $_ - 1')
