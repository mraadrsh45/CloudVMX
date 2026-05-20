#!/bin/bash

# CloudVMX Docker Build Script
# This script builds all Docker images for the CloudVMX project

echo "======================================"
echo "CloudVMX Docker Build Script"
echo "======================================"
echo ""

# Get the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Check if Docker is running
echo "Checking Docker daemon..."
if docker info > /dev/null 2>&1; then
    echo "✓ Docker is running"
else
    echo "✗ Docker is not responding properly"
    echo "Please start Docker and try again."
    exit 1
fi

echo ""

# Build Backend
echo "Building cloudvmx-backend..."
docker build -t cloudvmx-backend:latest -t cloudvmx-backend:0.0.1 \
    "cloudvmx-backend (3)/cloudvmx-backend"
if [ $? -ne 0 ]; then
    echo "✗ Backend build failed"
    exit 1
fi
echo "✓ Backend image built successfully"

echo ""

# Build Client
echo "Building cloudvmx-client..."
docker build -t cloudvmx-client:latest -t cloudvmx-client:0.0.1 \
    "cloudvmx/cloudvmx-client"
if [ $? -ne 0 ]; then
    echo "✗ Client build failed"
    exit 1
fi
echo "✓ Client image built successfully"

echo ""
echo "======================================"
echo "Build Complete!"
echo "======================================"
echo ""

# List built images
echo "Built images:"
docker images | grep cloudvmx

echo ""
echo "To start the services, run:"
echo "  docker-compose up -d"
echo ""
