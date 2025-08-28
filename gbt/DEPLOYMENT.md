# GBT Application Deployment Guide

This guide explains how to deploy the GBT application both locally using Docker Compose and to AWS using CloudFormation.

## Table of Contents
1. [Local Development with Docker Compose](#local-development-with-docker-compose)
2. [AWS Deployment with CloudFormation](#aws-deployment-with-cloudformation)

## Local Development with Docker Compose

### Prerequisites

- Docker and Docker Compose installed
- Java 17 or higher (for local development without Docker)
- Gradle (for local development without Docker)

### Quick Start

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd gbt
   ```

2. Start the application with Docker Compose:
   ```bash
   docker-compose up -d
   ```

3. Wait for all services to start (this might take a few minutes on first run)

4. Access the application:
   - API: http://localhost:8080/api/funds
   - MongoDB Express (Admin UI): http://localhost:8081

### Services

- **gbt-application**: Spring Boot application (port 8080)
- **mongo**: MongoDB database (port 27017)
- **mongo-express**: Web-based MongoDB admin UI (port 8081)

### Environment Variables

The following environment variables can be configured in the `docker-compose.yml` file:

- `SPRING_DATA_MONGODB_URI`: MongoDB connection string
- `APP_SECURITY_CLIENT_USERNAME`: Client username (default: client)
- `APP_SECURITY_CLIENT_PASSWORD`: Client password (default: client123)
- `APP_SECURITY_ADMIN_USERNAME`: Admin username (default: admin)
- `APP_SECURITY_ADMIN_PASSWORD`: Admin password (default: admin123)

### Running Tests

To run tests locally:

```bash
./gradlew test
```

### Stopping the Application

```bash
docker-compose down
```

To remove volumes (including database data):

```bash
docker-compose down -v
```

## AWS Deployment with CloudFormation

### Prerequisites

1. AWS CLI configured with appropriate permissions
2. Docker installed and running
3. AWS ECR repository for the application
4. MongoDB Atlas cluster (or self-hosted MongoDB)
5. VPC with at least 2 public subnets

## Deployment Steps

### 1. Package the Application

First, build the application JAR:

```bash
./gradlew clean build
```

### 2. Build and Push Docker Image

1. Authenticate Docker to your ECR registry:
   ```bash
   aws ecr get-login-password --region <region> | docker login --username AWS --password-stdin <account-id>.dkr.ecr.<region>.amazonaws.com
   ```

2. Build the Docker image:
   ```bash
   docker build -t <account-id>.dkr.ecr.<region>.amazonaws.com/gbt-application:latest .
   ```

3. Push the image to ECR:
   ```bash
   docker push <account-id>.dkr.ecr.<region>.amazonaws.com/gbt-application:latest
   ```

### 3. Deploy with CloudFormation

1. Create a CloudFormation stack using the template:
   ```bash
   aws cloudformation create-stack \
     --stack-name gbt-application-stack \
     --template-body file://cloudformation/backend.yaml \
     --parameters \
         ParameterKey=EnvironmentName,ParameterValue=prod \
         ParameterKey=VpcId,ParameterValue=vpc-xxxxxxxx \
         ParameterKey=SubnetIds,ParameterValue=subnet-xxxxxxx\,subnet-xxxxxxx \
         ParameterKey=MongoDBUri,ParameterValue=your-mongodb-uri \
     --capabilities CAPABILITY_NAMED_IAM
   ```

2. Monitor the stack creation:
   ```bash
   aws cloudformation describe-stacks --stack-name gbt-application-stack --query 'Stacks[0].StackStatus'
   ```

### 4. Verify Deployment

1. Get the service URL:
   ```bash
   aws cloudformation describe-stacks --stack-name gbt-application-stack --query 'Stacks[0].Outputs[?OutputKey==`ServiceURL`].OutputValue' --output text
   ```

2. Test the API endpoints:
   ```bash
   curl <service-url>/api/funds
   ```

## Configuration

The following environment variables can be configured:

- `SPRING_DATA_MONGODB_URI`: MongoDB connection string
- `APP_SECURITY_CLIENT_USERNAME`: Client username (default: client)
- `APP_SECURITY_CLIENT_PASSWORD`: Client password (default: client123)
- `APP_SECURITY_ADMIN_USERNAME`: Admin username (default: admin)
- `APP_SECURITY_ADMIN_PASSWORD`: Admin password (default: admin123)

## Updating the Application

1. Build and push a new Docker image with a new tag
2. Update the CloudFormation stack with the new image tag

## Cleanup

To delete all resources:

```bash
aws cloudformation delete-stack --stack-name gbt-application-stack
```
## Security Considerations

- Always use HTTPS in production
- Rotate database credentials regularly
- Restrict IAM permissions following the principle of least privilege
- Enable VPC flow logs for monitoring network traffic
- Use AWS Secrets Manager for sensitive configuration

## Troubleshooting

Check CloudWatch Logs for application logs:

```bash
aws logs describe-log-groups --query 'logGroups[?contains(logGroupName,`gbt-application`)].logGroupName' --output text | xargs -I {} aws logs tail {}
```

For ECS service events:

```bash
aws ecs describe-services \
  --cluster gbt-application-prod-cluster \
  --services gbt-application-prod-service \
  --query 'services[0].events[0:10]'
