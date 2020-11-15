#!/bin/bash

# Create an AWS cloudformation stack. Accepts following arguments
# $1 is name of cloudformation json file
# $2 is AWS stack name
# Waits for the stack create operation to complete either successfully or in error.
function create_aws_stack() {

    sleep 10
    echo "Creating $2 in AWS region $FINEX_AWS_REGION using cloudformation file $PATH_TO_CLOUDFORMAITON_FILES/$1"
    CREATE_OUTPUT=`aws --region $FINEX_AWS_REGION cloudformation create-stack  --stack-name $2 --template-body file://$PATH_TO_CLOUDFORMAITON_FILES/$1`
    
    if [[ "$CREATE_OUTPUT" =~ "StackId" ]]; then
        STACK_ID=`echo $CREATE_OUTPUT | tr '\n' ' ' | grep StackId | sed s/^.*StackId\"://  | tr '"' ' ' | tr -d [:space:]`
        echo "$2 creation in progress in region $3 with stack-id $STACK_ID"
    else
    echo "Failed to create $2 stack"
    exit 1
    fi

    STACK_CREATED=0
    COUNTER=0
    while [ $STACK_CREATED -eq 0 ]
    do 
        sleep 30
        STACK_STATUS=`aws --region $FINEX_AWS_REGION cloudformation describe-stacks --stack-name $2 --query "Stacks[0].StackStatus" | tr '"' ' ' | tr -d [:space:]`
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

# Create VPC in AWS region specified by the input argument number 2. The stack name will be suffixed
# with the same AWS region name specified in argument number 2
STACK_NAME="atlas-vpc-$FINEX_AWS_REGION"
create_aws_stack atlas-vpc.json $STACK_NAME

STACK_NAME="atlas-pvt-hosted-zone-$FINEX_AWS_REGION"
create_aws_stack atlas-private-hosted-zone.json $STACK_NAME
