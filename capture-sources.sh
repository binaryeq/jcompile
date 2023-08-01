#!/bin/sh

# script to capture source for each project + version in the dataset
# author jens dietrich

projects=`cat dataset.json`

ROOT="$(pwd)"
SOURCES="${ROOT}/sources"
DATASET="${ROOT}/dataset"

for row in $(echo "${projects}" | jq -r '.[] | @base64'); do
    _jq() {
     	echo ${row} | base64 --decode | jq -r ${1}
    }
    PROJECT_NAME="$(_jq '.name')"
	PROJECT_TAG="$(_jq '.tag')"
	echo "\tproject name: ${PROJECT_NAME}"
	echo "\tproject tag: ${PROJECT_TAG}"
	echo ""

	PROJECT_DIR=${DATASET}/${PROJECT_NAME}
	cd ${PROJECT_DIR}

	git checkout tags/${PROJECT_TAG}
	mvn source:jar -DbuildDir=${SOURCES} -Dmaven.repo.local=${ROOT}/.m2  -Drat.skip=true -Dcyclonedx.skip=true 
	cp -f ${PROJECT_DIR}/target/*-sources.jar ${SOURCES}

done

# cleanup
cd ${SOURCES}
rm -f *-test-sources.jar 
echo "-------------------------------------"

