#!/bin/bash

# Doing maven builds on all services
function maven_build_services() {
    FAILED_MAVEN_BUILD_COUNT=0
    for SVC in "config" "discovery" "apigateway" "products" "participants" "orders" "orderbooks" "trades"; do
        POM_PATH=$PATH_TO_SOURCE_CODE/financial-exchange-$SVC/pom.xml
        echo "Building $SVC service ..."
        MAVEN_BUILD_STATUS=`mvn -f $POM_PATH clean package | grep BUILD | awk '{print $3}'`
        if [[ $MAVEN_BUILD_STATUS == "SUCCESS" ]]; then
            echo "$SVC service build successful"
        else 
            echo "$SVC service build failed"
            (( FAILED_MAVEN_BUILD_COUNT++ ))
        fi  
    done
    if [[ $FAILED_MAVEN_BUILD_COUNT -eq 0 ]]; then
        echo "All services built successfully. Will proceed with docker builds"
    else 
        echo "Not all services built successfully. Will not proceed with docker builds"
        exit 1
    fi
}

# Build docker image for all Application Services and push to docker hub
function docker_build_base_image() {
    DOCKER_BUILD_STATUS_OUTPUT=`docker build \
    -f $PATH_TO_SOURCE_CODE/financial-exchange-env/dockerfile.oswithjdk \
    -t ${FINEX_DOCKER_USERNAME}/atlas-base-osjdk:latest \
    $PATH_TO_SOURCE_CODE/financial-exchange-env | grep "Successfully tagged"`

    DOCKER_BUILD_STATUS=`echo $DOCKER_BUILD_STATUS_OUTPUT  | awk '{print $1, $2}'`
    if [[ $DOCKER_BUILD_STATUS == "Successfully tagged" ]]; then
        echo "Base image with OS and JDK docker build successful"
        echo "Pushing base image docker image to docker hub"
        DOCKER_TAG=`echo $DOCKER_BUILD_STATUS_OUTPUT  | awk '{print $3}'`
        DOCKER_PUSH_OUTPUT=`docker push $DOCKER_TAG | grep "latest"`
        if [[ "$DOCKER_PUSH_OUTPUT" =~ "latest" ]]; then
            echo "Base docker image pushed to dockerhub successfully"
        else 
            echo "Something is not right. Base docker image push may have failed"
            exit 2
        fi
    else 
        echo "Base docker image build failed"
    fi
}

# Build docker image for all Application Services and push to docker hub
function docker_build_services() {
    for SVC in "config" "apigateway" "discovery" "products" "participants" "orders" "orderbooks" "trades"; do
        DOCKER_BUILD_STATUS_OUTPUT=`docker build \
        -f $PATH_TO_SOURCE_CODE/financial-exchange-$SVC/dockerfile.$SVC \
        -t ${FINEX_DOCKER_USERNAME}/atlas-$SVC-aws:latest \
        $PATH_TO_SOURCE_CODE/financial-exchange-$SVC | grep "Successfully tagged"`

        DOCKER_BUILD_STATUS=`echo $DOCKER_BUILD_STATUS_OUTPUT  | awk '{print $1, $2}'`
        if [[ $DOCKER_BUILD_STATUS == "Successfully tagged" ]]; then
            echo "$SVC service docker build successful"
            echo "Pushing $SVC service docker image to docker hub"
            DOCKER_TAG=`echo $DOCKER_BUILD_STATUS_OUTPUT  | awk '{print $3}'`
            DOCKER_PUSH_OUTPUT=`docker push $DOCKER_TAG | grep "latest"`
            if [[ "$DOCKER_PUSH_OUTPUT" =~ "latest" ]]; then
                echo "$SVC service docker image pushed to dockerhub successfully"
            else 
                echo "Something is not right. $SVC service docker image push may have failed"
                exit 2
            fi
        else 
            echo "$SVC service docker build failed"
        fi
    done
}

# Maven build all services
maven_build_services 

if [[ -n "$BUILD_OS_JDK_DOCKER_IMAGE" ]]; then
    docker_build_base_image
fi

# docker build for application services
docker_build_services

