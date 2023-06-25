#!/bin/sh

# Note: does not work with ARM CPUs !
DOCKER_IMAGE="svenruppert/maven-3.6.2-liberica:1.8.192"
DOCKER_CONTAINER="svenruppert-openjdk-8.0.192"

#DOCKER_IMAGE="adoptopenjdk/maven-openjdk8:latest"
#DOCKER_CONTAINER="adopt-openjdk-8"

DATASET_HOST="$(dirname pwd)/jcompile-dataset"
DATASET_CONTAINER="/dataset"

echo "using data folder ${DATASET_HOST}"

MAVEN_CACHE_HOST="$(pwd)/.m2"
MAVEN_CACHE_CONTAINER="/maven-cache"

PROJECT2BUILD=$DATASET_CONTAINER/commons-io

# run docker image from hub with java and mvn
# share & reuse maven cache for performance

docker stop $DOCKER_CONTAINER
docker rm $DOCKER_CONTAINER
docker pull $DOCKER_IMAGE
docker run -it --volume ${DATASET_HOST}:${DATASET_CONTAINER} --volume ${MAVEN_CACHE_HOST}:${MAVEN_CACHE_CONTAINER} --workdir $PROJECT2BUILD --name $DOCKER_CONTAINER $DOCKER_IMAGE mvn -Dmaven.repo.local=${MAVEN_CACHE_CONTAINER} -Drat.skip=true -Dmaven.test.skip=true clean package

