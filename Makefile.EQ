all: EQ.tsv

EQ.tsv: jars/EQ
	( time java -cp ../../oracle-construction/target/jcompile.jar nz.ac.wgtn.shadedetector.jcompile.oracles.SameArtifactDifferentCompilerFromDotClassOracle jars/EQ > EQ.tsv ) 2>&1 | tee make_EQ_tsv.log

#TODO: Change docker-build-all.sh to create jars under jars/EQ instead of jars, so that we don't need the rename dance at the end.
jars/EQ:
	( time make -j 3 --output-sync -f Makefile.EQ_jars ) 2>&1 | tee make_EQ_jars.log
	mv jars EQ
	mkdir jars
	mv EQ jars
