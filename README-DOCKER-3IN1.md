# CloudVMX 3-in-1 Docker Environment Setup Guide

## Overview

This setup creates a complete development and testing environment with 5 services all running in Docker:

### Services Overview

1. **Backend (Spring Boot)** - Port 8080
   - CloudVMX Backend REST API
   - Full Java 21 environment
   - All source files included

2. **Frontend (JavaFX)** - Port 3000
   - CloudVMX Client GUI
   - Depends on Backend
   - X11 libraries included

3. **Kali Linux** - Interactive Container
   - Penetration testing tools
   - Security testing framework
   - Network analysis tools

4. **Windows Development** - Interactive Container
   - .NET SDK 8.0
   - Java 21 + Maven
   - Node.js / npm
   - Wine for Windows app compatibility

5. **Android Development** - Interactive Container
   - Android SDK & Tools
   - Android Emulator support
   - Java 21 JDK
   - ADB and platform tools

---

## Prerequisites

- Docker Desktop (latest version)
- At least 8GB RAM
- 30GB disk space (for all images and projects)
- Internet connection (for downloading dependencies)

---

## Quick Start (Windows)

### Option 1: Automated Setup (Recommended)
```batch
cd c:\Users\luxma\Desktop\cloudvmx
build-3in1-docker.bat
```

### Option 2: Manual Docker Compose
```batch
cd c:\Users\luxma\Desktop\cloudvmx
docker-compose -f docker-compose-3in1.yml up -d
```

---

## Quick Start (Linux/macOS)

### Option 1: Automated Setup
```bash
cd ~/Desktop/cloudvmx
chmod +x build-3in1-docker.sh
./build-3in1-docker.sh
```

### Option 2: Manual Docker Compose
```bash
cd ~/Desktop/cloudvmx
docker-compose -f docker-compose-3in1.yml up -d
```

---

## File Structure

```
cloudvmx/
├── Dockerfile.kali              # Kali Linux security environment
├── Dockerfile.windows           # Windows development environment
├── Dockerfile.android           # Android development environment
├── docker-compose-3in1.yml      # Orchestrates all 5 services
├── build-3in1-docker.bat        # Windows setup script
├── build-3in1-docker.sh         # Linux/macOS setup script
├── kali-projects/               # Project folder for Kali
├── windows-projects/            # Project folder for Windows Dev
├── android-projects/            # Project folder for Android Dev
├── cloudvmx-backend/            # Backend source code
├── cloudvmx/
│   └── cloudvmx-client/         # Frontend source code
└── README-DOCKER-3IN1.md        # This file
```

---

## Using Each Environment

### Backend (Spring Boot)
```powershell
# Access API
curl http://localhost:8080

# View logs
docker logs -f cloudvmx-backend

# Rebuild backend
docker-compose -f docker-compose-3in1.yml build cloudvmx-backend
```

### Frontend (JavaFX)
```powershell
# Open in browser
http://localhost:3000

# View logs
docker logs -f cloudvmx-client

# Restart frontend
docker restart cloudvmx-client
```

### Kali Linux (Security Testing)
```bash
# Enter Kali terminal
docker exec -it cloudvmx-kali bash

# Run security tools inside Kali
docker exec -it cloudvmx-kali nmap localhost

# Run metasploit
docker exec -it cloudvmx-kali msfconsole

# File sharing
# Place files in: c:\Users\luxma\Desktop\cloudvmx\kali-projects\
# Accessible in container at: /home/kali/projects/
```

### Windows Development Environment
```bash
# Enter Windows Dev terminal
docker exec -it cloudvmx-windows bash

# Use .NET
docker exec -it cloudvmx-windows dotnet --version

# Use Node.js
docker exec -it cloudvmx-windows npm --version

# Use Java/Maven
docker exec -it cloudvmx-windows mvn --version

# File sharing
# Place files in: c:\Users\luxma\Desktop\cloudvmx\windows-projects\
# Accessible in container at: /home/dev/projects/
```

### Android Development Environment
```bash
# Enter Android Dev terminal
docker exec -it cloudvmx-android bash

# List Android SDK packages
docker exec -it cloudvmx-android sdkmanager --list

# Check Android emulator
docker exec -it cloudvmx-android emulator -list-avds

# Start Android emulator
docker exec -it cloudvmx-android emulator -avd android34 &

# ADB commands
docker exec -it cloudvmx-android adb devices

# File sharing
# Place projects in: c:\Users\luxma\Desktop\cloudvmx\android-projects\
# Accessible in container at: /home/android/projects/
```

---

## Service Management

### View All Service Status
```bash
docker-compose -f docker-compose-3in1.yml ps
```

### View Logs
```bash
# All services
docker-compose -f docker-compose-3in1.yml logs -f

# Specific service
docker-compose -f docker-compose-3in1.yml logs -f cloudvmx-backend
docker-compose -f docker-compose-3in1.yml logs -f kali
docker-compose -f docker-compose-3in1.yml logs -f windows-dev
docker-compose -f docker-compose-3in1.yml logs -f android-dev
```

### Stop Services
```bash
# Stop all
docker-compose -f docker-compose-3in1.yml down

# Stop specific service
docker stop cloudvmx-backend
docker stop cloudvmx-client
docker stop cloudvmx-kali
docker stop cloudvmx-windows
docker stop cloudvmx-android
```

### Restart Services
```bash
# Restart all
docker-compose -f docker-compose-3in1.yml restart

# Restart specific service
docker restart cloudvmx-backend
docker restart cloudvmx-client
```

### Remove Everything
```bash
# Stop and remove all containers, networks, and volumes
docker-compose -f docker-compose-3in1.yml down -v
```

---

## Docker Images Created

- **cloudvmx-backend** - ~400MB (Backend API)
- **cloudvmx-client** - ~450MB (Frontend GUI)
- **cloudvmx-kali** - ~1.5GB (Kali Linux with tools)
- **cloudvmx-windows** - ~2GB (Windows Dev environment)
- **cloudvmx-android** - ~2.5GB (Android SDK + emulator)

**Total Space: ~6.8GB** (plus project volumes)

---

## Network Architecture

```
┌─────────────────────────────────────────┐
│        cloudvmx-network (bridge)        │
├─────────────────────────────────────────┤
│  Backend:8080  ← Frontend:3000         │
│  Kali:bash     ← Windows:bash          │
│  Android:bash  ← All interconnected    │
└─────────────────────────────────────────┘
```

All services can communicate with each other by hostname:
- `cloudvmx-backend` (from other services)
- `cloudvmx-client` (from other services)
- `cloudvmx-kali` (from other services)
- `cloudvmx-windows` (from other services)
- `cloudvmx-android` (from other services)

---

## Volumes

### Persistent Data Folders
- `kali-home` → `/home/kali` (Kali user data)
- `windows-home` → `/home/dev` (Windows dev user data)
- `android-home` → `/home/android` (Android user data)

### Project Directories (Local Mounts)
- `./kali-projects` ↔ `/home/kali/projects`
- `./windows-projects` ↔ `/home/dev/projects`
- `./android-projects` ↔ `/home/android/projects`

---

## Troubleshooting

### Docker daemon not running
```powershell
# Start Docker Desktop (Windows)
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"

# Or restart service (Linux)
sudo systemctl restart docker
```

### Out of disk space
```bash
docker system prune -a  # Remove unused images
docker volume prune     # Remove unused volumes
```

### Service not starting
```bash
# Check logs
docker-compose -f docker-compose-3in1.yml logs [service-name]

# Rebuild service
docker-compose -f docker-compose-3in1.yml build --no-cache [service-name]
```

### Cannot access service from another container
```bash
# Check network connectivity
docker exec -it [container] ping [other-container-name]

# Check ports
docker port [container-name]
```

---

## File Inclusion Verification

### Backend Image ✓
- pom.xml (Maven configuration)
- src/ (all source code - Java, resources)
- target/ (compiled JAR files)
- All dependencies

### Frontend Image ✓
- pom.xml (Maven configuration)
- src/ (all source code - Java, FXML, resources)
- X11 libraries (for JavaFX GUI)
- All dependencies

### Kali Linux Image ✓
- Base Kali distribution
- Top 10 penetration testing tools
- Network analysis tools
- Metasploit framework
- Additional security tools

### Windows Dev Image ✓
- Ubuntu base with development tools
- .NET SDK 8.0
- Java 21 + Maven
- Node.js / npm
- Python 3
- Wine (Windows compatibility)

### Android Dev Image ✓
- Android SDK (command-line tools)
- Android 34 system image
- Build tools
- Emulator
- Platform tools (adb)
- Java 21 JDK

---

## Advanced Configuration

### Custom Environment Variables
Edit `docker-compose-3in1.yml`:
```yaml
environment:
  - JAVA_OPTS=-Xmx1024m -Xms512m
  - BACKEND_URL=http://custom-backend:8080
```

### Port Mapping
Change ports in `docker-compose-3in1.yml`:
```yaml
ports:
  - "8888:8080"  # Map 8080 to 8888 locally
```

### Resource Limits
Add to services in `docker-compose-3in1.yml`:
```yaml
deploy:
  resources:
    limits:
      cpus: '2.0'
      memory: 1024M
    reservations:
      cpus: '1.0'
      memory: 512M
```

---

## Integration Tips

### Using Kali to Test Backend
```bash
docker exec -it cloudvmx-kali bash
nmap cloudvmx-backend
curl http://cloudvmx-backend:8080
sqlmap -u "http://cloudvmx-backend:8080/api/..." --dbs
```

### Developing Android App
```bash
# In Android container
docker exec -it cloudvmx-android bash
cd projects/my-app
./gradlew build

# Start emulator
emulator -avd android34 &
adb devices
adb install app.apk
```

### Building Windows Applications
```bash
# In Windows Dev container
docker exec -it cloudvmx-windows bash
cd projects/my-dotnet-app
dotnet build
dotnet run
```

---

## API Reference

### 1. VM Lifecycle & Health Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET`  | `/api/health` | Backend and Docker connection health check |
| `GET`  | `/api/ping` | Ping Docker daemon |
| `GET`  | `/api/ops/status` | Current active VM container and Docker status |
| `POST` | `/api/ops/images/prefetch` | Prefetches default VM image |
| `GET`/`POST` | `/api/vms/{id}/start` | Starts a VM container for specified ID |
| `GET`/`POST` | `/api/vms/{id}/stop` | Stops the running VM container |

### 2. Apache Guacamole HTML5 Gateway Endpoints
| Method | Endpoint | Headers / Parameters | Description |
|--------|----------|----------------------|-------------|
| `POST` | `/api/guacamole/auth` | Body: `{"username": "...", "password": "..."}` | Authenticates user with Apache Guacamole and returns session token |
| `GET`  | `/api/guacamole/connections` | Header: `Guacamole-Token: <token>`<br>Query: `dataSource` (default: postgresql) | Lists all assigned VM connections for authenticated user |
| `POST` | `/api/guacamole/connect` | Body: `{"username": "...", "password": "...", "vmName": "...", "protocol": "rdp", "host": "...", "port": 3389}` | Authenticates, provisions/resolves connection, and returns direct HTML5 client access URL |
| `DELETE` | `/api/guacamole/session` | Header: `Guacamole-Token: <token>` | Terminates and invalidates Guacamole session |

---

## Useful Links

- Docker Documentation: https://docs.docker.com/
- Docker Compose: https://docs.docker.com/compose/
- Kali Linux Documentation: https://www.kali.org/docs/
- Android Developer Guide: https://developer.android.com/
- .NET Documentation: https://learn.microsoft.com/en-us/dotnet/

---

## Support

For issues or questions:
1. Check `docker-compose logs`
2. Verify Docker Desktop status
3. Ensure sufficient disk space
4. Check network connectivity
5. Review Dockerfile contents

---

**Last Updated**: March 11, 2026
**Version**: 1.0
