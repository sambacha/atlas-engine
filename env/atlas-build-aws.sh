#!/bin/bash

function showUsage() {
    echo "    " >&2
    echo "$(basename $0) -b build-target -r aws-region -c path-to-cloudformation-files [-s path-to-source-code]" >&2
    echo "               [-g app-configuration-git-repo-url] [-u git-repo-username] [-p git-repo-password] [-i docker-user]" >&2
    echo "               [-k aws-keypair-name] [-d aws-route53-hosted-zone-id] [-f public-url-for-dns-record] " >&2
    echo "    " >&2
    echo "               -b build-target  required argument   " >&2
    echo "                               deploy-vpc | build-services | deploy-services | deploy-dmz  " >&2
    echo "                               deploy-vpc - Deploy the vpc stack, subnets, internet gateway, security groups   " >&2
    echo "                               deploy-dmz - Deploy a single publicly available EC2 instance to act as a DMZ " >&2
    echo "                               build-services - Do maven build and docker build for all services   " >&2
    echo "                               deploy-services - only build & deploy stacks that includes atlas services  " >&2
    echo "                                          infrasturcture incuding load balancers & options DNS mapping. " >&2     
    echo "                                          Assumes the vpc stack is already built as services stack will " >&2 
    echo "                                          use exported references from the vpc stack" >&2 
    echo "  "  >&2
    echo "               -r aws-region Required for all deploy-* targets.   " >&2
    echo "                               aws region name such as us-east-1, us-east-2, etc  " >&2
    echo "  "  >&2
    echo "               -c path-to-cloudformation-files Required for all deploy-* targets.   " >&2
    echo "                               Directory containing the required cloudformation json files  " >&2
    echo "  "  >&2
    echo "               -s path-to-source-code Required only for build-services target.   " >&2
    echo "                               Base path to atlas services source code  " >&2
    echo "  "  >&2
    echo "               -t build base OS and JDK image? Optional and applies only for build-services target.   " >&2
    echo "                               Should the build-services build the base OS with JDK docker image?  " >&2
    echo "  "  >&2
    echo "               -g app-configuration-git-repo-url  Required only for build-services target. " >&2
    echo "                               git repository containing applicaiton services configurations  " >&2
    echo "  "  >&2
    echo "               -u git-repo-username  Required only for build-services target. " >&2
    echo "                               username for app configuraiton git repo  " >&2
    echo "  "  >&2
    echo "               -p git-repo-password  Required only for build-services target. " >&2
    echo "                               password for app configuraiton git repo  " >&2
    echo "  "  >&2
    echo "               -i docker-user  Required only for build-services target. " >&2
    echo "                               username for dockerhub  " >&2
    echo "  "  >&2
    echo "               -k aws-keypair-name  Required only for deploy-services or deploy-dmz target" >&2
    echo "  "  >&2
    echo "               -d aws-route53-hosted-zone-id  optional" >&2
    echo "                               If present, will create DNS record to atlas services external api  " >&2
    echo "  "  >&2
    echo "               -f public-url-for-dns-record  optional" >&2
    echo "                               Example: atlas.mydomain.com  If present, will map this url in the DNS record to atlas services external api  " >&2
    echo "   " >&2
}

VPC_BUILD="false"
while getopts ":g:u:p:r:d:f:c:k:b:s:ti:h" opt; do
  case ${opt} in
    g )
      ATLAS_GIT_REPO=$OPTARG
      ;;
    u )
      ATLAS_GIT_USER=$OPTARG
      ;;
    p )
      ATLAS_GIT_PASSWORD=$OPTARG
      ;;
    i )
      ATLAS_DOCKER_USERNAME=$OPTARG
      ;;
    d )
      ATLAS_R53_HOSTED_ZONE_ID=$OPTARG
      ;;
    r )
      ATLAS_AWS_REGION=$OPTARG
      ;;
    f )
      ATLAS_URL=$OPTARG
      ;;
    k )
      ATLAS_AWS_KEY_NAME=$OPTARG
      ;;
    b )
      ATLAS_BUILD_TARGET=$OPTARG
      ;;
    c )
      PATH_TO_CLOUDFORMAITON_FILES=$OPTARG
      ;;
    s )
      PATH_TO_SOURCE_CODE=$OPTARG
      ;;
    t )
      BUILD_OS_JDK_DOCKER_IMAGE="true"
      ;;
    h )
      showUsage
      exit 0
      ;;
    \? )
      echo "****** Invalid argument $opt"
      showUsage
      exit 1
      ;;
    : )
      echo "Invalid option: $OPTARG requires an argument" 1>&2
      ;;  
  esac
done
#shift "$(($OPTIND -1))"

VALID_ARGUMENTS="true"
if [[ -z "$ATLAS_BUILD_TARGET" ]]; then
    VALID_ARGUMENTS="false"
    echo "Missing build target. Must be one of [vpc, services, all]"
fi

if [[ "$VALID_ARGUMENTS" == "false" ]]; then
    showUsage
    exit 1
fi

if [[ "$ATLAS_BUILD_TARGET" == "deploy-vpc" ]]; then
    if [[ -z "$PATH_TO_CLOUDFORMAITON_FILES" ]]; then
      VALID_ARGUMENTS="false"
      echo "Missing path to the directory containing all AWS cloudformation files"
    fi
    if [ ! -f "$PATH_TO_CLOUDFORMAITON_FILES/atlas-vpc.json" ]; then
        echo "Cloudformation stack file not found in $PATH_TO_CLOUDFORMAITON_FILES/"
        VALID_ARGUMENTS="false"
    fi
    if [[ -z "$ATLAS_AWS_REGION" ]]; then
      VALID_ARGUMENTS="false"
      echo "Missing AWS regions"
    fi
fi

if [[ "$ATLAS_BUILD_TARGET" == "build-services" ]]; then
    if [[ -z "$PATH_TO_SOURCE_CODE" ]]; then
        VALID_ARGUMENTS="false"
        echo "Missing path to the directory containing atlas services source code"
    fi
    if [[ -z "$ATLAS_DOCKER_USERNAME" ]]; then
        VALID_ARGUMENTS="false"
        echo "Missing docker username"
    fi
    for SC_PATH in "config" "discovery" "apigateway" "products" "participants" "orders" "orderbooks" "trades"; do
      if [ ! -d "$PATH_TO_SOURCE_CODE/financial-exchange-$SC_PATH" ]; then
          echo "Missing directory $PATH_TO_SOURCE_CODE/financial-exchange-$SC_PATH/"
          VALID_ARGUMENTS="false"
      fi
    done
fi

if [[ "$ATLAS_BUILD_TARGET" == "deploy-services" ]]; then
    if [[ -z "$PATH_TO_CLOUDFORMAITON_FILES" ]]; then
      VALID_ARGUMENTS="false"
      echo "Missing path to the directory containing all AWS cloudformation files"
    fi
    if [[ -z "$ATLAS_GIT_REPO" ]]; then
        VALID_ARGUMENTS="false"
        echo "Missing URI to git repo for application services configuration files"
    fi
    if [[ -z "$ATLAS_GIT_USER" ]]; then
        VALID_ARGUMENTS="false"
        echo "Missing git username to git repo for application services configuration files"
    fi
    if [[ -z "$ATLAS_GIT_PASSWORD" ]]; then
        VALID_ARGUMENTS="false"
        echo "Missing git password to git repo for application services configuration files"
    fi
    if [[ -z "$ATLAS_AWS_REGION" ]]; then
      VALID_ARGUMENTS="false"
      echo "Missing AWS regions"
    fi
    if [[ -z "$ATLAS_AWS_KEY_NAME" ]]; then
        VALID_ARGUMENTS="false"
        echo "Missing AWS key name"
    fi
    for CF_FILE in "atlas-nat-gateway.json" "atlas-internal-alb.json" "atlas-external-alb.json" "atlas-config-service-zone1.json" "atlas-discovery-service-zone1.json" "atlas-apigateway-service-zone1.json" "atlas-application-services-zone1.json"; do
        if [ ! -f "$PATH_TO_CLOUDFORMAITON_FILES/atlas-vpc.json" ]; then
            echo "Cloudformation stack file $CF_FILE not found in $PATH_TO_CLOUDFORMAITON_FILES/"
            VALID_ARGUMENTS="false"
        fi
    done
    if [[ ! -z "$ATLAS_R53_HOSTED_ZONE_ID"  && ! -z "$ATLAS_URL"  ]]; then
        if [ ! -f "$PATH_TO_CLOUDFORMAITON_FILES/atlas-dns.json" ]; then
            echo "Cloudformation stack file not found in $PATH_TO_CLOUDFORMAITON_FILES"
            VALID_ARGUMENTS="false"
        fi
    fi
fi

if [[ "$ATLAS_BUILD_TARGET" == "deploy-dmz" ]]; then
    if [[ -z "$PATH_TO_CLOUDFORMAITON_FILES" ]]; then
      VALID_ARGUMENTS="false"
      echo "Missing path to the directory containing all AWS cloudformation files"
    fi
    if [[ -z "$ATLAS_AWS_REGION" ]]; then
      VALID_ARGUMENTS="false"
      echo "Missing AWS regions"
    fi
    if [[ -z "$ATLAS_AWS_KEY_NAME" ]]; then
        VALID_ARGUMENTS="false"
        echo "Missing AWS key name"
    fi
fi

if [[ "$VALID_ARGUMENTS" == "false" ]]; then
    showUsage
    exit 1
fi


if [[ "$ATLAS_BUILD_TARGET" == "deploy-vpc" ]]; then
    echo "Doing VPC deployment on region $ATLAS_AWS_REGION using cloudformation file $PATH_TO_CLOUDFORMAITON_FILES/atlas-vpc.json"
    export PATH_TO_CLOUDFORMAITON_FILES
    export ATLAS_AWS_REGION
    . ./atlas-deploy-vpc.sh
fi

if [[ "$ATLAS_BUILD_TARGET" == "deploy-dmz" ]]; then
    export PATH_TO_CLOUDFORMAITON_FILES
    export ATLAS_AWS_REGION
    export ATLAS_AWS_KEY_NAME
    echo "Doing DMZ EC2 instance deployment on region $ATLAS_AWS_REGION using cloudformation file $PATH_TO_CLOUDFORMAITON_FILES/atlas-vpc.json"
    . ./atlas-deploy-dmz.sh
fi

if [[ "$ATLAS_BUILD_TARGET" == "build-services" ]]; then
    export ATLAS_DOCKER_USERNAME
    export PATH_TO_SOURCE_CODE
    export BUILD_OS_JDK_DOCKER_IMAGE

    echo "Doing maven and docker build of all services with source code in $PATH_TO_SOURCE_CODE"
    . ./atlas-build-services.sh
fi

if [[ "$ATLAS_BUILD_TARGET" == "deploy-services" ]]; then
    export PATH_TO_CLOUDFORMAITON_FILES
    export ATLAS_AWS_REGION
    export ATLAS_AWS_KEY_NAME
    export ATLAS_GIT_REPO
    export ATLAS_GIT_USER
    export ATLAS_GIT_PASSWORD
    export ATLAS_DOCKER_USERNAME
    
    echo "Doing services deployment on region $ATLAS_AWS_REGION using cloudformation files in $PATH_TO_CLOUDFORMAITON_FILES/"
    if [[ ! -z "$ATLAS_R53_HOSTED_ZONE_ID"  && ! -z "$ATLAS_URL"  ]]; then
        export ATLAS_R53_HOSTED_ZONE_ID
        export ATLAS_URL
        echo "Will use add DNS record for $ATLAS_URL on route 53 hosted zone id $ATLAS_R53_HOSTED_ZONE_ID"
    fi

    . ./atlas-deploy-services-aws.sh
fi



