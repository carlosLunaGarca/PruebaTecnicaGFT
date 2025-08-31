#!/bin/bash

# =============================================================================
# GBT - Production Deployment Script
# =============================================================================

set -euo pipefail

# Configuration
STACK_NAME="gbt-production"
ENVIRONMENT="prod"
REGION="us-east-1"
ECR_REPO_NAME="gbt-application"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Cleanup function
cleanup() {
    if [[ $? -ne 0 ]]; then
        log_error "Deployment failed. Check logs above for details."
        log_info "Rolling back if necessary..."
        # Add rollback logic here if needed
    fi
}

trap cleanup EXIT

# Prerequisites check
check_prerequisites() {
    log_info "Checking prerequisites..."

    # Check AWS CLI
    if ! command -v aws &> /dev/null; then
        log_error "AWS CLI is not installed"
        exit 1
    fi

    # Check Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed"
        exit 1
    fi

    # Check AWS credentials
    if ! aws sts get-caller-identity &> /dev/null; then
        log_error "AWS credentials not configured"
        exit 1
    fi

    # Check if running tests pass
    log_info "Running tests..."
    ./gradlew test || {
        log_error "Tests failed. Aborting deployment."
        exit 1
    }

    log_success "Prerequisites check passed"
}

# Build and push Docker image
build_and_push_image() {
    log_info "Building and pushing Docker image..."

    # Get AWS account ID
    ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
    ECR_URI="${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/${ECR_REPO_NAME}"

    # Login to ECR
    aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ECR_URI

    # Build image with production optimizations
    docker build \
        --target production \
        --build-arg SPRING_PROFILES_ACTIVE=aws \
        -t $ECR_REPO_NAME:latest \
        -t $ECR_REPO_NAME:$(git rev-parse --short HEAD) \
        .

    # Tag for ECR
    docker tag $ECR_REPO_NAME:latest $ECR_URI:latest
    docker tag $ECR_REPO_NAME:$(git rev-parse --short HEAD) $ECR_URI:$(git rev-parse --short HEAD)

    # Push to ECR
    docker push $ECR_URI:latest
    docker push $ECR_URI:$(git rev-parse --short HEAD)

    log_success "Docker image built and pushed successfully"
}

# Deploy CloudFormation stack
deploy_infrastructure() {
    log_info "Deploying CloudFormation stack..."

    # Generate secure database password
    DB_PASSWORD=$(openssl rand -base64 32)

    # Deploy stack
    aws cloudformation deploy \
        --template-file cloudformation/backend.yaml \
        --stack-name $STACK_NAME \
        --parameter-overrides \
            EnvironmentName=$ENVIRONMENT \
            AppName=gbt-application \
            DBUser=gbtuser \
        --capabilities CAPABILITY_NAMED_IAM CAPABILITY_IAM \
        --region $REGION \
        --no-fail-on-empty-changeset

    if [[ $? -eq 0 ]]; then
        log_success "CloudFormation stack deployed successfully"
    else
        log_error "CloudFormation stack deployment failed"
        exit 1
    fi
}

# Update ECS service
update_ecs_service() {
    log_info "Updating ECS service..."

    # Get cluster and service names from stack outputs
    CLUSTER_NAME=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`ECSClusterName`].OutputValue' \
        --output text \
        --region $REGION)

    SERVICE_NAME=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`ECSServiceName`].OutputValue' \
        --output text \
        --region $REGION)

    # Force new deployment
    aws ecs update-service \
        --cluster $CLUSTER_NAME \
        --service $SERVICE_NAME \
        --force-new-deployment \
        --region $REGION

    log_info "Waiting for service to stabilize..."
    aws ecs wait services-stable \
        --cluster $CLUSTER_NAME \
        --services $SERVICE_NAME \
        --region $REGION

    log_success "ECS service updated successfully"
}

# Health check
perform_health_check() {
    log_info "Performing health check..."

    # Get load balancer URL
    LB_URL=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerURL`].OutputValue' \
        --output text \
        --region $REGION)

    # Wait for health check endpoint
    for i in {1..30}; do
        if curl -f -s "$LB_URL/actuator/health" > /dev/null; then
            log_success "Health check passed"
            break
        fi

        if [[ $i -eq 30 ]]; then
            log_error "Health check failed after 5 minutes"
            exit 1
        fi

        log_info "Waiting for application to be healthy... ($i/30)"
        sleep 10
    done
}

# Display deployment info
display_deployment_info() {
    log_success "Deployment completed successfully!"
    echo ""
    log_info "Deployment Information:"

    # Get stack outputs
    aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?starts_with(OutputKey, `LoadBalancerURL`) || starts_with(OutputKey, `SwaggerURL`) || starts_with(OutputKey, `MonitoringDashboardURL`)].{Key:OutputKey,Value:OutputValue}' \
        --output table \
        --region $REGION

    echo ""
    log_info "Useful commands:"
    echo "  View logs: aws logs tail /ecs/gbt-application-prod --follow --region $REGION"
    echo "  Connect to container: aws ecs execute-command --cluster <cluster> --task <task> --container gbt-application-container --interactive --command /bin/bash"
    echo "  View metrics: Open the monitoring dashboard URL above"
}

# Main execution
main() {
    log_info "Starting GBT Production Deployment"
    log_info "Stack: $STACK_NAME"
    log_info "Region: $REGION"
    log_info "Environment: $ENVIRONMENT"
    echo ""

    check_prerequisites
    build_and_push_image
    deploy_infrastructure
    update_ecs_service
    perform_health_check
    display_deployment_info

    log_success "🚀 GBT Production Deployment Completed Successfully!"
}

# Run main function
main "$@"
