#!/bin/sh

# script to compile projects using different java compilers
# author jens dietrich


compilers=`cat java-compilers.json`
projects=`cat dataset.json`
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
    	#echo "\tproject name: ${PROJECT_NAME}"
   		#echo "\tproject tag: ${PROJECT_TAG}"
   		echo "\tproject jar: ${PROJECT_JAR}"
   		echo ""

   		sh ./docker-build-project.sh ${IMAGE} ${CONTAINER_NAME} ${PROJECT_NAME} ${PROJECT_JAR} ${PROJECT_TAG}
	done
	echo "-------------------------------------"

done