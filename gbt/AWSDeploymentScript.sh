#!/bin/bash

set -e

# Configuration
APP_NAME="gbt-application"
ENVIRONMENT="prod"
REGION="us-west-2"
STACK_NAME="${APP_NAME}-${ENVIRONMENT}"

echo "🚀 Deploying ${APP_NAME} to AWS..."

# Build and push Docker image to ECR
echo "📦 Building Docker image..."
docker build -t ${APP_NAME}:latest .

# Get ECR repository URI from CloudFormation stack
ECR_URI=$(aws cloudformation describe-stacks \
    --stack-name ${STACK_NAME} \
    --query 'Stacks[0].Outputs[?OutputKey==`ECRRepositoryURI`].OutputValue' \
    --output text \
    --region ${REGION})

if [ -z "$ECR_URI" ]; then
    echo "❌ ECR Repository URI not found. Make sure CloudFormation stack is deployed."
    exit 1
fi

echo "🏷️  Tagging image for ECR..."
docker tag ${APP_NAME}:latest ${ECR_URI}:latest

echo "🔐 Logging into ECR..."
aws ecr get-login-password --region ${REGION} | docker login --username AWS --password-stdin ${ECR_URI}

echo "⬆️  Pushing image to ECR..."
docker push ${ECR_URI}:latest

echo "🔄 Updating ECS service..."
aws ecs update-service \
    --cluster ${APP_NAME}-${ENVIRONMENT}-cluster \
    --service ${APP_NAME}-${ENVIRONMENT}-service \
    --force-new-deployment \
    --region ${REGION}

echo "✅ Deployment completed successfully!"
