#!/bin/bash

function showUsage() {
    echo "    " >&2
    echo "$(basename $0) -c target -p password -i -P -s -h" >&2
    echo "    " >&2
    echo "    -t Target service for wich to build TLS private key, certificate and keystore  " >&2
    echo "          internal-ca - Internal certificate authority " >&2
    echo "          ilb - Internal AWS Application Load Balancer " >&2
    echo "          elb - Internet-facing AWS Elastic Load Balancer " >&2
    echo "          product | participant | order | orderbook | trade - Atlas services " >&2
    echo "  "  >&2
    echo "    -p Password for private key and keystore for the specified target  " >&2
    echo "  "  >&2
    echo "    -i Internal CA alias used to sign the CSR of dedicated service  " >&2
    echo "          Required only for dedicated service and not for the internal-ca itself " >&2
    echo "          It is assumed that the private key and certificate of the internal CA have name prefix matching this value " >&2
    echo "          Example: If this argument is 'myca' then it is assumed that there is a myca-key.pem and myca.crt files in current directory " >&2
    echo "  "  >&2
    echo "    -P Password for internal CA private key. This is used for sigining the deciated service CSR with internal CA  " >&2
    echo "   " >&2
    echo "    -r AWS region. " >&2 
    echo "          Applicable only for ilb target which is for the internal load balancer deployed in AWS" >&2
    echo "          Specify the region on which the internal load balancer will use the cerfiticate" >&2
    echo "   " >&2
    echo "   -f public-url associated whose Route 53 DNS record maps to the internet-facing load balancer, Optional" >&2
    echo "          Example: atlas.mydomain.com  If present, the elb certificate will use this value for the DNS in Subject Alternative Naems " >&2
    echo "   " >&2
    echo "    -s Optional AWS S3 bucket URI (example: s3://atlas-security) where to place the keystore. (Optional) " >&2
    echo "          If not provided, the TLS artifacts are placed in current directory " >&2
    echo "          If provided, it is assumed that " >&2 
    echo "              the local machine has the AWS CLI installed and CLI credentials (access and secret key) are configured " >&2 
    echo "              the S3 bucket URI is already created with appropriate access restrictions " >&2 
    echo "   " >&2
    echo "    -h show usage  " >&2
    echo "   " >&2
}

# ---------------------------------------------------------------------------
# Create a self-signed certificate that will serve as the internal certificate 
# authority. This internal-ca certificate will sign the certificates of the
# individual services. The internal-ca certificate will be imported as trusted
# certificate into the Java SE cacerts so that services can communicate with 
# one another without having to import all individual certificates into Java
# cacerts file
# ---------------------------------------------------------------------------
function createInternalCA() {
    # Private key for internal certificate authority
    openssl req -newkey rsa:2048 -keyout $ATLAS_TLS_TARGET-key.pem -out $ATLAS_TLS_TARGET.csr -passout pass:$ATLAS_TLS_PASSWORD \
    -subj "/C=US/ST=Illinois/L=Chicago/O=Acme Inc/OU=Administration/CN=Acme Certificate Authority"

    # Extensions configuration file to add BasicConfiguration designating
    # the certificate as a CA certificate
    touch $ATLAS_TLS_TARGET-extensions.cnf
    echo "basicConstraints=critical,CA:TRUE" >> $ATLAS_TLS_TARGET-extensions.cnf

    # Create empty serial number file. This file will be used to create serial numbers
    # for signing certificate requets
    touch $ATLAS_TLS_TARGET-serial.ser 
    echo "10B9342AB325ED00" > $ATLAS_TLS_TARGET-serial.ser 

    # Self-sign the CA certificate
    openssl x509 -req -in $ATLAS_TLS_TARGET.csr -passin pass:$ATLAS_TLS_PASSWORD -days 365 -signkey $ATLAS_TLS_TARGET-key.pem \
    -CAserial $ATLAS_TLS_TARGET-serial.ser -out $ATLAS_TLS_TARGET.crt -extfile $ATLAS_TLS_TARGET-extensions.cnf

    echo "Completed TLS artificats for Internal CA."
    echo "The target name and password used for this Internal CA must be specified "
    echo "as '-i' and '-P' argument, respectively when creating service certificates"

}

# ---------------------------------------------------------------------------
# 1. Create a private key and CSR 
# 2. Sign the service CSR with internal-ca certificate
# 3. Import the service certificate into dedicated service PKCS12 keystore
# ---------------------------------------------------------------------------
function createService() {
    # Private key and CSR
    openssl req -newkey rsa:2048 -keyout atlas-$ATLAS_TLS_TARGET-key.pem -out atlas-$ATLAS_TLS_TARGET.csr -passout pass:$ATLAS_TLS_PASSWORD \
    -subj "/C=US/ST=Illinois/L=Chiago/O=NaperIlTech/OU=Atlas/CN=*.$ATLAS_TLS_TARGET.atlas.com"

    # Extensions configuration file to add DNS names for which 
    # this certificate is applicable
    touch atlas-$ATLAS_TLS_TARGET-extensions.cnf
    echo "subjectAltName=DNS:*.$ATLAS_TLS_TARGET.atlas.com" >> atlas-$ATLAS_TLS_TARGET-extensions.cnf

    # Sign the service certificate request (csr) using the Internal CA certificate
    openssl x509 -req -in atlas-$ATLAS_TLS_TARGET.csr -passin pass:$ATLAS_INTERNAL_CA_PASSWORD -days 365 -CA $ATLAS_INTERNAL_CA.crt -CAkey $ATLAS_INTERNAL_CA-key.pem \
    -CAserial $ATLAS_INTERNAL_CA-serial.ser -out atlas-$ATLAS_TLS_TARGET.crt -extfile atlas-$ATLAS_TLS_TARGET-extensions.cnf

    # PKCS12 keystore with service certificate chain that includes the Internal CA certificate.
    openssl pkcs12 -export -in atlas-$ATLAS_TLS_TARGET.crt -inkey atlas-$ATLAS_TLS_TARGET-key.pem -passin pass:$ATLAS_TLS_PASSWORD \
    -name $ATLAS_TLS_TARGET -CAfile $ATLAS_INTERNAL_CA.crt -caname $ATLAS_INTERNAL_CA -chain -out atlas-$ATLAS_TLS_TARGET.p12 -passout pass:$ATLAS_TLS_PASSWORD

    echo "$ATLAS_TLS_PASSWORD" > ./$ATLAS_TLS_TARGET.txt

    echo "Completed TLS artificats for $ATLAS_TLS_TARGET."
}

# ---------------------------------------------------------------------------
# 1. Create a private key and CSR 
# 2. Sign the load balancer CSR with internal-ca certificate
# 3. Create text files for the load balancer and internal-ca certificate PEM 
#    to use for importing into the AWS Certificate Manager
# ---------------------------------------------------------------------------
function createLoadBalancer() {

    echo "Will use LB DNS: $LB_DNS_NAME"

    # Private key and CSR
    openssl req -newkey rsa:2048 -keyout atlas-$ATLAS_TLS_TARGET-key.pem -out atlas-$ATLAS_TLS_TARGET.csr -passout pass:$ATLAS_TLS_PASSWORD \
    -subj "/C=US/ST=Illinois/L=Chiago/O=NaperIlTech/OU=Atlas/CN=$LB_DNS_NAME"

    # Extensions configuration file to add DNS names for which 
    # this certificate is applicable
    touch atlas-$ATLAS_TLS_TARGET-extensions.cnf
    echo "subjectAltName=DNS:$LB_DNS_NAME" >> atlas-$ATLAS_TLS_TARGET-extensions.cnf

    # Sign the service certificate request (csr) using the Internal CA certificate
    openssl x509 -req -in atlas-$ATLAS_TLS_TARGET.csr -passin pass:$ATLAS_INTERNAL_CA_PASSWORD -days 365 -CA $ATLAS_INTERNAL_CA.crt -CAkey $ATLAS_INTERNAL_CA-key.pem \
    -CAserial $ATLAS_INTERNAL_CA-serial.ser -out atlas-$ATLAS_TLS_TARGET.crt -extfile atlas-$ATLAS_TLS_TARGET-extensions.cnf

    # Create a PEM text file for the load balancer certificate
    openssl rsa -in atlas-$ATLAS_TLS_TARGET-key.pem -passin pass:$ATLAS_TLS_PASSWORD > atlas-$ATLAS_TLS_TARGET-acm-certificate-private-key.pem

    echo "Completed TLS artificats for $ATLAS_TLS_TARGET."
}

# ---------------------------------------------------------------------------
# Copy to S3 bucket
# ---------------------------------------------------------------------------
function copyToS3() {
    if [[ "$ATLAS_TLS_TARGET" == "internal-ca" ]]; then 
        if [[ -f "internal-ca.crt" ]]; then
            aws s3 cp ./$ATLAS_TLS_TARGET.crt $ATLAS_S3_BUCKET
            aws s3 cp ./$ATLAS_TLS_TARGET.crt $ATLAS_S3_BUCKET/atlas-certificate-chain.pem
            echo "Copied relevant artifacts to $ATLAS_S3_BUCKET on AWS account $AWS_ACCOUNT"
        fi
    else 
        if [[ -f "atlas-$ATLAS_TLS_TARGET.p12" ]]; then 
            aws s3 cp ./atlas-$ATLAS_TLS_TARGET.p12 $ATLAS_S3_BUCKET
            aws s3 cp ./$ATLAS_TLS_TARGET.txt $ATLAS_S3_BUCKET
            rm atlas-$ATLAS_TLS_TARGET*
            rm $ATLAS_TLS_TARGET.txt
            echo "Copied relevant artifacts to $ATLAS_S3_BUCKET on AWS account $AWS_ACCOUNT"
        fi
        if [[ -f "atlas-$ATLAS_TLS_TARGET-acm-certificate-private-key.pem" ]]; then 
            aws s3 cp ./atlas-$ATLAS_TLS_TARGET.crt $ATLAS_S3_BUCKET/atlas-$ATLAS_TLS_TARGET-acm-certificate-body.pem
            aws s3 cp atlas-$ATLAS_TLS_TARGET-acm-certificate-private-key.pem $ATLAS_S3_BUCKET 
            rm atlas-$ATLAS_TLS_TARGET*
            echo "Copied relevant artifacts to $ATLAS_S3_BUCKET on AWS account $AWS_ACCOUNT"
        fi
    fi
}

while getopts ":t:p:i:P:s:r:f:h" opt; do
  case ${opt} in
    t )
      ATLAS_TLS_TARGET=$OPTARG
      ;;
    p )
      ATLAS_TLS_PASSWORD=$OPTARG
      ;;
    s )
      ATLAS_S3_BUCKET=$OPTARG
      ;;
    i )
      ATLAS_INTERNAL_CA=$OPTARG
      ;;
    P )
      ATLAS_INTERNAL_CA_PASSWORD=$OPTARG
      ;;
    r )
      ATLAS_AWS_REGION=$OPTARG
      ;;
    f )
      ATLAS_URL=$OPTARG
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

VALID_ARGUMENTS="true"
if [[ -z "$ATLAS_TLS_TARGET" ]]; then
    VALID_ARGUMENTS="false"
    echo "Missing target for TLS certificate artifacts. Must be one of [internal-ca, ilb, elb, apigateway, discovery, config, product, participant, order, orderbook, trade]"
else 
    case "$ATLAS_TLS_TARGET" in
        ilb|elb|apigateway|discovery|config|product|participant|order|orderbook|trade)
            echo "Building TLS artificats for $ATLAS_TLS_TARGET ..."
            if [[ -z "$ATLAS_INTERNAL_CA"  || -z "$ATLAS_INTERNAL_CA_PASSWORD"  ]]; then 
                VALID_ARGUMENTS="false"
                echo "Must specify internal CA alias and password for service targert"
            fi
            ;;
        internal-ca)
            ;;
        *)
            VALID_ARGUMENTS="false"
            echo "$ATLAS_TLS_TARGET is an invalid target"
            ;;
    esac
fi

if [[ -z "$ATLAS_TLS_PASSWORD" ]]; then
    VALID_ARGUMENTS="false"
    echo "Missing password for TLS private key and keystore"
fi

if [[ -n "$ATLAS_S3_BUCKET" ]]; then
    AWS_CLI_CHECK=`aws sts get-caller-identity 2> /dev/null`
    if [[ "$AWS_CLI_CHECK" == *"Account"* ]]; then
        AWS_ACCOUNT=`aws sts get-caller-identity | grep Account | awk -F: '{print $2}' | tr '"' ' ' | tr ',', ' '`
    else
        VALID_ARGUMENTS="false"
        echo "AWS CLI is not installed or local machine is improperly credentialed"
    fi
fi

if [[ "$ATLAS_TLS_TARGET" == "ilb" ]]; then
    if [[ -z "$ATLAS_AWS_REGION" ]]; then
        echo "AWS region must be specified when creating certificate for AWS internal load balancer"
        VALID_ARGUMENTS="false"
    else
        LB_DNS_NAME="*.$ATLAS_AWS_REGION.elb.amazonaws.com"
    fi
fi

if [[ "$ATLAS_TLS_TARGET" == "elb" ]]; then
    if [[ -n "$ATLAS_URL" ]]; then
        LB_DNS_NAME=$ATLAS_URL
    else
        if [[ -z "$ATLAS_AWS_REGION" ]]; then
            echo "AWS region must be specified when public url is unsepcified and creating certificate for AWS internet-facing load balancer"
            VALID_ARGUMENTS="false"
        else
            LB_DNS_NAME="*.elb.$ATLAS_AWS_REGION.amazonaws.com"
        fi
    fi
fi

if [[ "$VALID_ARGUMENTS" == "false" ]]; then
    showUsage
    exit 1
fi

case "$ATLAS_TLS_TARGET" in
    internal-ca)
        createInternalCA
        ;;
    apigateway|discovery|config|product|participant|order|orderbook|trade)
        createService
        ;;
    ilb|elb)
        createLoadBalancer
        ;;
esac
 
if [[ -n "$ATLAS_S3_BUCKET" ]]; then
    copyToS3
fi
