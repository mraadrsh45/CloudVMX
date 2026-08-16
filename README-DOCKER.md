# CloudVMX Docker Setup Guide

## Prerequisites

- Docker Desktop installed and running
- Docker Compose (included with Docker Desktop)
- At least 2GB of available memory for containers

## Project Structure

```
cloudvmx/
├── cloudvmx-backend (3)/
│   └── cloudvmx-backend/
│       ├── Dockerfile (multi-stage build)
│       ├── pom.xml
│       └── src/
├── cloudvmx/
│   └── cloudvmx-client/
│       ├── Dockerfile (with X11 libraries)
│       ├── pom.xml
│       └── src/
├── docker-compose.yml
├── .dockerignore
└── build-docker-images.ps1 (for Windows)
```

## Building Docker Images

### Option 1: Using Build Script (Windows)
```powershell
.\build-docker-images.ps1
```

### Option 2: Using Build Script (Linux/macOS)
```bash
chmod +x build-docker-images.sh
./build-docker-images.sh
```

### Option 3: Manual Build
```bash
# Build Backend
docker build -t cloudvmx-backend:latest "cloudvmx-backend (3)/cloudvmx-backend"

# Build Client
docker build -t cloudvmx-client:latest "cloudvmx/cloudvmx-client"
```

## Running with Docker Compose

### Start Services
```bash
docker-compose up -d
```

### Check Service Status
```bash
docker-compose ps
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f cloudvmx-backend
docker-compose logs -f cloudvmx-client
```

### Stop Services
```bash
docker-compose down
```

### Remove Everything (including volumes)
```bash
docker-compose down -v
```

## Services Configuration

### Backend (Spring Boot)
- **Image**: cloudvmx-backend:latest
- **Port**: 8080
- **Healthcheck**: http://localhost:8080/actuator/health
- **Memory**: 512MB max, 256MB initial

### Client (JavaFX)
- **Image**: cloudvmx-client:latest
- **Port**: 3000 (if applicable)
- **Dependencies**: Requires backend to be healthy first
- **Memory**: 512MB max, 256MB initial

## Environment Variables

You can customize the environment by editing `docker-compose.yml`:

**Backend**:
- `JAVA_OPTS`: JVM options (default: `-Xmx512m -Xms256m`)

**Client**:
- `JAVA_OPTS`: JVM options (default: `-Xmx512m -Xms256m`)
- `BACKEND_URL`: Backend API URL (default: `http://cloudvmx-backend:8080`)

## Dockerfile Details

### Backend Dockerfile (Multi-Stage Build)
1. **Build Stage**: Compiles Maven project with JDK 21
2. **Runtime Stage**: Uses minimal Alpine Linux with JRE 21
3. **Exports Port**: 8080 for Spring Boot
4. **Built JAR**: Copied from build stage for fast startup

### Client Dockerfile (Multi-Stage Build)
1. **Build Stage**: Maven build with JDK 21
2. **Runtime Stage**: Alpine Linux with JRE + X11 libraries
3. **X11 Libraries**: Essential for JavaFX GUI support
4. **Libraries Included**: libxext6, libxrender1, libxtst6, libxi6
## API Reference

### 1. VM Lifecycle & Health Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/health` | Backend and Docker connection health check |
| `GET` | `/api/ping` | Ping Docker daemon |
| `GET` | `/api/ops/status` | Current active VM container and Docker status |
| `POST` | `/api/ops/images/prefetch` | Prefetches default VM image |
| `GET`/`POST` | `/api/vms/{id}/start` | Starts a VM container for specified ID |
| `GET`/`POST` | `/api/vms/{id}/stop` | Stops the running VM container |

### 2. Apache Guacamole HTML5 Gateway Endpoints
| Method | Endpoint | Headers / Parameters | Description |
|--------|----------|----------------------|-------------|
| `POST` | `/api/guacamole/auth` | Body: `{"username": "...", "password": "..."}` | Authenticates user with Apache Guacamole and returns session token |
| `GET` | `/api/guacamole/connections` | Header: `Guacamole-Token: <token>`<br>Query: `dataSource` (default: postgresql) | Lists all assigned VM connections for authenticated user |
| `POST` | `/api/guacamole/connect` | Body: `{"username": "...", "password": "...", "vmName": "...", "protocol": "rdp", "host": "...", "port": 3389}` | Authenticates, provisions/resolves connection, and returns direct HTML5 client access URL |
| `DELETE` | `/api/guacamole/session` | Header: `Guacamole-Token: <token>` | Terminates and invalidates Guacamole session |

## Troubleshooting

### Docker Daemon Not Running
**Windows/macOS**: Start Docker Desktop from Applications or use `open -a Docker`

**Linux**: Start Docker service
```bash
sudo systemctl start docker
```

### Port Already in Use
If port 8080 or 3000 is in use, modify `docker-compose.yml`:
```yaml
ports:
  - "8888:8080"  # Map to different port
```

### Build Failures
1. Ensure all source files are in place
2. Check `.dockerignore` doesn't exclude needed files
3. Verify Maven can download dependencies (check internet connection)
4. Clean build if there are cache issues:
```bash
docker-compose down -v
docker system prune -a
docker-compose up --build
```

### Memory Issues
If containers crash due to memory:
1. Increase Docker Desktop memory allocation
2. Reduce JVM memory in `docker-compose.yml`

## Image Sizes

- **cloudvmx-backend**: ~400-500MB (includes Maven artifacts)
- **cloudvmx-client**: ~450-550MB (includes X11 libraries)

## Production Considerations

For production deployment:
1. Use specific version tags instead of `:latest`
2. Implement health checks with retry logic
3. Configure resource limits properly
4. Use .env files for secrets
5. Set up proper logging infrastructure
6. Consider using a private Docker registry

## File Inclusion Verification

All Dockerfiles include:
- ✓ pom.xml (Maven configuration)
- ✓ src/ (all source code)
- ✓ Build artifacts (JAR files)
- ✓ Dependencies (downloaded via Maven)
- ✓ Essential libraries (X11 for client)

No files are excluded except those in `.dockerignore`:
- target/ (rebuilt in container)
- .git (not needed in runtime)
- .idea, .vscode (IDE files)
- *.class, *.log (generated files)
