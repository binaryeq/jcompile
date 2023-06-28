#!/bin/sh

# Build a given mvn project using a Java compiler provided by a docker container. 
# Maven is provided by the host to ensure consistency, and the creation of further images. 
# The Maven cache (.m2) is also provided by the host for performance.
# @author jens dietrich

DOCKER_IMAGE=$1
DOCKER_CONTAINER=$2
PROJECT=$3
# needed for caching
JAR_NAME=$4
TAG=$5

echo "using docker image: ${DOCKER_IMAGE}"
echo "using docker container name: ${DOCKER_CONTAINER}"
echo "project name: ${PROJECT}"
echo "project jar to be generated: ${JAR_NAME}"
echo "project tag: ${TAG}"


RESULT_ROOT_FOLDER="jars"

RESULT_FOLDER=${RESULT_ROOT_FOLDER}/${DOCKER_CONTAINER}
RESULT_FILE=${RESULT_FOLDER}/${JAR_NAME}

if test -f "${RESULT_FILE}"; then
    echo "${RESULT_FILE} already exists, no compilation needed -- delete file to recompile" 
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

MAVEN_CACHE_HOST="$(pwd)/.m2"
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
	--name $DOCKER_CONTAINER $DOCKER_IMAGE \

echo "building project"
docker exec -it $DOCKER_CONTAINER ${MAVEN_CONTAINER}/bin/mvn -Dmaven.repo.local=${MAVEN_CACHE_CONTAINER} -Drat.skip=true -Dmaven.test.skip=true -Dmaven.javadoc.skip=true clean package

echo "copying result into ${RESULT_FOLDER}"
cp ${DATASET_HOST}/${PROJECT}/target/${JAR_NAME} ${RESULT_FOLDER}


docker stop $DOCKER_CONTAINER
docker rm $DOCKER_CONTAINER  # to avoid container with this name already in use