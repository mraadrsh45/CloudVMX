package com.luxman.cloudvmx_backend.guacamole;

import java.util.Map;

public record GuacamoleConnection(
        String identifier,
        String name,
        String protocol,
        Map<String, String> parameters,
        String html5ClientUrl
) {}
