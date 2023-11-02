#!/bin/sh

# script to compile projects using different java compilers
# author jens dietrich


compilers=`cat java-compilers.json`
#compilers=`cat java-compilers-debug.json`
projects=`cat dataset.json`
#projects=`cat dataset-debug.json`


ECHO_IF_DRY_RUN=
SQUOTE=
FOR_MAKE=
if [ "$1" = "--dry-run" ]
then
	ECHO_IF_DRY_RUN=echo
	SQUOTE=\'
elif [ "$1" = "--output-make-rules" ]
then
	FOR_MAKE=1
fi

# root result folders
JARS="jars"
for row in $(echo "${compilers}" | jq -r '.[] | @base64'); do
    _jq() {
     	echo "$row" | base64 --decode | jq -r "$1"
    }


    CONTAINER_NAME="$(_jq '.name')"
    IMAGE="$(_jq '.image')"
    PREP_WORKTREE_CMD="$(_jq '.prep_worktree_cmd + '\"\")"

   	#echo "container name: ${CONTAINER_NAME}"
   	echo "# ---- compiling projects using: ${IMAGE} then ${PREP_WORKTREE_CMD:-(nothing further)} -----"
   	#echo ""

    for row2 in $(echo "${projects}" | jq -r '.[] | @base64'); do
	    _jq() {
	     	echo "$row2" | base64 --decode | jq -r "$1"
	    }
	    PROJECT_NAME="$(_jq '.name')"
    	PROJECT_TAG="$(_jq '.tag')"
    	PROJECT_JAR="$(_jq '.jar')"
    	#echo "\tproject name: ${PROJECT_NAME}"
   		#echo "\tproject tag: ${PROJECT_TAG}"
   		#echo "\tproject jar: ${PROJECT_JAR}"
   		#echo ""

		if [ $FOR_MAKE = 1 ]
		then
			echo "ALL_JAR_DONES += $JARS/$CONTAINER_NAME/$PROJECT_JAR.done"
			echo "ALL_PROJECT_$PROJECT_NAME: $JARS/$CONTAINER_NAME/$PROJECT_JAR.done"
			echo "ALL_COMPILER_$CONTAINER_NAME: $JARS/$CONTAINER_NAME/$PROJECT_JAR.done"
			echo "$JARS/$CONTAINER_NAME/$PROJECT_JAR.done:"
			/usr/bin/echo -e '\t'./docker-build-project.sh ${IMAGE} ${CONTAINER_NAME} ${PROJECT_NAME} ${PROJECT_JAR} ${PROJECT_TAG} ${JARS} "'$PREP_WORKTREE_CMD'" '&& touch $@'
			echo
		else
			$ECHO_IF_DRY_RUN sh ./docker-build-project.sh ${IMAGE} ${CONTAINER_NAME} ${PROJECT_NAME} ${PROJECT_JAR} ${PROJECT_TAG} ${JARS} "$SQUOTE$PREP_WORKTREE_CMD$SQUOTE"
		fi
	done
	echo "# -------------------------------------"

done
