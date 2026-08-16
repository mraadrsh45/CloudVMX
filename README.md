# CloudVMX 🚀

**CloudVMX** is a unified cloud virtualization and remote desktop management platform. It provides a modular backend API, a JavaFX desktop client GUI, integrated remote access solutions (Apache Guacamole & RustDesk), and containerized development/testing environments (Kali Linux, Windows Dev, Android Dev).

---

## 🏗️ Architecture & Components

```
CloudVMX/
├── cloudvmx-backend (3)/
│   └── cloudvmx-backend/          # Spring Boot REST API & VM Management Service
│       ├── src/main/java/         # VM Controllers, Guacamole tunnels & session handlers
│       └── pom.xml
├── cloudvmx/
│   └── cloudvmx-client/           # JavaFX Desktop Client GUI
│       ├── src/main/java/         # Client UI Controllers & Application entrypoint
│       └── pom.xml
├── cloudvmx-rustdesk/             # RustDesk Remote Server Hub (HBBS / HBBR)
│   ├── docker-compose.yml
│   └── start-hub.bat
├── Dockerfile.android             # Android SDK / Emulator environment container
├── Dockerfile.kali                # Kali Linux security & penetration testing container
├── Dockerfile.windows             # Windows / Wine / .NET development container
├── docker-compose-3in1.yml        # Orchestration for all 5 services
└── docker-compose.yml             # Base container orchestration
```

---

## ✨ Features

- 🖥️ **JavaFX Client GUI**: Modern desktop user interface for initiating, viewing, and managing remote machine sessions.
- ⚙️ **Spring Boot Backend**: RESTful API for lifecycle management of virtual instances, session allocation, and remote connection proxying.
- 🌐 **Multi-Protocol Remote Access**:
  - **Apache Guacamole Integration**: Web-native RDP/VNC/SSH protocol translation and WebSocket-ready streaming.
  - **RustDesk Hub**: Self-hosted relay and rendezvous server for ultra-low latency desktop streaming.
- 🐳 **3-in-1 Containerized Workspaces**:
  - **Kali Linux**: Security, penetration testing, and network analysis tools.
  - **Windows Dev**: .NET 8.0 SDK, Wine compatibility layer, Node.js, and Java tooling.
  - **Android Dev**: Android SDK, ADB platform-tools, and emulator support.

---

## 📋 Prerequisites

- **Java JDK 21+** (for building backend and client)
- **Maven 3.8+**
- **Docker Desktop** (with Docker Compose support)
- **Git**

---

## 🚀 Quick Start

### 1. Backend Service (Spring Boot)

Run the backend REST API locally:

```bash
cd "cloudvmx-backend (3)/cloudvmx-backend"
./mvnw spring-boot:run
```
*The REST API will be available at `http://localhost:8080`.*

### 2. Client Application (JavaFX)

Launch the JavaFX GUI client:

```bash
cd cloudvmx/cloudvmx-client
./mvnw javafx:run
```

### 3. RustDesk Remote Hub

Start the RustDesk rendezvous (`hbbs`) and relay (`hbbr`) servers:

```bash
cd cloudvmx-rustdesk
docker compose up -d
```

### 4. 3-in-1 Docker Development Environments

To build and start all containerized environments (Backend, Frontend, Kali, Windows, Android):

**On Windows:**
```cmd
build-3in1-docker.bat
```

**On Linux/macOS:**
```bash
chmod +x build-3in1-docker.sh
./build-3in1-docker.sh
```

Or using Docker Compose directly:
```bash
docker compose -f docker-compose-3in1.yml up -d
```

---

## 📡 API Endpoints Overview

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/vm/status` | Get current VM status and metrics |
| `POST` | `/api/vm/start` | Start target virtual machine |
| `POST` | `/api/vm/stop` | Gracefully stop virtual machine |
| `POST` | `/api/guacamole/connect` | Establish an Apache Guacamole remote session |
| `DELETE` | `/api/guacamole/disconnect` | Terminate active Guacamole connection session |

---

## 📖 Documentation

- [Docker 3-in-1 Setup Guide](README-DOCKER-3IN1.md)
- [Base Docker Setup](README-DOCKER.md)
- [Integration Fixes & Troubleshooting](INTEGRATION-FIX.md)

## 👤 Author & Maintainer

- **Luxman Kumar** - [@mraadrsh45](https://github.com/mraadrsh45)
- **Email**: [luxmankumar628@gmail.com](mailto:luxmankumar628@gmail.com)

---

## 📄 License

This project is licensed under the MIT License - see the repository details for more information.

