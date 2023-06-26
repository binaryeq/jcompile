#!/bin/sh

# script to compile projects using different java compilers
# author jens dietrich


compilers=`cat java-compilers.json`
for row in $(echo "${compilers}" | jq -r '.[] | @base64'); do
    _jq() {
     echo ${row} | base64 --decode | jq -r ${1}
    }


    CONTAINER_NAME="$(_jq '.name')"
    IMAGE="$(_jq '.image')"

   	echo " container name: ${CONTAINER_NAME}"
   	echo " docker image: ${IMAGE}"

    sh ./docker-build-project.sh ${IMAGE} ${CONTAINER_NAME}

done