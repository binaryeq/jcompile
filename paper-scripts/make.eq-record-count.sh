#!/bin/bash
printf "%'d" $(zcat EQ.tsv.gz | tail +2 | wc -l)
