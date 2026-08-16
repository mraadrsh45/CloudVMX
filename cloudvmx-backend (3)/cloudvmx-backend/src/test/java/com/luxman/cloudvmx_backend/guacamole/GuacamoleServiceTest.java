package com.luxman.cloudvmx_backend.guacamole;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GuacamoleServiceTest {

    private final GuacamoleService service = new GuacamoleService("http://localhost:8080/guacamole");

    @Test
    void testGenerateHtml5ClientUrl() {
        String token = "dummy-token-12345";
        String dataSource = "postgresql";
        String connectionId = "42";

        String url = service.generateHtml5ClientUrl(token, dataSource, connectionId);

        String expectedEncoded = Base64.getEncoder().encodeToString(("42\0c\0postgresql").getBytes(StandardCharsets.UTF_8));
        String expectedUrl = "http://localhost:8080/guacamole/#/client/" + expectedEncoded + "?token=dummy-token-12345";

        assertEquals(expectedUrl, url);
    }

    @Test
    void testGuacamoleSessionRecord() {
        GuacamoleSession session = new GuacamoleSession("tokenABC", "user1", "mysql", List.of("mysql", "postgresql"));
        assertEquals("tokenABC", session.authToken());
        assertEquals("user1", session.username());
        assertEquals("mysql", session.dataSource());
        assertEquals(2, session.availableDataSources().size());
    }

    @Test
    void testGuacamoleConnectionRecord() {
        GuacamoleConnection conn = new GuacamoleConnection("1", "WinVM", "rdp", Map.of("hostname", "10.0.0.5"), "http://localhost:8080/guacamole/#/client/xxx");
        assertEquals("1", conn.identifier());
        assertEquals("WinVM", conn.name());
        assertEquals("rdp", conn.protocol());
        assertEquals("10.0.0.5", conn.parameters().get("hostname"));
    }
}
