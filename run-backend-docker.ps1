# Start Docker Desktop and build backend
# Run this script to set up and run backend in Docker

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "CloudVMX Backend Docker Setup" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check if Docker Desktop needs to be started
Write-Host "STEP 1: Checking Docker..." -ForegroundColor Yellow
$dockerRunning = $false
try {
    $dockerTest = docker info 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Docker is running" -ForegroundColor Green
        $dockerRunning = $true
    }
}
catch {
    $dockerRunning = $false
}

if (-not $dockerRunning) {
    Write-Host "✗ Docker Desktop is not running" -ForegroundColor Red
    Write-Host ""
    Write-Host "Starting Docker Desktop..." -ForegroundColor Yellow
    
    # Try to start Docker Desktop
    $dockerPath = "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    if (Test-Path $dockerPath) {
        & $dockerPath
        Write-Host "Docker Desktop started. Waiting 30 seconds for it to fully initialize..." -ForegroundColor Yellow
        Start-Sleep -Seconds 30
    }
    else {
        Write-Host "✗ Docker Desktop not found at $dockerPath" -ForegroundColor Red
        Write-Host "Please manually start Docker Desktop and run this script again." -ForegroundColor Yellow
        exit 1
    }
}

Write-Host ""

# Step 2: Verify connection to Docker daemon
Write-Host "STEP 2: Verifying Docker connection..." -ForegroundColor Yellow
$maxRetries = 5
$retryCount = 0
$connected = $false

while ($retryCount -lt $maxRetries -and -not $connected) {
    try {
        $result = docker ps 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ Connected to Docker daemon" -ForegroundColor Green
            $connected = $true
        }
    }
    catch {
        $retryCount++
        if ($retryCount -lt $maxRetries) {
            Write-Host "Retrying... ($retryCount/$maxRetries)" -ForegroundColor Yellow
            Start-Sleep -Seconds 3
        }
    }
}

if (-not $connected) {
    Write-Host "✗ Cannot connect to Docker daemon" -ForegroundColor Red
    Write-Host "Please check Docker Desktop status and try again." -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Step 3: Navigate to project directory
Write-Host "STEP 3: Building backend Docker image..." -ForegroundColor Yellow
$projectRoot = "c:\Users\luxma\Desktop\cloudvmx"
cd $projectRoot

# Get backend directory path
$backendDir = "cloudvmx-backend (3)\cloudvmx-backend"

Write-Host "Building from: $backendDir" -ForegroundColor Gray
Write-Host ""

# Build the image with detailed output
docker build `
    -t cloudvmx-backend:latest `
    -t cloudvmx-backend:0.0.1 `
    -f "$backendDir\dockerfile" `
    "$backendDir" `
    --progress=plain

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Docker build failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "✓ Backend image built successfully" -ForegroundColor Green
Write-Host ""

# Step 4: Verify image
Write-Host "STEP 4: Verifying image..." -ForegroundColor Yellow
docker images | findstr cloudvmx-backend
Write-Host ""

# Step 5: Run container
Write-Host "STEP 5: Running backend container..." -ForegroundColor Yellow

# Stop any existing container
Write-Host "Stopping any existing backend container..." -ForegroundColor Gray
docker stop cloudvmx-backend 2>$null
docker rm cloudvmx-backend 2>$null

# Run the new container
docker run -d `
    --name cloudvmx-backend `
    -p 8080:8080 `
    -e JAVA_OPTS="-Xmx512m -Xms256m" `
    cloudvmx-backend:latest

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Failed to start container" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Backend container started" -ForegroundColor Green
Write-Host ""

# Step 6: Verify container is running
Write-Host "STEP 6: Checking container status..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

$containerStatus = docker inspect -f '{{.State.Running}}' cloudvmx-backend
if ($containerStatus -eq "true") {
    Write-Host "✓ Container is running" -ForegroundColor Green
}
else {
    Write-Host "Container might not be running yet, checking logs..." -ForegroundColor Yellow
    docker logs cloudvmx-backend
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Setup Complete!" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Backend is running at: http://localhost:8080" -ForegroundColor White
Write-Host ""

Write-Host "Useful commands:" -ForegroundColor Yellow
Write-Host "  View logs:        docker logs -f cloudvmx-backend" -ForegroundColor White
Write-Host "  Stop container:   docker stop cloudvmx-backend" -ForegroundColor White
Write-Host "  Restart container: docker restart cloudvmx-backend" -ForegroundColor White
Write-Host "  Remove container: docker rm -f cloudvmx-backend" -ForegroundColor White
Write-Host "  View running containers: docker ps" -ForegroundColor White
Write-Host ""
