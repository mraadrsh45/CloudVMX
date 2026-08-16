package com.luxman.cloudvmx_backend.guacamole;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/guacamole")
@CrossOrigin(origins = "*")
public class GuacamoleController {

    private final GuacamoleService guacamoleService;

    public GuacamoleController(GuacamoleService guacamoleService) {
        this.guacamoleService = guacamoleService;
    }

    /**
     * Authenticate against Apache Guacamole and get session token.
     */
    @PostMapping("/auth")
    public ResponseEntity<?> authenticate(@RequestBody Map<String, String> creds) {
        try {
            String username = creds.get("username");
            String password = creds.get("password");
            if (username == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "username and password are required"));
            }
            GuacamoleSession session = guacamoleService.authenticate(username, password);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * List all assigned connections for user's session.
     */
    @GetMapping("/connections")
    public ResponseEntity<?> getConnections(@RequestHeader("Guacamole-Token") String token,
                                            @RequestParam(defaultValue = "postgresql") String dataSource) {
        try {
            List<GuacamoleConnection> list = guacamoleService.getAssignedConnections(token, dataSource);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Seamlessly connect user to a VM and retrieve HTML5 client session URL.
     */
    @PostMapping("/connect")
    public ResponseEntity<?> connectToVm(@RequestBody Map<String, Object> req) {
        try {
            String username = (String) req.get("username");
            String password = (String) req.get("password");
            String vmName = (String) req.getOrDefault("vmName", "CloudVM");
            String protocol = (String) req.getOrDefault("protocol", "rdp");
            String host = (String) req.getOrDefault("host", "localhost");
            int port = req.containsKey("port") ? Integer.parseInt(req.get("port").toString()) : (protocol.equalsIgnoreCase("rdp") ? 3389 : 5900);

            @SuppressWarnings("unchecked")
            Map<String, String> extraParams = (Map<String, String>) req.get("parameters");

            GuacamoleConnection connection = guacamoleService.connectUserToVm(
                    username, password, vmName, protocol, host, port, extraParams
            );
            return ResponseEntity.ok(connection);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * End user Guacamole session.
     */
    @DeleteMapping("/session")
    public ResponseEntity<?> logout(@RequestHeader("Guacamole-Token") String token) {
        try {
            guacamoleService.logout(token);
            return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Session terminated"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
