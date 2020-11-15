# AtlasCX: A Financial Exchange Engine  

> Further Improvements are required, and no audit has been done yet

- [AtlasCX: A Financial Exchange Engine](#atlascx--a-financial-exchange-engine)
- [Service Level Architecture View](#service-level-architecture-view)
  * [Infrastructure Services](#infrastructure-services)
    + [Configuration Service](#configuration-service)
    + [Discovery Service](#discovery-service)
    + [API Gateway Service](#api-gateway-service)
  * [Application Services](#application-services)
    + [Product Service](#product-service)
    + [Participant Service](#participant-service)
    + [Order Service](#order-service)
    + [Orderbook Service](#orderbook-service)
    + [Trade Service](#trade-service)
  * [PostgreSQL Database](#postgresql-database)
  * [Load Balancers](#load-balancers)
- [Deployment](#deployment)
  * [Localhost Deployment](#localhost-deployment)
  * [AWS Deployment](#aws-deployment)
    + [Using the Build Scripts to Deploy Fresh Installation to AWS](#using-the-build-scripts-to-deploy-fresh-installation-to-aws)
      - [Prepare Working Space](#prepare-working-space)
      - [Automated VPC Build](#automated-vpc-build)
      - [Manual PostgreSQL Deployment](#manual-postgresql-deployment)
      - [Deploy DMZ EC2 instance](#deploy-dmz-ec2-instance)
      - [DDL for `atlas` Schema](#ddl-for--atlas--schema)
      - [Create TLS Artifacts](#create-tls-artifacts)
      - [Maven Build and Docker Build Services](#maven-build-and-docker-build-services)
      - [Deploy Services](#deploy-services)
- [Application Security](#application-security)
  * [Data Encryption](#data-encryption)
- [Future Directions](#future-directions)


*  [Atlas](https://github.com/sambacha/atlas-engine)
   *  Synchronous RESTful web services for internal and external API
   *  Service mesh implementation with Spring Cloud
      *  Application services communicate via API gateway service
      *  Spring Zuul for API gateway
      *  Spring Eureka for service discovery
      *  Spring Configuration Server for service configuration distribution
      * AWS internal load balancer configured with path-based routes to the internal APIs offered by
services
   *  AWS external Elastic Load Balancer load balancer associated to DNS record of public domain
name
      *  Routes the system external API to API gateway service
   *  PostgreSQL for persistence
   *  AWS cloudformation to deploy entire infrastructure

The stock exchange application itself provides APIs to place orders which are then matched using an
orderbook implementation using simple price-time order matching. Additionally it has APIs to
retrieve products available for trade, to view matched trades, and to retrieve orders. 

The distributed services of the system are individual Spring Boot applications. Together the
services form an API-based system with API in the form of web services. The API is categorized into
internal and external API. The internal API follow the path convention is
`/atlas/internal/api/domain`, and external API follows the path convention `/atlas/api/domain`. The
objective of the internal vs external API is to make external API potentially available on the
public Internet whereas the internal API is available only within the organization. The external API
consists of capabilities such as placing and order whereas internal API consists of capabilities
such as product maintenance.

The services in the system use PostgreSQL for persistence. While an ideal microservices architecture
calls for each service having its own decoupled persistence storage, the simplicity of the data
model led to use of single PostgreSQL schema used by all services. The business data model itself
provides significant isolation, and the service implementations themselves only query and update
only those database tables that are directly applicable to the service. So while there is no forced
isolation by means of decoupled schemas or even decoupled database instances, the service
implementations practice the decoupled storage use.

In addition to the application services, the system has Spring Cloud Configuration
(https://spring.io/projects/spring-cloud-config) for Configuration Management and Spring Cloud
Netflix (https://cloud.spring.io/spring-cloud-netflix/reference/html) for API routing (Spring Zuul)
and Service Discovery (Spring Eureka). 

The application services retrieve their application parameters from the configuration service. The
application parameters may be different, for example, depending on the type of deployment such as
local or AWS or any other mode of operation. The Spring Cloud configuration service, in turn,
fetches the configurations from designated git repository on behalf of the requesting Application
Service. The git repository should contain `.yml` files for each application and its mode of
operation. The mode of operation is determined by the spring profile setting in each application.

The application services communicate with each other via the API gateway service. The application
services and API gateway service register with the discovery service to allow API gateway to
discover the available application services for API routing and load balancing.

All service types are dockerized. Each docker image can be run in a docker container where the
docker containers can be launched on a single bare-metal hardware, single VM, or multiple VMs. The
objective of this implementation is to deploy each service in a dedicated AWS t3.micro EC2 instance.
The `/atlas-env` contains the necessary AWS Cloudformation configuration files and bash
scripts that automate the maven and docker builds of the services as well as deployment of necessary
infrastructure in AWS.

All service-to-service and service-to-load-balancers communication is secured via TLS. Refer to the
[Application Security](#application-security) for implementation details.  

The diagram below gives a complete picture of all that is involved in this repo.

![Complete Picture](atlas-docs/CompletePicture.png)  

Now that the system is described at a high-level, please refer to the subsequent sections for
details information.

# Service Level Architecture View

The financial exchange employs a microservices architecture with independent services interacting
with one another to provide a full order management system with a price-time priority orderbook. The
figure below shows a high level view of the service level architecture. There are two types of
services.

1. Application Services
2. Infrastructure Services

The application services implement the business logic of the financial exchange while the
infrastructure services support the distributed environment under which the application services run
and collaborate with one another.

![Services](atlas-docs/AllServices.png) 

## Infrastructure Services

Refer to the Atlas Complete Picture diagram above for reference. The infrastructure services are 

1.  Configuration Service
2.  Discovery Service
3.  API Gateway Service

### Configuration Service

[Configuration Service](atlas-config/) is a Java Spring Boot application that is also a
Spring Cloud Config Server. It contains no dedicated application code. Instead it relies entirely on
the the out-of-the-box Spring Cloud Config Server implementation. It uses a git repository as the
source of configuration files to serve to the application services. The git repository information
such as git URI and git credentials are supplied to the docker build process for the configuration
service. As such the git configuration repository details are pre-packaged into the docker image and
ready to run when the image is run in a docker container. The excerpt from the [Configuration
Service Dockerfile](atlas-config/dockerfile.configserver) show how the git details are
passed into the docker build which are then exported as environment variable in the docker image.

```Dockerfile
ARG GIT_URI_ARG
ARG GIT_USER_ARG
ARG GIT_PASSWORD_ARG

ENV GIT_URI $GIT_URI_ARG
ENV GIT_USER $GIT_USER_ARG
ENV GIT_PASSWORD $GIT_PASSWORD_ARG
```

Then the docker build command is issued as follows to "bake" the git repo details into the docker
image.

```bash
# Supply appropriate --build-arg values for the 
# GIT_URI_ARG, GIT_USER_ARG, and GIT_PASSWORD_ARG

docker build -f dockerfile.configserver \
--build-arg GIT_URI_ARG=https://github.com/githubUser/configuraiton.git \
--build-arg GIT_USER_ARG=githubUser \
--build-arg GIT_PASSWORD=password \
-t dockerUser/atlas-config-server .
```

Thea Spring Boot application.yml references the environment variables for git repository details.  

```yaml
spring:
  application:
    name: atlas-config
  cloud:
    config:
      server:
        git:
          uri: ${git_uri}
          force-pull: true
          username: ${git_user}
          password: ${git_password}
server:
  port: 8080
```
 
One or more configuration services can be deployed behind an internal load balancer for
fault-tolerance and high-availability.

### Discovery Service

[Discovery Service](atlas-discovery/) is a Java Spring Boot application that is also a
Spring Netflix Eureka server. It contains no dedicated application code. Instead it relies entirely
on the the out-of-the-box Spring Eureka Server implementation. 

* pring Boot and Eureka Server application
* Retrieves the deployment-specific configuration properties from the Configuration Service
* Allow all other services (except the Configuration Service) to register and discover peer services
* Allow discovery of registered services

### API Gateway Service

[API Gateway Service](atlas-apigateway/) is a Java Spring Boot application that is also
a Spring Zuul application. The API Gateway Service uses the routing and filtering features of Spring
Zuul. As such it serves as the API gateway for the application services as well as external API
invocations. The current implementation only uses the routing features of Zuul, but does not take
advantage of this filtering capabilities. Stories are in the backlog for near future to use filters
to incorporate security and distributed tracing features to financial exchange.

* Spring Boot and Zuul application
* Retrieves the deployment-specific configuration properties from the Configuration Service
* Registers with the Discovery Service to allow Application Services to discover the API Gateway
Service  as the API Gateway Service
* Routes API invocations amongst the Application Services

## Application Services

The Application Services implement the business logic of the financial exchange system.

![Services](atlas-docs/ApplicationServices.png) 

Each service is:

* Spring Boot application
* Retrieves the deployment-specific configuration properties from the Configuration Service
* Registers with the Discovery Service to allow discovery by API Gateway Service
* Communicates with other Application Services via the API Gateway Service

The table below identifies the location of the source code for each service within this repository.

| Service               | Repository Location                                                 |
| --------------------- | ------------------------------------------------------------------- |
| Product Service       | [atlas-product](atlas-product)            |
| Participant Service   | [atlas-participant](atlas-participant)    |
| Order Service         | [atlas-order](atlas-order)                |
| Orderbook Service     | [atlas-orderbook](atlas-orderbook)        |
| Trade Service         | [atlas-trade](atlas-trade)                |

 Application Services depend on persistence of multiple domain object types.

* Equity (i.e. Product)
* Broker (i.e. Participant)
* Orders
* Trade

Market participants (i.e. Brokers) place a buy and sell Orders on a variety of products (i.e.
Equity). The orders are matched by in an orderbook which implements price-time-priority algorithm.
Matched orders are represented in trades.

All capabilities are supported via API offered by all services. The Application Services provide an
internal API an an external API. The internal API follows the path pattern `/atlas/internal/`, and
it is intended for use by inter-service communication and for internal users to manage activities
such as product and participant create, delete and updates. The external API follows the path
pattern `/atlas/external/`, and it is intended to be available to external users.

The Application Services are self-explanatory from the name itself, but the list below gives a brief
overview of each service.

### Product Service

* Provides operations to manage financial instrument products data. Currently only supported product
is Equity.
* Create, update and delete operations are available via the internal API.
* Retrieve operation is available via internal and external API.

### Participant Service

* Provides operations to manage market participants data. Currently only supported participant is
Broker.
* Create, update and delete operations are available via the internal API.
* Retrieve operation is available via internal and external API.

### Order Service

* Provides order management operations.
* Add new order, update or cancel existing orders, or get order status via external API.
* Orderbook Service updates the order state (i.e. booked, filled) via internal API 

### Orderbook Service

* Price-time-priority orderbook for each product
* Order Service uses internal API to apply new and updated orders to the orderbook via internal API
* Does not offer external API

### Trade Service

* Provides operations to manage trades in the database.
* Orderbook Services adds new trades via internal API
* External users can query trade details via external API

Each service employs nearly identical structural architectural pattern with differences being in the
business logic or business domain objects.

![Services](atlas-docs/ApplicationServiceStructure.png) 

Each Application Service has a `bootstrap.yml` that configures it with the Spring profile and the
URI for the Configuration Service. Both values are taken from environment variables passed into the
docker container of the Application Service during deployment. The active profile and Configuration
Service URI are used during application startup to retrieve the full configuration for the
application. Below is an example of a `bootstrap.yml` file in Order Service.

```yaml
spring:
  application:
    name: fienx-order
  profiles:
    active: ${spring_profiles}
  cloud:
    config:
      uri: ${config_server_uri}
      label: master
```

## PostgreSQL Database

There is a single database instance and single database schema for all services. Ideal microservices
architecture should employ a dedicated database schema, but the simplicity of the data model and its
inherent clean boundaries makes having separate schemas unwarranted. That said, it is just as easy
to create separate schema without much departure from the overall architecture of the system. The
[atlasschema.sql](atlas-env/atlasschema.sql) contains the DDL for the data model shown
below.

![Services](atlas-docs/DataModel.png) 

## Load Balancers

The architecture employs use of two AWS Application Load Balancers. 

* An internal Application Load Balancer to route HTTP invocation to Infrastructure Services.
* An internet-facing Elastic Load Balancer to route API invocations from the external participants
from the public Internet to the API Gateway Services

Although the internal and internet-facing load balancers described here are provisioned and tested
on AWS platform, the same could be implemented on any cloud or on-prem configuration.

# Deployment

The system deployment has been tested on two deployment configurations.

* localhost
* AWS

## Localhost Deployment

The primary objective of the localhost deployment is for rapid unit test of the business logic of
the system. It involves running all Application Services, Configuration Service, and the PostgreSQL
database on a single machine. Every service and the PostgreSQL database was run as a docker
container. Since all services are running on the same machine, each service has to expose its API on
different TCP port. Obviously there are no load balancers deployed in the localhost configuration.

Note that there is no need for code change in any of the services. Simply deploy the applications
with `localhost` profile as the active Spring profile and make corresponding Spring configuration
files available in the git configuration repository. For example by having the spring profile set to
`localhost` on `atlas-product` service, the `atlas-product` service will request its properties file
titled `atlas-product-localhost.yml` from the Configuration Service. The
`atlas-product-localhost.yml` should have the appropriate server listen port as well as database
URI. 

## AWS Deployment

Similar to the `localhost` configuration the application properties for each service is configured
with the combination of spring profile named `aws` and the matching `atlas-xxxxx-aws.yml` in the
configuration git repository where `xxxxx` is the service name.

The AWS deployment architecture is shown in the figure below. Each Application Service and
Configuration Service run in a dedicated t2.micro EC2 instance. 

![Services](atlas-docs/AWSDeployment.png) 

The [atlas-env](atlas-env) directory contains several AWS Cloudformation
stack JSON configuration files and shell scripts that stand up the entire deployment from setting up
VPC with all the requisite subnets and security groups as well as deploying all services and the
load balancers. The figure below shows the relationship between the shell scripts, Cloudformation
files, and the maven/docker builds. Note that to minimize cost and to attempt to run in the AWS free
tier as much as possible, the EC2 instances are launched in only one of the two availability zone.
Hence the Cloudformation files are shown only for zone1. It's just as easy to replicate the zone2
cloud formation from the zone1 Cloudformation files.

![Build and Deployment](atlas-docs/BuildAndDeployment.png) 

The build & deployment process is mostly automated except for the manual creation of PostgreSQL RDS
in AWS. There are two possible scenarios to use the build & deployment scripts and Cloudformation.

* Fresh Installation 
* Rebuild & Deploy Services

Either scenario is triggered from the main entry point using the shell script
[atlas-build-aws.sh](atlas-env/atlas-build-aws.sh). The Rebuild & Deploy Services
scenario is just a subset of the Fresh Installation Scenario.

The section below describes the procedure to do the full fresh installation as if this architecture
had never been deployed in AWS. The fresh installation scenario will appear laborious and tedious
due to the one-time procedures for initial workspace preparation and PostgreSQL deployment in Amazon
RDS, but the routine updates after the fresh install only requires the rebuild & deploy of services
which are fully automated with single command to run the `atlas-build-aws.sh`. Deploy of this new
feature requires to rebuild the java applications, creating docker images and deploying the new
images in AWS. All that could be performed with the Rebuild & Deploy Services scenario. Another
useful aspect of the separation of the Rebuild & Deploy Services is the cost in AWS. Most everyone
who will use this system will do so for experimental purposes. As such there is not an expectation
to leave all EC2 instances and the load balancers running. Instead one will deploy the services and
the load balancer for the duration of couple of hours. In that case the Cloudformation stacks for
the services and the load balancers can be deleted and resurrected with the Rebuild & Deploy Service
scenario when needed again while leaving the VPC stack running. The VPC stack as implemented here
incurs minimal, if any, cost in AWS free tier.

### Using the Build Scripts to Deploy Fresh Installation to AWS

There are three steps necessary for full fresh installation. Two of the three steps (which are the
most tedious and error-prone) are fully automated with shell script and AWS Cloudformation.

1. Prepare the working space
2. Automated build of the VPC 
3. Manual build of PostgreSQL RDS and creation of `atlas` database schema
4. Create database tables in `atlas` schema
5. Automated build of services. This includes
   1. Maven build of the java applications
   2. Docker build and push to Docker Hub
   3. AWS stacks for NAT Gateway, EC2, Load Balancers and DNS

Each of the two automated builds are invoked using the `atlas-build-aws.sh`. This main script has
self-explanatory usage documentation that one can invoke with a `-h` option or simply execute the
script without any options. The information in the usage/help document is adequate so it is not
repeated here.

#### Prepare Working Space

In order to execute the automated build steps, the following prerequisite conditions must be
available.

1.  Install either OpenJDK or Oracle JDK version 8 or later.
2.  Install git, if not already installed. Refer to
https://git-scm.com/book/en/v2/Getting-Started-Installing-Git 
3.  Install docker, if not already installed. Refer to https://docs.docker.com/get-docker
4.  Install maven, if not already installed. Refer to https://maven.apache.org/install.html
5.  Confirm that git, docker, and maven are in the path variables in your environment.
6.  Clone this git repository so that all source code and build scripts are available locally. Note
that in the future this step could also be automated in the build script, but for now the git clone
of this repository is to be manually performed.
7.  Create a Docker Hub account, if you don't already have one. The docker images for the
Application Services and Configuration Service will be pushed to this repository.
8.  Create a git repository for the Application Services configuration files. Use
https://github.com/sambacha/atlas-engine-config as reference. If you create this repository in
GitHub, you may want to make it a private repository as it will contain details specific to your own
deployment configuration. The Configuration Service docker build will require the details of this
git repository as described above in the Configuration Service section.
9.  Create an AWS account, if you don't already have one.
10. Create an AWS key-pair in the AWS region where you wish to deploy atlas.

#### Automated VPC Build

1. Change to directory `atlas-env` in your local git repository where you cloned
https://github.com/sambacha/atlas-engine.
2.  Run the `atlas-build-aws.sh` as shown below to build out the VPC in your desired AWS region. 

```console
atlas-build-aws.sh -b deploy-vpc -r <aws region name> -c <absolute path to atlas-env 
in your local git clone of https://github.com/sambacha/atlas-engine>
```
3. The shell script will take few minutes to build the VPC stack in AWS. You can monitor the output
from the `atlas-build-aws.sh` to track the progress. The progress can also be tracked in the AWS
Cloudformation console.

Once the `atlas-build-aws.sh` completes successfully the following AWS elements will have been
created.

* VPC named atlas-vpc
* Internet gateway
* Private subnets in two AZs (one per AZ) for Infrastructure Services and Application Services
* Private subnets in two AZs (one per AZ) for RDS
* Public subnets in two AZs (one per AZ) to host an EC2 accessible via public Internet for
administrative purpose
* NAT Gateway to allow Internet access to private subnets
* Three security groups. One for the services, one for database and one for access over the Internet
* Relevant route tables and network ACLs   

#### Manual PostgreSQL Deployment

Create a new PostgreSQL instance in AWS RDS console. Use the following values. Use default values
for parameters not listed below. This procedure needs to be executed only if the database is not
already created.

1. Navigate to the RDS on AWS console. 
2. Proceed to create a new database instance with parameters shown below. The parameters below are
just a suggestion to keep the RDS usage within the AWS free tier. The names of VPC and Security
Group are those that were created in the Automated VPC Build step.

| Parameter                             | Value                                               |
| ------------------------------------- | --------------------------------------------------- |
| Create Database                       | Standard Create                                     |
| Engine Option                         | Choose latest stable release of PostgreSQL          |
| Templates                             | Free Tier                                           |
| DB Instance Identifier                | atlas-database                                      |
| Credentials                           | Choose appropriate username and password            |
| DB Instance Size                      | db.t2.micro                                         |
| Storage Type                          | General Purpose SSD                                 |
| Allocated Storage                     | 20 GiB                                              |
| Enable storage autoscaling            | Disabled                                            |
| Connectivity                          | AtlasVpc                                            |
| Publicly accessible                   | No                                                  |
| VPC Security Groups                   | Remove the Default security. Add AtlasDbSg, AtlasAppSg,
AtlasPubSg          |
| Availability Zone                     | Choose zone A                                       |
| Initial database name                 | atlas                                               |
| Automatic backup                      | Disable                                             |
| Performance insights                  | Disable                                             |
| Enhanced Monitoring                   | Disable                                             |
| Log Exports                           | Disable                                             |
| Auto minor versiong upgrade           | Disable                                             |
| Maintenance window                    | Select date and time appropriate for you            |
| Delete protection                     | Disable                                             |

3. Once the database is available, create the database tables to support the atlas data model as
shown in the next section
4. Record the database address which will have the form
`atlas-database.xxxxxxxxx.us-east-2.rds.amazonaws.com/atlas`. This will need to be added to the
database configuration parameter in the application yaml files in the configuration repository.
Example shown below

```yaml
spring: 
  application:
    name: atlas-order
  datasource:
    url: jdbc:postgresql://atlas-database.xxxxxxxxx.us-east-2.rds.amazonaws.com/atlas
    username: myuser
    password: mypassword
```
#### Deploy DMZ EC2 instance

With the database and services running in private subnet, we need means to perform some manual
installation & troubleshoot from the outside. Therefore an EC2 with public IP & DNS is necessary.
This EC2 is referred to as a DMZ instance.

1. Change to directory `atlas-env` in your local git repository where you cloned
https://github.com/sambacha/atlas-engine.
2.  Run the `atlas-build-aws.sh` as shown below to build out the VPC in your desired AWS region. 

```console
atlas-build-aws.sh -b deploy-dmz -r <aws region name> -c <absolute path to atlas-env in
your local git clone of https://github.com/sambacha/atlas-engine> -k <AWS key-pair name> 
```
3. Look for an EC2 instance named `atlas-dmz-zone1` with public IP and DNS. This instance has python
and postgres CLI (pgcli) packages installed. We will run postgres DDL to create the tables in the
database.

#### DDL for `atlas` Schema 

1. SSH into the `atlas-dmz-zone1` EC2 instance
2. SFTP the [atlasschema.sql](atlas-env/atlasschema.sql) to `atlas-dmz-zone1`
3. Connect to the atlas schema in the PostgreSQL database created in the previous step. Example
shown below. Get the PostgreSQL URL from the AWS RDS console.

```bash
pgcli postgres://username:password@atlas-database.xxxxxxxxx.us-east-2.rds.amazonaws.com/atlas
```
5. Load the `atlasschema.sql` file into pgcli which will automatically run all DDL commands.

#### Create TLS Artifacts

Refer to the [Application Security](#application-security) section for TLS security architecture.

All private keys, certificates and keystores are created manually by a security administrator
external to the automated deployment process, though a shell script
[atlas-build-certs.sh](atlas-env/atlas-build-certs.sh) is available to expeditiously
create all necessary TLS artifacts as well as copy the relevant artifacts to an S3 bucket used
during service deplooyment on AWS. The procedure below uses that
[atlas-build-certs.sh](atlas-env/atlas-build-certs.sh). Anyone wishing to forgo the use
of [atlas-build-certs.sh](atlas-env/atlas-build-certs.sh) and create the TLS artifacts
manually, please refer to the shell script code as it is quite straightforward and well-documented
to allow one to extract the necessary steps that must be performed manually. Another option one may
consider it to use the [atlas-build-certs.sh](atlas-env/atlas-build-certs.sh) as the
base, and make necessary adjustments to create a customized script.

Each step is described as to the purpose and, where applicable, supplemented with the command to
execute on [atlas-build-certs.sh](atlas-env/atlas-build-certs.sh). Password values are
defaulted to `changeit` in the example commands. Please substitute appropriate desired password
value in place of the default password `changeit`.

1. Create a secure S3 bucket at `s3://atlas-security`. 
2. Create a self-signed TLS certificate that serves as an internal certificate authority, and copy
the internal CA certificate (`.crt`) file to `s3://atlas-security`
   
   ```bash
   atlas-build-certs.sh -t internal-ca -p changeit -s s3://atlas-security
   ```

3. For each dedicated service TLS artifact not intended for the AWS load balancers
   1. Create a private key and a Certificate Signature Request (CSR) for each of the dedicated
service.
   2. Sign each dedicated service CSR using Internal-CA certificate.
   3. Create a PKCS12 keystore for each dedicated service, and import the dedicated private key,
Internal-CA-Signed dedicated certificate, and the Internal-CA certificate itself.
   4. Create a simple text file for each service, and add one line with the keystore password used
to create the PKCS12 keystore. 
   5. Copy the PKCS12 keystore files and the password files for all service to
`s3://atlas-security`. Note that the password should ideally be kept in password vault system such
as CyberArk, but for this application a secure S3 bucket should suffice.

      ```bash
      # All steps between sub-steps in step 3 are automated with a single execution of the
atlas-build-certs.sh script for each of the dedicated service artifact. The -i is the alias of the
Internal CA certificate and -P is the password for the Internal CA certificate, and this is repeated
for each dedicated TLS artifact for sigining the CSR

      atlas-build-certs.sh -t product -p changeit -i internal-ca -P changeit -s s3://atlas-security
      atlas-build-certs.sh -t participant -p changeit -i internal-ca -P changeit -s
s3://atlas-security
      atlas-build-certs.sh -t order -p changeit -i internal-ca -P changeit -s s3://atlas-security
      atlas-build-certs.sh -t orderbook -p changeit -i internal-ca -P changeit -s
s3://atlas-security
      atlas-build-certs.sh -t trade -p changeit -i internal-ca -P changeit -s s3://atlas-security
      atlas-build-certs.sh -t apigateway -p changeit -i internal-ca -P changeit -s
s3://atlas-security
      atlas-build-certs.sh -t discovery -p changeit -i internal-ca -P changeit -s
s3://atlas-security
      atlas-build-certs.sh -t config -p changeit -i internal-ca -P changeit -s s3://atlas-security
      ```
4.  Create certificate and private key for load balancers to import into the AWS Certificate Manager
(ACM). An ACM ARN for each imported certificate is provided in the load balancer Cloudformation
scripts, with which AWS retrieves these artifacts from ACM when instantiating the load balancers.
Refer to https://docs.aws.amazon.com/acm/latest/userguide/import-certificate.html for details on
process of importing certificate into ACM. The ACM import requires three inputs
       *  `Certificate body`: Public key certificate (`.crt`) of the TLS certificate key-pair in PEM
format
       *  `Certificate private key` Un-encrypted private key of the TLS certificate key-pair in PEM
format
       *  `Certificate chain`: Internal CA certificate (`.crt`) in PEM format

    The [atlas-build-certs.sh](atlas-env/atlas-build-certs.sh) creates `Certificate
body` and `Certificate private key` needed for by running the following commands. These will be
ouptut in files with pattern `*-acm-certificate-body.pem` and `*-acm-certificate-private-key.pem`.  

      ```bash
      atlas-build-certs.sh -t ilb -p ilb -i internal-ca -P internalca -r us-east-2 -s
s3://atlas-security
      atlas-build-certs.sh -t elb -p elb -i internal-ca -P internalca -f atlas.naperiltech.com -s
s3://atlas-security
      ```

      Note that the internal load balancer command is provided a required `-r` option to specify AWS
region. The shell script uses this value to determine the DNS to attach in the Subject Alternative
Names extensions when creating the certificate for the internal load balancer. This is dould be the
DNS suffix that AWS assigns to internal load balancer. For example, if `-r` is set to us-east-2, the
DNS is `*.us-east-2.elb.amazonaws.com`

      Similarly the external load balancer (i.e. internet-facing) is provided an optional `-f`
option to speicfy the publicly registered domain name. The shell script uses this value to determine
the DNS to attach in the Subject Alternative Names extensions when creating the certificate for the
internet-facing load balancer. If, however, intention is to not map the internet-facing load
balancer to a publicly registered domain name, then the `-r` option is required to provide the AWS
region. Like the case with internal load balancer, the shell script will determine the DNS name. In
the case of internet-facing network load balancer, however, the pattern for the DNS name has a
subtle difference as compared to that of internal applicaiton load balancer. For example, if `-r` is
set to us-east-2, the DNS is `*.elb.us-east-2.amazonaws.com`

5.  Import the certificate and private key PEM for the internal ALB and the internet-facing ELB into
the ACM. 
    * Refer to https://docs.aws.amazon.com/acm/latest/userguide/import-certificate.html for
procedure 
    * Use the above described `Ceritifcate body` and `Certificate private key` ACM inputs for each
of the load balancer certificate. 
    * Use the `internal-ca.crt` from above for the `Certificate chain` ACM input.
    * **IMPORTANT**: Tag each certificate as follows. The load balancer CloudFormation scripts will
use these tag names to look up the certificate ARN.
      * Internal Load Balancer:  `atlas-ilb`
      * Internet-facing Load Balancer: `atlas-elb`

#### Maven Build and Docker Build Services

1. Change to directory `atlas-env` in your local git repository where you cloned
https://github.com/sambacha/atlas-engine.
2.  Run the `atlas-build-aws.sh` as shown below to perform maven build, docker images build, and
push the docker images to dockerhub. It will perform these actions on all Infrastructure Services &
Application Services. 

```console
atlas-build-aws.sh -b build-services -r <aws region name> -c <absolute path to
atlas-env in your local git clone of https://github.com/sambacha/atlas-engine> -s
<absolute path to parent directory of your local git repository> -i <Docker Hub username>
```
3. The shell script will take upwards of 30 to 45 minutes to perform all maven builds, docker
builds, docker push and create AWS infrastructure. You can monitor the output from the
`atlas-build-aws.sh` to track the progress. The progress can also be tracked in the AWS
Cloudformation console.

#### Deploy Services

1. Change to directory `atlas-env` in your local git repository where you cloned
https://github.com/sambacha/atlas-engine.
2.  Run the `atlas-build-aws.sh` as shown below to build & deploy Infrastructure Services &
Application Services in your desired AWS region. Note that the `-d` and `-f` are optional. These two
optional argument allow you to create a DNS record in AWS Route 53 for your publicly registered
domain to the external API available via the internet-facing load balancer. If you don't have a
publicly registered domain name or do not wish to create a public DNS record, you may omit the `-d`
and `-f` options.

```console
atlas-build-aws.sh -b deploy-services -r <aws region name> -c <absolute path to
atlas-env in your local git clone of https://github.com/sambacha/atlas-engine> -g <URI
of the configuration git repository> -u <git username of configuration git repository> -p <git
password of configuraiton git repository> -k <AWS key-pair name> -i <Docker Hub username> -d <route
53 hosted zone id> -f <publicly registerd url>
```
3. When the shell script is complete, the atlas system is fully deployed with the necessary AWS
infrastructure and services running and ready for API executions. Specifically this script will
   1. Deploy all services in their dedicated EC2 instances
   2. Create an internal Application Load Balancer with the Configuration Service, Discover Service
and API Gateway Service as the targets.
   3. Create an internet-facing Elastic Load Balancer with API Gateway Services as the targets.
   4. If public DNS record and URL are supplied a DNS record is created to the internet-facing ELB
to complete the path from the public Internet to the APIs offered by the application services.

# Application Security 

There are two parts to application security
* Data Encryption 
* User Authentication & Authorization

Version 2.1 adds the Data Encryption aspect of application security

## Data Encryption

All service-to-service and service-to-load-balancer traffic is secured using non-mutual TLS. The
figure below shows the communication pathways that are secure HTTPS.

![TLS Security](atlas-docs/TLSSecurityFigure.png) 

Each Spring Boot service is configured with a SSL/TLS certificate PKCS12 (.p12) keystore. The
keystore has a TLS certificate dedicated to the service. The Internet-Facing ELB and Internal ALB
are also configured with a dedicated TLS certificate. All dedicated certificates are signed by a
common self-signed TLS certificate that is treated like an internal certificate authority. 

The internal CA TLS certificate is then imported into the Java `cacerts` as a trusted certificate at
the time of docker startup for each Spring Boot service, thus establishing all dedicated
certificates as trusted certificates. This approach mimics the dedicated certificates as being a
real CA-signed certificates. The TLS certificates for the AWS load balancers are imported into the
AWS Certificate Manager (ACM). The dedicated certificates imported into ACM are also imported with
the internal CA TLS certificate as a certificate chain thereby allowing the load balancers to trust
the server certificate provided to them when the load balancer acts in client role.

While the AWS load balancers access the certificate from the ACM, the Spring Boot services access
them from the TLS keystore configured in the service configuration file. The keystore file itself is
copied from a pre-designated S3 bucket into a directory on the EC2 instance where the service runs
as a docker container. The internal CA TLS certificate is also copied from the same S3 bucket into
the same directory on EC2. The directory is then mapped to a docker container volume from which the
Spring Boot application accesses the keystore and the internal CA TLS certificate. The internal CA
TLS certificate is imported into the Java `cacerts` as a trusted certificate at the time of docker
startup for each service.

All private keys, certificates and keystores are created manually by a security administrator
external to the automated deployment process, though a shell script
[atlas-build-certs.sh](atlas-env/atlas-build-certs.sh) is available to expeditiously
create all necessary TLS artifacts as well as copy the relevant artifacts to an S3 bucket. The
figure below captures the static picture and the certificate creation and propagation workflow. Most
of the workflow is automated with use of
[atlas-build-certs.sh](atlas-env/atlas-build-certs.sh) and the EC2 cloudformation,
although a system administrator may opt to forgo the use of
[atlas-build-certs.sh](atlas-env/atlas-build-certs.sh) and create the TLS artifacts
manually. 

![TLS Security Certificate Process](atlas-docs/TLSSecurityFigure2.png) 

In the event that system administrator wishes to create the TLS artifacts manually, the procedure
below describes the required actions which are codified in the
[atlas-build-certs.sh](atlas-env/atlas-build-certs.sh).

1. Create a self-signed TLS certificate that serves as an internal certificate authority.
2. Create a private key and a Certificate Signature Request (CSR) for each of the dedicated service
as well as the internal ALB and internet-facing ELB.
3. Sign each dedicated service CSR using Internal-CA certificate.
4. Create a PKCS12 keystore for each dedicated service, and import the dedicated private key,
Internal-CA-Signed dedicated certificate, and the Internal-CA certificate itself.
5. Create a simple text file for each service, and add one line with the keystore password used to
create the PKCS12 keystore. 
6. Create a secure S3 bucket at `s3://atlas-security`.
7. Copy the PKCS12 keystore files and the password files for all service to `s3://atlas-security`.
Note that the password should ideally be kept in password vault system such as CyberArk, but for
this application a secure S3 bucket should suffice.
8. Copy the internal CA certificate file to `s3://atlas-security`

The automated AWS deployment automatically perform the following steps to use the content of
`s3://atlas-security`

1. The EC2 CloudFormation script contains startup code that will 
   1. copy from `s3://atlas-security` into a root-protected folder at `/atlas-security` on the EC2
instance the appropriate keystore file and password file for the service as well as the Internal-CA
certificate. 
   2. Run the docker image and map `/atlas-security` on EC2 to a `/atlas-security` inside the docker
container.
2. Docker `ENTRYPOINT` will import the Internal-CA certificate form its mapped `/atlas-security`
into the `cacerts` of the JDK in the docker image
3. Each Spring Boot Application has SSL keystore configuration pointing to the `/atlas-security` to
use the TLS certificate of the service.


