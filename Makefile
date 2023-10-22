# Makefile to (re)build all projects in dataset.json with all compilers in java-compilers.json.
#
# To build everything that is not up-to-date: make
# To do the same, but with up to 4 builds in parallel: make -j 4
# To see everything that would be build: make -n
# To build a specific project (note the ".done" at the end): make jars/openjdk-20.0.1/bcel-6.7.0.jar.done
#
# Automatically regenerates rules to build each jar whenever dataset.json or java-compilers.json changes, by running ducker-build-all.sh --output-make-rules.

# ALL_JAR_DONES is not filled out until after the include, but make takes the first goal as the default.
all: all_defined_after_include

generated_rules_for_all_jars.mk: dataset.json java-compilers.json
	echo "Regenerating make rules in $@ from $^..."
	./docker-build-all.sh --output-make-rules > $@

# GNU make knows to regenerate this, but only when needed
include generated_rules_for_all_jars.mk

all_defined_after_include: $(ALL_JAR_DONES)

.PHONY: all_defined_after_include
