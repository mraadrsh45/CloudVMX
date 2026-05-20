#!/bin/bash

# CloudVMX 3-in-1 Docker Setup Script
# Builds and runs Kali, Windows Dev, Android, Backend, and Frontend

set -e

echo ""
echo "================================================"
echo "  CloudVMX 3-in-1 Docker Environment Setup"
echo "================================================"
echo "  - Kali Linux (Security Testing)"
echo "  - Windows Development (Web/Desktop Dev)"
echo "  - Android Development (Mobile Dev)"
echo "  - CloudVMX Backend (Spring Boot)"
echo "  - CloudVMX Frontend (JavaFX)"
echo "================================================"
echo ""

# Check Docker
echo "STEP 1: Checking Docker installation..."
if ! command -v docker &> /dev/null; then
    echo "X Docker is not installed"
    echo "Please install Docker: https://www.docker.com/products/docker-desktop"
    exit 1
fi
echo "+ Docker found: $(docker --version)"
echo ""

# Check Docker daemon
echo "STEP 2: Waiting for Docker daemon..."
if ! docker info > /dev/null 2>&1; then
    echo "Starting Docker daemon..."
    open -a Docker 2>/dev/null || sudo systemctl start docker || true
    echo "Waiting 30 seconds for Docker to initialize..."
    sleep 30
fi
echo "+ Docker daemon is ready"
echo ""

# Navigate to project
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

# Create project directories
echo "STEP 3: Creating project directories..."
mkdir -p kali-projects windows-projects android-projects
echo "+ Directories created"
echo ""

# Build images
echo "STEP 4: Building Docker images (this may take 10-15 minutes)..."
echo ""

echo "[4.1] Building Backend image..."
docker build -t cloudvmx-backend:latest "cloudvmx-backend (3)/cloudvmx-backend"
echo "+ Backend image built"
echo ""

echo "[4.2] Building Frontend image..."
docker build -t cloudvmx-client:latest "cloudvmx/cloudvmx-client"
echo "+ Frontend image built"
echo ""

echo "[4.3] Building Kali Linux image..."
docker build -t cloudvmx-kali:latest -f Dockerfile.kali .
echo "+ Kali image built"
echo ""

echo "[4.4] Building Windows Dev image..."
docker build -t cloudvmx-windows:latest -f Dockerfile.windows .
echo "+ Windows Dev image built"
echo ""

echo "[4.5] Building Android Dev image..."
docker build -t cloudvmx-android:latest -f Dockerfile.android .
echo "+ Android Dev image built"
echo ""

# Verify images
echo "STEP 5: Verifying all images..."
echo ""
docker images | grep cloudvmx
echo ""

# Start services
echo "STEP 6: Starting all services..."
docker-compose -f docker-compose-3in1.yml up -d
echo "+ All services started"
echo ""

# Wait for initialization
echo "Waiting for services to initialize..."
sleep 10
echo ""

# Show status
echo "STEP 7: Service Status"
echo "================================================"
docker-compose -f docker-compose-3in1.yml ps
echo "================================================"
echo ""

echo ""
echo "================================================"
echo "  Setup Complete!"
echo "================================================"
echo ""
echo "Services:"
echo ""
echo "  Backend:          http://localhost:8080"
echo "  Frontend:         http://localhost:3000"
echo "  Kali Linux:       docker exec -it cloudvmx-kali bash"
echo "  Windows Dev:      docker exec -it cloudvmx-windows bash"
echo "  Android Dev:      docker exec -it cloudvmx-android bash"
echo ""
echo "Useful Commands:"
echo ""
echo "  Start all:        docker-compose -f docker-compose-3in1.yml up -d"
echo "  Stop all:         docker-compose -f docker-compose-3in1.yml down"
echo "  View logs:        docker-compose -f docker-compose-3in1.yml logs -f"
echo "  Kali terminal:    docker exec -it cloudvmx-kali bash"
echo "  Windows terminal: docker exec -it cloudvmx-windows bash"
echo "  Android terminal: docker exec -it cloudvmx-android bash"
echo "  List containers:  docker ps -a"
echo "  Remove all:       docker-compose -f docker-compose-3in1.yml down -v"
echo ""
