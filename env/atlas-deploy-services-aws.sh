#!/bin/bash

# Create an AWS cloudformation stack. Accepts following arguments
# $1 is name of cloudformation json file
# $2 is AWS stack name
# $3 is optional AWS stack parameters
# Waits for the stack create operation to complete either successfully or in error.
function create_aws_stack() {

    echo "Creating $2 in AWS region $ATLAS_AWS_REGION using cloudformation file $PATH_TO_CLOUDFORMAITON_FILES/$1"
    sleep 10   
    if [[ -z "$3" ]]; then
        CREATE_OUTPUT=`aws --region $ATLAS_AWS_REGION cloudformation create-stack --stack-name $2 --template-body file://$PATH_TO_CLOUDFORMAITON_FILES/$1`
    else
        CREATE_OUTPUT=`aws --region $ATLAS_AWS_REGION cloudformation create-stack --stack-name $2 --parameters $3 --template-body file://$PATH_TO_CLOUDFORMAITON_FILES/$1`
    fi

    if [[ "$CREATE_OUTPUT" =~ "StackId" ]]; then
        STACK_ID=`echo $CREATE_OUTPUT | tr '\n' ' ' | grep StackId | sed s/^.*StackId\"://  | tr '"' ' ' | tr -d [:space:]`
        echo "$2 creation in progress with stack-id $STACK_ID"
    else
    echo "Failed to create $2 stack"
    exit 1
    fi

    STACK_CREATED=0
    COUNTER=0
    while [ $STACK_CREATED -eq 0 ]
    do 
        sleep 30
        STACK_STATUS=`aws --region $ATLAS_AWS_REGION cloudformation describe-stacks --stack-name $2 --query "Stacks[0].StackStatus" | tr '"' ' ' | tr -d [:space:]`
        if [[ "$STACK_STATUS" == "CREATE_COMPLETE" ]]; then
            echo "$2 stack creation is complete"
            STACK_CREATED=1
        elif [[ "$STACK_STATUS" == "CREATE_IN_PROGRESS" ]]; then 
            echo "$2 stack creation is in progress. Waiting for $2 stack creation to complete"
        elif [[ "$STACK_STATUS" == "CREATE_FAILED" ]]; then
            echo "$2 stack creation failed. Please investigate. Script will exit"
            exit 1
        elif [[ "$STACK_STATUS" == "ROLLBACK_IN_PROGRESS" ]];  then
            echo "$2 stack creation failed and it is being rolled back. Please investigate. Script will exit once rollback is completed"
            exit 1
        elif [[ "$STACK_STATUS" == "ROLLBACK_COMPLETE" ]];  then
            echo "$2 stack creation failed and rollback is complete. Please investigate. Script will exit"
            exit 1
        else 
            echo "f$2 stack creation status $STACK_STATUS is not one of the well recognized. Still will wait and monitor"
        fi
        (( COUNTER++ ))
        if [[ $COUNTER -ge 20 && $STACK_CREATED -eq 0 ]]; then
            echo "Giving up after 5 minutes. $2 stack is still not created. Last reported status is $STACK_STATUS. Exiting"
            exit 1
        fi
    done
}

# Collect the output values from created stack. Accepts following arguments
# $1 is AWS stack name
# $2 is OutputKey name
function get_aws_stack_output() {
    OUTPUT_VALUE=`aws --region $ATLAS_AWS_REGION cloudformation describe-stacks --stack-name $1 --query "Stacks[0].Outputs[?OutputKey=='$2'].OutputValue" --output text`
    echo "$OUTPUT_VALUE"
}

function get_acm_certificate_arn() {
    for LB_CERTIFICATE_ARN in `aws acm list-certificates --output text |  awk '{print $2}'`; do 
        FOUND_TAG=`aws acm list-tags-for-certificate --certificate-arn $LB_CERTIFICATE_ARN --output text | grep $1`
        if [[ -n "$FOUND_TAG" ]]; then
            break
        fi
    done

    if [[ -n "$FOUND_TAG" ]]; then
        echo "Certificate ARN for $CERTIFICATE_NAME_TAG is: $LB_CERTIFICATE_ARN"
    else
        echo "Certificate for $CERTIFICATE_NAME_TAG not found in ACM ... Exiting"
        exit 1
    fi
}

# Deploy the atlas-nat-gateway so that all services in the private subnet
# have internet access to install docker and other packages

STACK_NAME="atlas-nat-gateway-$ATLAS_AWS_REGION"
create_aws_stack atlas-nat-gateway.json $STACK_NAME 

# Deploy internal application load balancer. The DNS name of the interanl ALB
# will be supplied into the docker images for all application services
STACK_NAME="atlas-internal-alb-$ATLAS_AWS_REGION"
CERTIFICATE_NAME_TAG="atlas-ilb"
get_acm_certificate_arn $CERTIFICATE_NAME_TAG
create_aws_stack atlas-internal-alb.json $STACK_NAME "ParameterKey=CertificateArn,ParameterValue=$LB_CERTIFICATE_ARN"
INTERNAL_ALB_DNS_NAME=`get_aws_stack_output $STACK_NAME InternalAlbDnsName`
echo "Internal ALB DNS Name is [$INTERNAL_ALB_DNS_NAME]"

# Deploy external application load balancer. 
STACK_NAME="atlas-external-alb-$ATLAS_AWS_REGION"
create_aws_stack atlas-external-alb.json $STACK_NAME
EXTERNAL_ALB_DNS_NAME=`get_aws_stack_output $STACK_NAME ExternalAlbDnsName`
echo "External ALB DNS Name is [$EXTERNAL_ALB_DNS_NAME]"

# Deploy the config service EC2 and add it to the internal ALB
STACK_NAME="atlas-config-service-$ATLAS_AWS_REGION-zone1"
create_aws_stack atlas-config-service-zone1.json $STACK_NAME "ParameterKey=KeyName,ParameterValue=$ATLAS_AWS_KEY_NAME ParameterKey=DockerUsername,ParameterValue=$ATLAS_DOCKER_USERNAME ParameterKey=ConfigGitUri,ParameterValue=$ATLAS_GIT_REPO ParameterKey=ConfigGitUsername,ParameterValue=$ATLAS_GIT_USER ParameterKey=ConfigGitPassword,ParameterValue=$ATLAS_GIT_PASSWORD"

# Wait 1 minutes to let the configuration service become available via the load balancer
echo "Waiting 1 minute .... for configuration service to become visible via load balancer"
sleep 60

# Deploy the discovery service EC2 and add it to the internal ALB
STACK_NAME="atlas-discovery-service-$ATLAS_AWS_REGION-zone1"
create_aws_stack atlas-discovery-service-zone1.json $STACK_NAME "ParameterKey=KeyName,ParameterValue=$ATLAS_AWS_KEY_NAME ParameterKey=DockerUsername,ParameterValue=$ATLAS_DOCKER_USERNAME"

echo "Waiting 1 minute .... for discovery service to become visible via load balancer"
sleep 60

# Deploy the API gateway EC2 and add it to the internal ALB
STACK_NAME="atlas-apigateway-service-$ATLAS_AWS_REGION-zone1"
CERTIFICATE_NAME_TAG="atlas-elb"
get_acm_certificate_arn $CERTIFICATE_NAME_TAG
create_aws_stack atlas-apigateway-service-zone1.json $STACK_NAME "ParameterKey=KeyName,ParameterValue=$ATLAS_AWS_KEY_NAME ParameterKey=DockerUsername,ParameterValue=$ATLAS_DOCKER_USERNAME ParameterKey=CertificateArn,ParameterValue=$LB_CERTIFICATE_ARN"

# Deploy all application services
STACK_NAME="atlas-application-services-$ATLAS_AWS_REGION-zone1"
create_aws_stack atlas-application-services-zone1.json $STACK_NAME "ParameterKey=KeyName,ParameterValue=$ATLAS_AWS_KEY_NAME ParameterKey=DockerUsername,ParameterValue=$ATLAS_DOCKER_USERNAME"

# Create DNS A-record tying http://atlas.mydomain.com to the external ALB so
# external users can invoke the atlas API in the public internet via http://atlas.mydomain.com
# domain. For example:  To place an order --> http://atlas.mydomain.com/atlas/api/order
# The required DNS parameters must be declared in the environment before running this script
# If these environment variables are not defined, DNS record will not be created but the user
# can still invoke the API from public internet using the public DNS name of the internet-facing external ALB
#
if [[ -z $ATLAS_R53_HOSTED_ZONE_ID || -z $ATLAS_URL ]]; then
    echo "Route 53 hosted zone id and the atlas URL are not defined as environment variables. Will not register a DNS record to the internet-facing load balancer"
else
    STACK_NAME="atlas-dns-$ATLAS_AWS_REGION"
    create_aws_stack atlas-dns.json $STACK_NAME "ParameterKey=DNSHostedZoneId,ParameterValue=$ATLAS_R53_HOSTED_ZONE_ID ParameterKey=AtlasURL,ParameterValue=$ATLAS_URL"
fi
