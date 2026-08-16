package com.luxman.cloudvmx_backend.guacamole;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GuacamoleService {

    private final String guacamoleUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GuacamoleService(@Value("${guacamole.url:http://localhost:8080/guacamole}") String guacamoleUrl) {
        this.guacamoleUrl = guacamoleUrl.replaceAll("/+$", "");
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Authenticates user against Apache Guacamole REST API and retrieves a unique session token.
     */
    public GuacamoleSession authenticate(String username, String password) throws Exception {
        String formBody = "username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(guacamoleUrl + "/api/tokens"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Guacamole authentication failed (HTTP " + response.statusCode() + "): " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String token = root.path("authToken").asText();
        String user = root.path("username").asText(username);
        String dataSource = root.path("dataSource").asText("default");

        List<String> dataSources = new ArrayList<>();
        JsonNode available = root.path("availableDataSources");
        if (available.isArray()) {
            available.forEach(node -> dataSources.add(node.asText()));
        }

        return new GuacamoleSession(token, user, dataSource, dataSources);
    }

    /**
     * Retrieves all assigned VM connections for the authenticated session.
     */
    public List<GuacamoleConnection> getAssignedConnections(String token, String dataSource) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(guacamoleUrl + "/api/session/data/" + dataSource + "/connections?token=" + token))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch connections (HTTP " + response.statusCode() + "): " + response.body());
        }

        List<GuacamoleConnection> connections = new ArrayList<>();
        JsonNode root = objectMapper.readTree(response.body());
        root.fields().forEachRemaining(entry -> {
            JsonNode item = entry.getValue();
            String id = item.path("identifier").asText(entry.getKey());
            String name = item.path("name").asText();
            String protocol = item.path("protocol").asText();

            Map<String, String> params = new HashMap<>();
            JsonNode paramsNode = item.path("parameters");
            if (paramsNode.isObject()) {
                paramsNode.fields().forEachRemaining(p -> params.put(p.getKey(), p.getValue().asText()));
            }

            String clientUrl = generateHtml5ClientUrl(token, dataSource, id);
            connections.add(new GuacamoleConnection(id, name, protocol, params, clientUrl));
        });

        return connections;
    }

    /**
     * Creates a new connection in Apache Guacamole for a virtual machine.
     */
    public String createConnection(String token, String dataSource, String name, String protocol, Map<String, String> parameters) throws Exception {
        Map<String, Object> payload = Map.of(
                "name", name,
                "protocol", protocol,
                "parameters", parameters != null ? parameters : Map.of()
        );

        String json = objectMapper.writeValueAsString(payload);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(guacamoleUrl + "/api/session/data/" + dataSource + "/connections?token=" + token))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new RuntimeException("Failed to create connection (HTTP " + response.statusCode() + "): " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        return root.path("identifier").asText();
    }

    /**
     * Builds the seamless HTML5 client URL to open the remote VM session directly in a browser.
     */
    public String generateHtml5ClientUrl(String token, String dataSource, String connectionId) {
        // Guacamole standard client identifier: Base64(connectionId + "\0c\0" + dataSource)
        String rawIdentifier = connectionId + "\0c\0" + dataSource;
        String encodedIdentifier = Base64.getEncoder().encodeToString(rawIdentifier.getBytes(StandardCharsets.UTF_8));
        return guacamoleUrl + "/#/client/" + encodedIdentifier + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    /**
     * Authenticates user, provisions/locates the VM connection, and generates the seamless HTML5 session URL.
     */
    public GuacamoleConnection connectUserToVm(String username, String password, String vmName,
                                              String protocol, String host, int port,
                                              Map<String, String> extraParams) throws Exception {
        GuacamoleSession session = authenticate(username, password);

        // Check if connection already exists
        List<GuacamoleConnection> existing = getAssignedConnections(session.authToken(), session.dataSource());
        Optional<GuacamoleConnection> match = existing.stream()
                .filter(c -> c.name().equalsIgnoreCase(vmName))
                .findFirst();

        String connectionId;
        Map<String, String> params = new HashMap<>(extraParams != null ? extraParams : Map.of());
        params.put("hostname", host);
        params.put("port", String.valueOf(port));

        if (match.isPresent()) {
            connectionId = match.get().identifier();
        } else {
            connectionId = createConnection(session.authToken(), session.dataSource(), vmName, protocol, params);
        }

        String html5Url = generateHtml5ClientUrl(session.authToken(), session.dataSource(), connectionId);
        return new GuacamoleConnection(connectionId, vmName, protocol, params, html5Url);
    }

    /**
     * Invalidates / terminates the unique Guacamole session.
     */
    public void logout(String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(guacamoleUrl + "/api/tokens/" + URLEncoder.encode(token, StandardCharsets.UTF_8)))
                .DELETE()
                .build();

        httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }

    public String getGuacamoleUrl() {
        return guacamoleUrl;
    }
}
