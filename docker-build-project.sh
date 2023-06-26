#!/bin/sh

# Note: does not work with ARM CPUs !
#DOCKER_IMAGE="svenruppert/maven-3.6.2-liberica:1.8.192"
#DOCKER_CONTAINER="svenruppert-openjdk-8.0.192"

#docker pull adoptopenjdk/openjdk8:jre8u372-b07-ubuntu

#DOCKER_IMAGE="adoptopenjdk/openjdk8:jre8u372-b07-ubuntu"  # does not contain javac

DOCKER_IMAGE=$1
DOCKER_CONTAINER=$2

PROJECT="commons-io"
# needed for caching
JAR_NAME="commons-io-2.10.0.jar"
TAG="rel/commons-io-2.10.0"
RESULT_ROOT_FOLDER="jars"


echo "using docker image: ${DOCKER_IMAGE}"
echo "using docker container name: ${DOCKER_CONTAINER}"

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
docker exec -it $DOCKER_CONTAINER ${MAVEN_CONTAINER}/bin/mvn -Dmaven.repo.local=${MAVEN_CACHE_CONTAINER} -Drat.skip=true -Dmaven.test.skip=true clean package

echo "copying result into ${RESULT_FOLDER}"
cp ${DATASET_HOST}/${PROJECT}/target/${JAR_NAME} ${RESULT_FOLDER}


docker stop $DOCKER_CONTAINER
docker rm $DOCKER_CONTAINER  # to avoid container with this name already in use