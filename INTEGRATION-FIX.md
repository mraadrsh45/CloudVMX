# Complete CloudVMX Setup - Full Stack Integration Guide

## Problem: Frontend Working But Backend Not Integrated

The issue occurs when:
- Frontend (JavaFX GUI) displays but shows "Status: Offline"
- Backend is either not running or not reachable
- API endpoints are not responding

## Fixed Issues

### 1. Backend Docker Configuration
**Problem**: Backend was trying to connect to `tcp://localhost:2375` which isn't standard
**Solution**: Updated VMController to auto-detect Docker socket and use:
- Windows: `npipe:////./pipe/docker_engine`
- Linux: `unix:///var/run/docker.sock`

### 2. Frontend-Backend Communication
**Problem**: Frontend hardcoded to `localhost:8080` but couldn't reach backend
**Solution**: 
- Updated HelloController to use proper error handling
- Added `/health` endpoint for better status checking
- Fixed thread-based backend initialization

### 3. API Response Format
**Problem**: Backend returning plain strings instead of JSON
**Solution**: Updated all endpoints to return JSON responses:
```java
ResponseEntity<Map<String, String>>
```

---

## Quick Start (Windows)

### Option 1: Automated Integration Script
```powershell
cd c:\Users\luxma\Desktop\cloudvmx
.\setup-integration.bat
```

### Option 2: Manual Commands
```powershell
# Build both images
docker build -t cloudvmx-backend:latest "cloudvmx-backend (3)\cloudvmx-backend"
docker build -t cloudvmx-client:latest "cloudvmx\cloudvmx-client"

# Create network
docker network create cloudvmx-net

# Start Backend
docker run -d --name cloudvmx-backend --network cloudvmx-net -p 8080:8080 cloudvmx-backend:latest

# Start Frontend (after backend is ready)
docker run -d --name cloudvmx-client --network cloudvmx-net -p 3000:3000 cloudvmx-client:latest
```

---

## Verify Integration

### Check Backend is Running
```powershell
# Test API endpoint
curl http://localhost:8080/api/health

# View backend logs
docker logs -f cloudvmx-backend
```

### Check Frontend Status
```powershell
# View frontend logs
docker logs -f cloudvmx-client
```

### Expected Output

**Frontend Status**: Should show "Status: Connected" in GUI  
**Backend Logs**: Should show Docker client initialization messages  
**API Response**: Should return JSON:
```json
{
  "status": "UP",
  "docker": "connected"
}
```

---

## Code Changes Made

### 1. VMController.java (Backend)
- ✓ Auto-detect Docker socket (Windows/Linux)
- ✓ Better error handling with null checks
- ✓ Return JSON responses instead of strings
- ✓ Added `/health` endpoint
- ✓ Improved logging with `[CloudVMX]` prefix

### 2. HelloController.java (Frontend)
- ✓ Use `/health` endpoint for connection check
- ✓ Better exception handling
- ✓ Thread-based backend check (non-blocking)
- ✓ Proper UI updates on main thread
- ✓ Color-coded status (GREEN = Connected, RED = Offline)

### 3. application.properties (Backend Config)
- ✓ Added server port configuration
- ✓ Added CORS settings
- ✓ Added logging configuration
- ✓ Added Docker host environment variable support

---

## Troubleshooting

### Frontend Still Shows "Status: Offline"

**Check 1**: Backend is running
```powershell
docker ps | findstr cloudvmx-backend
```

**Check 2**: Backend logs
```powershell
docker logs cloudvmx-backend
```
Look for: `[CloudVMX] Docker connection successful!`

**Check 3**: API is responding
```powershell
curl http://localhost:8080/api/health
```

### Backend Won't Connect to Docker

**Solution**: Ensure Docker Desktop is fully running
```powershell
# Restart Docker Desktop
Stop-Process -Name "Docker Desktop" -Force
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
```

### Port 8080 Already in Use

**Solution**: Change backend port in setup script or use different port:
```powershell
docker run -d --name cloudvmx-backend -p 8888:8080 cloudvmx-backend:latest
```

### Network Connection Issues

**Solution**: Verify containers are on same network
```powershell
docker network ls
docker network inspect cloudvmx-net
```

---

## File Modifications Summary

| File | Changes |
|------|---------|
| VMController.java | Auto-detect Docker socket, JSON responses, null checks |
| HelloController.java | Thread-based health check, better error handling |
| application.properties | Server config, CORS, logging |

---

## Network Architecture

```
┌─────────────────────────────────┐
│   Local Host Machine             │
├─────────────────────────────────┤
│  Frontend (Port 3000)           │
│  ↓                               │
│  Backend API (Port 8080)        │
│  ↓                               │
│  Docker Daemon                  │
│  ↓                               │
│  VM Containers                  │
└─────────────────────────────────┘
```

All containers connected via `cloudvmx-net` bridge network.

---

## API Endpoints (Now Available)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/health` | GET | Check backend connection |
| `/api/ping` | GET | Ping Docker daemon |
| `/api/{id}/start` | GET/POST | Start VM |
| `/api/{id}/stop` | GET/POST | Stop VM |
| `/api/ops/status` | GET | Get container status |
| `/api/ops/images/prefetch` | POST | Pull OS images |

---

## Next Steps

1. **Run the integration script**:
   ```powershell
   .\setup-integration.bat
   ```

2. **Wait for both services to start** (30-45 seconds)

3. **Open Frontend**: Frontend UI should show "Status: Connected"

4. **Try VM Operations**: Click Start/Stop buttons to manage containers

5. **Check Logs**: View logs if anything fails

---

## Support Commands

```powershell
# View all running containers
docker ps

# Stop both services
docker stop cloudvmx-backend cloudvmx-client

# Remove both containers
docker rm cloudvmx-backend cloudvmx-client

# View network
docker network inspect cloudvmx-net

# Full cleanup
docker ps -a | findstr cloudvmx | ForEach-Object { docker rm -f $_.Split()[0] }
```

---

**Last Updated**: March 11, 2026  
**Status**: Backend and Frontend Integration Fixed ✓
