@echo off
REM Start Docker and build frontend client
REM Run this batch file to set up and run frontend in Docker

setlocal enabledelayedexpansion

echo.
echo =====================================
echo  CloudVMX Frontend Docker Setup
echo =====================================
echo.

REM Step 1: Check if Docker is installed
echo STEP 1: Checking Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo X Docker is not installed or not in PATH
    echo Please install Docker Desktop: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)
echo + Docker is installed
echo.

REM Step 2: Start Docker Desktop if not running
echo STEP 2: Starting Docker Desktop...
docker ps >nul 2>&1
if errorlevel 1 (
    echo Docker Desktop is starting...
    start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    echo Waiting 40 seconds for Docker to initialize...
    timeout /t 40 /nobreak
) else (
    echo + Docker is already running
)
echo.

REM Step 3: Navigate to project and build
echo STEP 3: Building frontend Docker image...
cd /d "c:\Users\luxma\Desktop\cloudvmx"

set FRONTEND_DIR=cloudvmx\cloudvmx-client
echo Building from: %FRONTEND_DIR%
echo.

docker build -t cloudvmx-client:latest -t cloudvmx-client:0.0.1 "%FRONTEND_DIR%"
if errorlevel 1 (
    echo X Docker build failed
    pause
    exit /b 1
)

echo.
echo + Frontend image built successfully
echo.

REM Step 4: Verify image
echo STEP 4: Verifying image...
docker images | find "cloudvmx-client"
echo.

REM Step 5: Stop any existing container
echo STEP 5: Stopping any existing container...
docker stop cloudvmx-client >nul 2>&1
docker rm cloudvmx-client >nul 2>&1
echo.

REM Step 6: Run container
echo STEP 6: Running frontend container...
docker run -d ^
    --name cloudvmx-client ^
    -p 3000:3000 ^
    -e JAVA_OPTS="-Xmx512m -Xms256m" ^
    -e BACKEND_URL="http://localhost:8080" ^
    cloudvmx-client:latest

if errorlevel 1 (
    echo X Failed to start container
    pause
    exit /b 1
)

echo + Frontend container started
echo.

REM Step 7: Check status
echo STEP 7: Checking container status...
timeout /t 3 /nobreak >nul
docker ps | find "cloudvmx-client"
if errorlevel 1 (
    echo Container status:
    docker logs cloudvmx-client
) else (
    echo + Container is running
)

echo.
echo =====================================
echo  Setup Complete!
echo =====================================
echo.
echo Frontend URL: http://localhost:3000
echo Backend URL: http://localhost:8080
echo.
echo Useful commands:
echo   docker logs -f cloudvmx-client          View logs
echo   docker stop cloudvmx-client             Stop container
echo   docker restart cloudvmx-client          Restart container
echo   docker ps                               View running containers
echo.

pause
