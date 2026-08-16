package com.luxman.cloudvmx_backend.guacamole;

import java.util.List;

public record GuacamoleSession(
        String authToken,
        String username,
        String dataSource,
        List<String> availableDataSources
) {}
