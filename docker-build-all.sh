#!/bin/sh

# script to compile projects using different java compilers
# author jens dietrich


JCOMPILE_ROOT=$(git rev-parse --show-toplevel)

compilers=`cat $JCOMPILE_ROOT/java-compilers.json`
#compilers=`cat $JCOMPILE_ROOT/java-compilers-debug.json`
projects=`cat $JCOMPILE_ROOT/dataset.json`
#projects=`cat $JCOMPILE_ROOT/dataset-debug.json`


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
JARS="jars/EQ"
for row in $(echo "${compilers}" | jq -r '.[] | @base64'); do
    _jq() {
     	echo "$row" | base64 --decode | jq -r "$1"
    }


    CONTAINER_NAME="$(_jq '.name')"
    IMAGE="$(_jq '.image')"
    PREP_WORKTREE_CMD="$(_jq '.prep_worktree_cmd + '\"\")"
    EXTRA_MVN_ARGS="$(_jq '.extra_mvn_args + '\"\")"

   	#echo "container name: ${CONTAINER_NAME}"
   	echo "# ---- compiling projects using: ${IMAGE} with extra mvn args '$EXTRA_MVN_ARGS' then ${PREP_WORKTREE_CMD:-(nothing further)} -----"
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
			/usr/bin/echo -e '\t'$JCOMPILE_ROOT/docker-build-project.sh ${IMAGE} ${CONTAINER_NAME} ${PROJECT_NAME} ${PROJECT_JAR} ${PROJECT_TAG} ${JARS} "'$PREP_WORKTREE_CMD'" "'$EXTRA_MVN_ARGS'" '&& touch $@'
			echo
		else
			$ECHO_IF_DRY_RUN sh $JCOMPILE_ROOT/docker-build-project.sh ${IMAGE} ${CONTAINER_NAME} ${PROJECT_NAME} ${PROJECT_JAR} ${PROJECT_TAG} ${JARS} "$SQUOTE$PREP_WORKTREE_CMD$SQUOTE" "$SQUOTE$EXTRA_MVN_ARGS$SQUOTE"
		fi
	done
	echo "# -------------------------------------"

done
