#!/bin/bash

# Delete all but the atlas-application-services and atlas-internal-alb stacks
# These two stacks are dependencies for other stacks whereby output exported by
# these two stacks are cross-stack-referened. So we need to wait a while before 
# the "child" stacks are well underway for deletion. The instance id values
# exported by atlas-application-services is used by the atlas-internal-alb-listener
# and atlas-external-alb-listener for creating target groups. The ARN of ALB
# created by atlas-internal-alb and atlas-external-alb are used by their 
# respective listnner stacks.

for STACK in "atlas-dns-us-east-2" \
"atlas-application-services-us-east-2-zone1" \
"atlas-apigateway-service-us-east-2-zone1" \
"atlas-discovery-service-us-east-2-zone1" \
"atlas-config-service-us-east-2-zone1"; do
    aws cloudformation delete-stack --stack-name $STACK   
done

# Wait 30 seconds for the dependent stacks to be well underway in the deletion
# before proceeding to issue delete stack commands on their dependencies
sleep 600

echo "Deleting load balancer stacks"

for ESTACK in "atlas-nat-gateway-us-east-2" \
"atlas-external-alb-us-east-2" \
"atlas-internal-alb-us-east-2"; do
    aws cloudformation delete-stack --stack-name $ESTACK
done
