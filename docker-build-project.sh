#!/bin/sh

# Build a given mvn project using a Java compiler provided by a docker container. 
# Maven is provided by the host to ensure consistency, and the creation of further images. 
# For performance, the Maven cache is also provided by the host at $JCOMPILE_ROOT/.m2 and shared across
# runs; you can override its location by setting MAVEN_CACHE_HOST in the environment.
# As a debugging aid, setting the env var STOP_BEFORE to certain values will stop the script at certain points.
# @author jens dietrich

DOCKER_IMAGE=$1
DOCKER_CONTAINER_BASENAME=$2
PROJECT=$3
# needed for caching
JAR_NAME=$4
TAG=$5
# result destination
RESULT_ROOT_FOLDER=$6
PREP_WORKTREE_CMD="$7"
EXTRA_MVN_ARGS="$8"

# We expect other scripts to live here
JCOMPILE_ROOT=$(git rev-parse --show-toplevel)

# Make the container name unique to each process to enable parallel builds
DOCKER_CONTAINER="${DOCKER_CONTAINER_BASENAME}__pid$$"

TMP_LOG="build.$$.log"

RESULT_FOLDER=${RESULT_ROOT_FOLDER}/${DOCKER_CONTAINER_BASENAME}
RESULT_FILE=${RESULT_FOLDER}/${JAR_NAME}
RESULT_ERROR_LOG=${RESULT_FOLDER}/${JAR_NAME}".error"
JAR_BASENAME="${JAR_NAME%%.jar}"
WORKTREE_HOST="$(pwd)/worktrees/pid$$-$JAR_BASENAME"

echo "docker image: ${DOCKER_IMAGE}"
echo "docker container basename: ${DOCKER_CONTAINER_BASENAME}"
echo "docker container name: ${DOCKER_CONTAINER}"
echo "project name: ${PROJECT}"
echo "project jar to be generated: ${JAR_NAME}"
echo "project tag: ${TAG}"
echo "result root folder: ${RESULT_ROOT_FOLDER}"
echo "result folder: ${RESULT_FOLDER}"
echo "host worktree: ${WORKTREE_HOST}"
echo ""



if test -f "${RESULT_FILE}"; then
	echo ""
    echo "USE CACHE -- ${RESULT_FILE} already exists, no compilation needed -- delete file to recompile" 
    # for useability in batch scripts
	echo ""
	echo "================================================"
	echo ""
    exit 0
fi

if test -f "${RESULT_ERROR_LOG}"; then
	echo ""
    echo "FAILURE  -- previous compilation has failed, details in: ${RESULT_ERROR_LOG} , delete ${RESULT_ERROR_LOG} to attempt new build" 
    # for useability in batch scripts
	echo ""
	echo "================================================"
	echo ""
    exit 0
fi

KEEP_ONLY_CLASSES_IN_JAR="$JCOMPILE_ROOT/keep-only-classes-in-jar.sh"
if test '!' -x "$KEEP_ONLY_CLASSES_IN_JAR"; then
	echo "Cannot find executable $KEEP_ONLY_CLASSES_IN_JAR."
	exit 1
fi

DETECT_BYTECODE_FEATURES="$JCOMPILE_ROOT/detect-bytecode-features.pl"
if test '!' -x "$DETECT_BYTECODE_FEATURES"; then
	echo "Cannot find executable $DETECT_BYTECODE_FEATURES."
	exit 1
fi

# No longer run as it's slow and we don't use the results
DETECT_BYTECODE_FEATURES_OLDJEP181="$JCOMPILE_ROOT/detect-bytecode-features-OLDJEP181.pl"
if test '!' -x "$DETECT_BYTECODE_FEATURES_OLDJEP181"; then
	echo "Cannot find executable $DETECT_BYTECODE_FEATURES_OLDJEP181."
	exit 1
fi

mkdir -p ${RESULT_FOLDER}

DATASET_HOST="$JCOMPILE_ROOT/dataset"
DATASET_CONTAINER="/dataset"

echo "checking out tag ${TAG} to ${WORKTREE_HOST}"
mkdir -p $(dirname "${WORKTREE_HOST}")
# Need umask to make all dirs writable by high-valued docker uid if --userns-remap is in force
( umask 0; git -C "${DATASET_HOST}/${PROJECT}" worktree add --detach "${WORKTREE_HOST}" "tags/${TAG}" )

if test -n "$PREP_WORKTREE_CMD"; then
	#( umask 0; cd "$WORKTREE_HOST"; $PREP_WORKTREE_CMD )
	( umask 0; cd "$JCOMPILE_ROOT"; $PREP_WORKTREE_CMD "$WORKTREE_HOST" )
fi

MAVEN_HOST="$JCOMPILE_ROOT/apache-maven-3.9.2"
MAVEN_CONTAINER="/apache-maven"

echo "using data folder ${DATASET_HOST}"

MAVEN_CACHE_HOST=${MAVEN_CACHE_HOST:-$JCOMPILE_ROOT/.m2}     # Default to $JCOMPILE_ROOT/.m2 unless env var already set
echo "using Maven cache dir ${MAVEN_CACHE_HOST}"
MAVEN_CACHE_CONTAINER="/maven-cache"

PROJECT2BUILD=${DATASET_CONTAINER}


# run docker image from hub with java and mvn
# share & reuse maven cache for performance

#docker stop $DOCKER_CONTAINER
docker pull $DOCKER_IMAGE
#docker start $DOCKER_CONTAINER
# We no longer use '--user $(id -u):$(id -g)' since if --userns-remap is in force, that winds up creating restricted-perm files
# under . at random intervals which we can't clean up from the host. See https://github.com/binaryeq/jcompile/pull/10#issuecomment-1771989895
[ "$STOP_BEFORE" = "docker-run" ] && exit
docker run \
	-dt \
	--volume ${WORKTREE_HOST}:${DATASET_CONTAINER} \
	--volume ${MAVEN_CACHE_HOST}:${MAVEN_CACHE_CONTAINER} \
	--volume ${MAVEN_HOST}:${MAVEN_CONTAINER} \
	--workdir $PROJECT2BUILD \
	--name $DOCKER_CONTAINER $DOCKER_IMAGE \

echo "building project"
# "docker exec -it" fails if stdin is not a terminal, e.g., when running in the background
# "umask 0" to make downloaded artifacts, which will be saved in the shared cache as root, can be modified/deleted from the host. "exec" to keep mvn as pid 1, important for docker signal handling.
[ "$STOP_BEFORE" = "mvn" ] && exit
docker exec -t $DOCKER_CONTAINER sh -c "umask 0; exec ${MAVEN_CONTAINER}/bin/mvn -Dmaven.repo.local=${MAVEN_CACHE_CONTAINER} -Drat.skip=true -DskipTests -Dmaven.javadoc.skip=true -Dcyclonedx.skip=true -Danimal.sniffer.skip=true $EXTRA_MVN_ARGS clean package" | sed $'s,\x1b\\[[0-9;]*[a-zA-Z],,g' | tee ${TMP_LOG}
# Some projects make files with restricted perms even if umask 0 is in force, and if --userns-remap is in force we otherwise wouldn't be able to delete them on the host afterwards.
# Ignore "permission denied" on the top-level dir as it's owned by the host uid -- easier than trying to use wildcards to correctly get dotfiles and dotdirs.
[ "$STOP_BEFORE" = "chmod" ] && exit
docker exec -t $DOCKER_CONTAINER chmod -R a+rwX .

echo ""
[ "$STOP_BEFORE" = "cp" ] && exit
if test -f "${WORKTREE_HOST}/target/${JAR_NAME}"; then
	echo "SUCCESS! - copying /target/${JAR_NAME}  into ${RESULT_FOLDER}"
	cp ${WORKTREE_HOST}/target/${JAR_NAME} ${RESULT_FOLDER}

	# At least some projects also create a jar containing compiled tests -- get that too.
	TEST_JAR_PATH="${WORKTREE_HOST}/target/${JAR_NAME%%.jar}-tests.jar"
	if test -f $TEST_JAR_PATH; then
		echo "Found tests jar $TEST_JAR_PATH, copying that also but stripping out non-class files."
		"$KEEP_ONLY_CLASSES_IN_JAR" "$TEST_JAR_PATH" "$RESULT_FOLDER/${TEST_JAR_PATH##*/}"
	fi

	# Gather some additional metadata
	( cd "${WORKTREE_HOST}" && find target/generated-sources target/generated-test-sources -type f -name '*.java' | sort ) > "${RESULT_FOLDER}/${JAR_NAME}.generated-sources"	# Failure here creates a 0-length file, which is fine
	( cd "${WORKTREE_HOST}" && find target -type f -name '*.class' | xargs "$DETECT_BYTECODE_FEATURES" | sort ) > "${RESULT_FOLDER}/${JAR_NAME}.bytecode-features"
else 
	echo "FAILURE! - copying error logs into ${RESULT_ERROR_LOG}"
	cp ${TMP_LOG} ${RESULT_ERROR_LOG}
fi

[ "$STOP_BEFORE" = "docker-stop" ] && exit
docker stop $DOCKER_CONTAINER
docker rm $DOCKER_CONTAINER  # to avoid container with this name already in use

[ "$STOP_BEFORE" = "remove-worktree" ] && exit
git -C "${DATASET_HOST}/${PROJECT}" worktree remove -f "${WORKTREE_HOST}"

# for useability in batch scripts
echo ""
echo "================================================"
echo ""
