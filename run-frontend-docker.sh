#!/bin/bash

# CloudVMX Frontend Docker Setup
# Run this script to build and run frontend in Docker

echo ""
echo "====================================="
echo "  CloudVMX Frontend Docker Setup"
echo "====================================="
echo ""

# Check if Docker is installed
echo "STEP 1: Checking Docker..."
if ! command -v docker &> /dev/null; then
    echo "X Docker is not installed"
    echo "Please install Docker: https://www.docker.com/products/docker-desktop"
    exit 1
fi
echo "+ Docker is installed"
echo ""

# Check if Docker daemon is running
echo "STEP 2: Checking Docker daemon..."
docker ps >& /dev/null
if [ $? -ne 0 ]; then
    echo "Docker daemon is not running"
    echo "Starting Docker..."
    open -a Docker 2>/dev/null || sudo systemctl start docker
    echo "Waiting 30 seconds for Docker to initialize..."
    sleep 30
else
    echo "+ Docker daemon is running"
fi
echo ""

# Build frontend image
echo "STEP 3: Building frontend Docker image..."
cd "$(dirname "$0")"

FRONTEND_DIR="cloudvmx/cloudvmx-client"
echo "Building from: $FRONTEND_DIR"
echo ""

docker build -t cloudvmx-client:latest -t cloudvmx-client:0.0.1 "$FRONTEND_DIR"
if [ $? -ne 0 ]; then
    echo "X Docker build failed"
    exit 1
fi

echo ""
echo "+ Frontend image built successfully"
echo ""

# Verify image
echo "STEP 4: Verifying image..."
docker images | grep cloudvmx-client
echo ""

# Stop existing container
echo "STEP 5: Stopping any existing container..."
docker stop cloudvmx-client >& /dev/null
docker rm cloudvmx-client >& /dev/null
echo ""

# Run container
echo "STEP 6: Running frontend container..."
docker run -d \
    --name cloudvmx-client \
    -p 3000:3000 \
    -e JAVA_OPTS="-Xmx512m -Xms256m" \
    -e BACKEND_URL="http://localhost:8080" \
    cloudvmx-client:latest

if [ $? -ne 0 ]; then
    echo "X Failed to start container"
    exit 1
fi

echo "+ Frontend container started"
echo ""

# Check status
echo "STEP 7: Checking container status..."
sleep 3
docker ps | grep cloudvmx-client
if [ $? -ne 0 ]; then
    echo "Container logs:"
    docker logs cloudvmx-client
else
    echo "+ Container is running"
fi

echo ""
echo "====================================="
echo "  Setup Complete!"
echo "====================================="
echo ""
echo "Frontend URL: http://localhost:3000"
echo "Backend URL: http://localhost:8080"
echo ""
echo "Useful commands:"
echo "  docker logs -f cloudvmx-client          View logs"
echo "  docker stop cloudvmx-client             Stop container"
echo "  docker restart cloudvmx-client          Restart container"
echo "  docker ps                               View running containers"
echo ""
