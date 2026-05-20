# CloudVMX Docker Build Script
# This script builds all Docker images for the CloudVMX project

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "CloudVMX Docker Build Script" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Get the script directory
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# Check if Docker is running
Write-Host "Checking Docker daemon..." -ForegroundColor Yellow
try {
    $dockerStatus = docker info 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Docker is running" -ForegroundColor Green
    }
    else {
        Write-Host "✗ Docker is not responding properly" -ForegroundColor Red
        Write-Host "Please start Docker Desktop and try again." -ForegroundColor Yellow
        exit 1
    }
}
catch {
    Write-Host "✗ Docker command failed" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Build Backend
Write-Host "Building cloudvmx-backend..." -ForegroundColor Yellow
docker build -t cloudvmx-backend:latest -t cloudvmx-backend:0.0.1 `
    "cloudvmx-backend (3)/cloudvmx-backend"
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Backend build failed" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Backend image built successfully" -ForegroundColor Green

Write-Host ""

# Build Client
Write-Host "Building cloudvmx-client..." -ForegroundColor Yellow
docker build -t cloudvmx-client:latest -t cloudvmx-client:0.0.1 `
    "cloudvmx/cloudvmx-client"
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Client build failed" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Client image built successfully" -ForegroundColor Green

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Build Complete!" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# List built images
Write-Host "Built images:" -ForegroundColor Yellow
docker images | findstr cloudvmx

Write-Host ""
Write-Host "To start the services, run:" -ForegroundColor Cyan
Write-Host "  docker-compose up -d" -ForegroundColor White
Write-Host ""
