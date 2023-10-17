#!/bin/sh

# Build a given mvn project using a Java compiler provided by a docker container. 
# Maven is provided by the host to ensure consistency, and the creation of further images. 
# For performance, the Maven cache is also provided by the host at $(pwd)/.m2 and shared across
# runs; you can override its location by setting MAVEN_CACHE_HOST in the environment.
# @author jens dietrich

DOCKER_IMAGE=$1
DOCKER_CONTAINER=$2
PROJECT=$3
# needed for caching
JAR_NAME=$4
TAG=$5
# result destination
RESULT_ROOT_FOLDER=$6

echo "docker image: ${DOCKER_IMAGE}"
echo "docker container name: ${DOCKER_CONTAINER}"
echo "project name: ${PROJECT}"
echo "project jar to be generated: ${JAR_NAME}"
echo "project tag: ${TAG}"
echo "result root folder: ${RESULT_ROOT_FOLDER}"
echo ""



TMP_LOG="build.log"

RESULT_FOLDER=${RESULT_ROOT_FOLDER}/${DOCKER_CONTAINER}
RESULT_FILE=${RESULT_FOLDER}/${JAR_NAME}
RESULT_ERROR_LOG=${RESULT_FOLDER}/${JAR_NAME}".error"

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


mkdir -p ${RESULT_FOLDER}

DATASET_HOST="$(pwd)/dataset"
DATASET_CONTAINER="/dataset"

echo "checking out tag ${TAG}"
git -C ${DATASET_HOST}/${PROJECT} checkout tags/${TAG}

MAVEN_HOST="$(pwd)/apache-maven-3.9.2"
MAVEN_CONTAINER="/apache-maven"

echo "using data folder ${DATASET_HOST}"

MAVEN_CACHE_HOST=${MAVEN_CACHE_HOST:-$(pwd)/.m2}     # Default to $(pwd)/.m2 unless env var already set
echo "using Maven cache dir ${MAVEN_CACHE_HOST}"
MAVEN_CACHE_CONTAINER="/maven-cache"

PROJECT2BUILD=${DATASET_CONTAINER}/${PROJECT}


# clear old build from outside the container -- otherwise, when the container build fails,
# old jars might be copied over
mvn clean -f ${DATASET_HOST}/${PROJECT}/pom.xml

# run docker image from hub with java and mvn
# share & reuse maven cache for performance

#docker stop $DOCKER_CONTAINER
docker pull $DOCKER_IMAGE
#docker start $DOCKER_CONTAINER
docker run \
	-dit \
	--volume ${DATASET_HOST}:${DATASET_CONTAINER} \
	--volume ${MAVEN_CACHE_HOST}:${MAVEN_CACHE_CONTAINER} \
	--volume ${MAVEN_HOST}:${MAVEN_CONTAINER} \
	--workdir $PROJECT2BUILD \
	--user $(id -u):$(id -g) \
	--name $DOCKER_CONTAINER $DOCKER_IMAGE \

echo "building project"
docker exec -it $DOCKER_CONTAINER ${MAVEN_CONTAINER}/bin/mvn -Dmaven.repo.local=${MAVEN_CACHE_CONTAINER} -Drat.skip=true -Dmaven.test.skip=true -Dmaven.javadoc.skip=true -Dcyclonedx.skip=true clean package  | sed $'s,\x1b\\[[0-9;]*[a-zA-Z],,g' | tee ${TMP_LOG}


echo ""
if test -f "${DATASET_HOST}/${PROJECT}/target/${JAR_NAME}"; then
	echo "SUCCESS! - copying /target/${JAR_NAME}  into ${RESULT_FOLDER}"
	cp ${DATASET_HOST}/${PROJECT}/target/${JAR_NAME} ${RESULT_FOLDER}
	docker stop $DOCKER_CONTAINER
else 
	echo "FAILURE! - copying error logs into ${RESULT_ERROR_LOG}"
	cp ${TMP_LOG} ${RESULT_ERROR_LOG}
fi

docker stop $DOCKER_CONTAINER
docker rm $DOCKER_CONTAINER  # to avoid container with this name already in use

# for useability in batch scripts
echo ""
echo "================================================"
echo ""
