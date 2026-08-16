@echo off
REM CloudVMX 3-in-1 Docker Setup Script
REM Builds and runs Kali, Windows Dev, Android, Backend, and Frontend environments

setlocal enabledelayedexpansion

echo.
echo ================================================
echo  CloudVMX 3-in-1 Docker Environment Setup
echo ================================================
echo  - Kali Linux (Security Testing)
echo  - Windows Development (Web/Desktop Dev)
echo  - Android Development (Mobile Dev)
echo  - CloudVMX Backend (Spring Boot)
echo  - CloudVMX Frontend (JavaFX)
echo ================================================
echo.

REM Check Docker installation
echo STEP 1: Checking Docker installation...
docker --version >nul 2>&1
if errorlevel 1 (
    echo X Docker is not installed
    echo Please install Docker Desktop: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)
echo + Docker found
echo.

REM Check Docker daemon
echo STEP 2: Waiting for Docker daemon...
docker info >nul 2>&1
if errorlevel 1 (
    echo Docker daemon is not running
    start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    echo Waiting 45 seconds for Docker to initialize...
    timeout /t 45 /nobreak
)
echo + Docker daemon is ready
echo.

REM Navigate to project directory
cd /d "c:\Users\luxma\Desktop\cloudvmx"

REM Create project directories
echo STEP 3: Creating project directories...
if not exist "kali-projects" mkdir kali-projects
if not exist "windows-projects" mkdir windows-projects
if not exist "android-projects" mkdir android-projects
echo + Directories created
echo.

REM Build images
echo STEP 4: Building Docker images (this may take 10-15 minutes)...
echo.

echo [4.1] Building Backend image...
docker build -t cloudvmx-backend:latest "cloudvmx-backend (3)\cloudvmx-backend" -f "cloudvmx-backend (3)\cloudvmx-backend\dockerfile"
if errorlevel 1 (
    echo X Backend build failed
    pause
    exit /b 1
)
echo + Backend image built
echo.

echo [4.2] Building Frontend image...
docker build -t cloudvmx-client:latest "cloudvmx/cloudvmx-client" -f "cloudvmx/cloudvmx-client\Dockerfile"
if errorlevel 1 (
    echo X Frontend build failed
    pause
    exit /b 1
)
echo + Frontend image built
echo.

echo [4.3] Building Kali Linux image...
docker build -t cloudvmx-kali:latest -f Dockerfile.kali .
if errorlevel 1 (
    echo X Kali build failed
    pause
    exit /b 1
)
echo + Kali image built
echo.

echo [4.4] Building Windows Dev image...
docker build -t cloudvmx-windows:latest -f Dockerfile.windows .
if errorlevel 1 (
    echo X Windows Dev build failed
    pause
    exit /b 1
)
echo + Windows Dev image built
echo.

echo [4.5] Building Android Dev image...
docker build -t cloudvmx-android:latest -f Dockerfile.android .
if errorlevel 1 (
    echo X Android Dev build failed
    pause
    exit /b 1
)
echo + Android Dev image built
echo.

REM List all built images
echo STEP 5: Verifying all images...
echo.
docker images | findstr cloudvmx
echo.

REM Start all services with docker-compose
echo STEP 6: Starting all services...
docker-compose -f docker-compose-3in1.yml up -d
if errorlevel 1 (
    echo X Failed to start services
    pause
    exit /b 1
)
echo + All services started
echo.

REM Wait for services to initialize
echo Waiting for services to initialize...
timeout /t 10 /nobreak
echo.

REM Show service status
echo STEP 7: Service Status
echo ================================================
docker-compose -f docker-compose-3in1.yml ps
echo ================================================
echo.

echo.
echo ================================================
echo  Setup Complete!
echo ================================================
echo.
echo Services:
echo.
echo Backend:          http://localhost:8080
echo Frontend:         http://localhost:3000
echo Kali Linux:       docker exec -it cloudvmx-kali bash
echo Windows Dev:      docker exec -it cloudvmx-windows bash
echo Android Dev:      docker exec -it cloudvmx-android bash
echo.
echo Useful Commands:
echo.
echo   Start all services:        docker-compose -f docker-compose-3in1.yml up -d
echo   Stop all services:         docker-compose -f docker-compose-3in1.yml down
echo   View service logs:         docker-compose -f docker-compose-3in1.yml logs -f
echo   Enter Kali terminal:       docker exec -it cloudvmx-kali bash
echo   Enter Windows terminal:    docker exec -it cloudvmx-windows bash
echo   Enter Android terminal:    docker exec -it cloudvmx-android bash
echo   List all containers:       docker ps -a
echo   Remove all services:       docker-compose -f docker-compose-3in1.yml down -v
echo.

pause
