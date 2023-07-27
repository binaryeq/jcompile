#!/bin/sh

# script to compile projects using the same  java compilers multiple times
# in order to study non-determinism in the compiler
# author jens dietrich

# use only selected compilers / one version per component for repeated builds

compilers=`cat java-compilers-selection.json`
projects=`cat dataset-selection.json`
COMPILER_RUN_COUNT=3
for row in $(echo "${compilers}" | jq -r '.[] | @base64'); do
    _jq() {
     	echo ${row} | base64 --decode | jq -r ${1}
    }


    CONTAINER_NAME="$(_jq '.name')"
    IMAGE="$(_jq '.image')"

   	#echo "container name: ${CONTAINER_NAME}"
   	echo "---- compiling projects using: ${IMAGE}   -----"
   	#echo ""

    for row2 in $(echo "${projects}" | jq -r '.[] | @base64'); do
	    _jq() {
	     	echo ${row2} | base64 --decode | jq -r ${1}
	    }
	    PROJECT_NAME="$(_jq '.name')"
    	PROJECT_TAG="$(_jq '.tag')"
    	PROJECT_JAR="$(_jq '.jar')"
    	echo "\tproject name: ${PROJECT_NAME}"
   		echo "\tproject tag: ${PROJECT_TAG}"
   		echo "\tproject jar: ${PROJECT_JAR}"
   		echo ""

   		for ((i=1;i<=${COMPILER_RUN_COUNT};i++)); do 
   			# echo "\t\t run $i / ${COMPILER_RUN_COUNT}"
   			RESULT_ROOT_FOLDER=repeated/jars-$i
			echo "\t\t results will be generated in  ${RESULT_ROOT_FOLDER}"
			sh ./docker-build-project.sh ${IMAGE} ${CONTAINER_NAME} ${PROJECT_NAME} ${PROJECT_JAR} ${PROJECT_TAG} ${RESULT_ROOT_FOLDER}
		done
		echo ""

	done
	echo "-------------------------------------"

done