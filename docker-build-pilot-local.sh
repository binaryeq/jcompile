#!/bin/sh

# Note: does not work with ARM CPUs !
#DOCKER_IMAGE="svenruppert/maven-3.6.2-liberica:1.8.192"
#DOCKER_CONTAINER="svenruppert-openjdk-8.0.192"

#docker pull adoptopenjdk/openjdk8:jre8u372-b07-ubuntu

#DOCKER_IMAGE="adoptopenjdk/openjdk8:jre8u372-b07-ubuntu"  # does not contain javac
DOCKER_IMAGE="eclipse-temurin:8u372-b07-jdk"
DOCKER_CONTAINER="openjdk8-8.0.372"

DATASET_HOST="$(pwd)/dataset"
DATASET_CONTAINER="/dataset"

MAVEN_HOST="$(pwd)/apache-maven-3.9.2"
MAVEN_CONTAINER="/apache-maven"

echo "using data folder ${DATASET_HOST}"

MAVEN_CACHE_HOST="$(pwd)/.m2"
MAVEN_CACHE_CONTAINER="/maven-cache"

PROJECT2BUILD=$DATASET_CONTAINER/commons-io

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

docker stop $DOCKER_CONTAINER
docker rm $DOCKER_CONTAINER  # to avoid container with this name already in use