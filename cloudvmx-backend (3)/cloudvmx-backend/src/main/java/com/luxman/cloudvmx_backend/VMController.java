package com.luxman.cloudvmx_backend;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/vms", "/api"})
@CrossOrigin(origins = "*")
public class VMController {

    private static String activeContainerId = null;
    private DockerClient dockerClient = null;

    @PostConstruct
    public void initializeDocker() {
        try {
            // Auto-detect Docker host based on OS
            String dockerHost = System.getenv("DOCKER_HOST");
            if (dockerHost == null) {
                // Windows: use npipe
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    dockerHost = "npipe:////./pipe/docker_engine";
                } else {
                    // Unix: use socket
                    dockerHost = "unix:///var/run/docker.sock";
                }
            }

            System.out.println("[CloudVMX] Initializing Docker Client with: " + dockerHost);

            DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost(dockerHost)
                    .build();

            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .maxConnections(100)
                    .connectionTimeout(Duration.ofSeconds(30))
                    .responseTimeout(Duration.ofSeconds(45))
                    .build();

            this.dockerClient = DockerClientBuilder.getInstance(config)
                    .withDockerHttpClient(httpClient)
                    .build();

            // Test connection
            this.dockerClient.pingCmd().exec();
            System.out.println("[CloudVMX] Docker connection successful!");

        } catch (Exception e) {
            System.err.println("[CloudVMX] Failed to initialize Docker: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        if (dockerClient != null) {
            response.put("status", "UP");
            response.put("docker", "connected");
        } else {
            response.put("status", "DEGRADED");
            response.put("docker", "not connected");
        }
        return ResponseEntity.ok(response);
    }

    // VM Management endpoint
    @RequestMapping(value = "/{id}/{action}", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, String>> manageVm(@PathVariable String id, @PathVariable String action) {
        Map<String, String> response = new HashMap<>();

        if (dockerClient == null) {
            response.put("status", "ERROR");
            response.put("message", "Docker client not initialized");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }

        System.out.println("[CloudVMX] Request: " + action + " for " + id);

        try {
            if (action.equals("start")) {
                String osType = "linux";
                try {
                    osType = dockerClient.infoCmd().exec().getOsType();
                } catch (Exception ignored) {}

                String image = osType != null && osType.toLowerCase().contains("windows")
                        ? "mcr.microsoft.com/windows/servercore:ltsc2022"
                        : "alpine:latest";
                List<String> cmd = osType != null && osType.toLowerCase().contains("windows")
                        ? List.of("cmd", "/c", "timeout", "/t", "3600")
                        : List.of("sleep", "3600");

                boolean hasImage = !dockerClient.listImagesCmd().withImageNameFilter(image).exec().isEmpty();
                if (!hasImage) {
                    dockerClient.pullImageCmd(image).start().awaitCompletion();
                }

                CreateContainerResponse container = dockerClient.createContainerCmd(image)
                        .withName("CloudVMX_" + id + "_" + System.currentTimeMillis())
                        .withCmd(cmd.toArray(new String[0]))
                        .exec();

                dockerClient.startContainerCmd(container.getId()).exec();
                activeContainerId = container.getId();

                System.out.println("[CloudVMX] VM Started: " + container.getId());
                response.put("status", "SUCCESS");
                response.put("message", "VM Started");
                response.put("containerId", container.getId());
                return ResponseEntity.ok(response);

            } else if (action.equals("stop")) {
                if (activeContainerId == null) {
                    response.put("status", "ERROR");
                    response.put("message", "No VM is running");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

                System.out.println("[CloudVMX] Stopping: " + activeContainerId);
                dockerClient.stopContainerCmd(activeContainerId).exec();
                activeContainerId = null;

                response.put("status", "SUCCESS");
                response.put("message", "VM Stopped");
                return ResponseEntity.ok(response);
            }

            response.put("status", "ERROR");
            response.put("message", "Invalid action");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            System.err.println("[CloudVMX] Error: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        if (dockerClient == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Docker not connected");
        }

        try {
            dockerClient.pingCmd().exec();
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            System.err.println("[CloudVMX] Ping failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("ERROR: " + e.getMessage());
        }
    }

    @PostMapping("/ops/images/prefetch")
    public ResponseEntity<String> prefetch() {
        if (dockerClient == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Docker not connected");
        }

        try {
            String osType = "linux";
            try {
                osType = dockerClient.infoCmd().exec().getOsType();
            } catch (Exception ignored) {}

            String image = osType != null && osType.toLowerCase().contains("windows")
                    ? "mcr.microsoft.com/windows/servercore:ltsc2022"
                    : "alpine:latest";

            boolean hasImage = !dockerClient.listImagesCmd().withImageNameFilter(image).exec().isEmpty();
            if (!hasImage) {
                dockerClient.pullImageCmd(image).start().awaitCompletion();
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            System.err.println("[CloudVMX] Prefetch failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERROR: " + e.getMessage());
        }
    }

    @GetMapping("/ops/status")
    public ResponseEntity<Map<String, String>> status() {
        Map<String, String> response = new HashMap<>();
        response.put("containerId", activeContainerId == null ? "none" : activeContainerId);
        response.put("docker", dockerClient == null ? "disconnected" : "connected");
        return ResponseEntity.ok(response);
    }
}
