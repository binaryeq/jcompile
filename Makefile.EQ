# Makefile to (re)build EQ1.tsv and its prerequisites, notably the jars under jars/EQ.
# Run this from a subdirectory of runs/. It will produce a jars/EQ directory and EQ.tsv.

JCOMPILE_ROOT := $(shell git rev-parse --show-toplevel)

all: EQ.tsv

EQ.tsv: jars/EQ
	( time java -cp $(JCOMPILE_ROOT)/oracle-construction/target/jcompile.jar nz.ac.wgtn.shadedetector.jcompile.oracles.SameArtifactDifferentCompilerFromDotClassOracle jars/EQ > $@.tsv ) 2>&1 | tee make.$@.log

#TODO: Change docker-build-all.sh to create jars under jars/EQ instead of jars, so that we don't need the rename dance at the end.
jars/EQ:
	( time make -j 3 --output-sync -f $(JCOMPILE_ROOT)/Makefile.EQ_jars ) 2>&1 | tee make.EQ_jars.log
	mv jars EQ
	mkdir jars
	mv EQ jars
