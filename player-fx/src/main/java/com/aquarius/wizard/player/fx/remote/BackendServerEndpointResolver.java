package com.aquarius.wizard.player.fx.remote;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves the backend server base URL from runtime overrides and external config.
 */
public final class BackendServerEndpointResolver {

    static final String DEFAULT_SERVER_BASE_URL = "http://127.0.0.1:18080";
    static final String SERVER_BASE_URL_PROPERTY = "wizard.player.server.base-url";
    static final String SERVER_BASE_URL_ENV = "WIZARD_PLAYER_SERVER_BASE_URL";
    private static final String CONFIG_FILE_NAME = "player-fx.ini";
    private static final String CONFIG_FILE_PATH_PROPERTY = "wizard.player.config-file";

    private BackendServerEndpointResolver() {
    }

    public static String resolveBaseUrl() {
        final String propertyValue = System.getProperty(SERVER_BASE_URL_PROPERTY);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return normalizeBaseUrl(propertyValue);
        }

        final String environmentValue = System.getenv(SERVER_BASE_URL_ENV);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return normalizeBaseUrl(environmentValue);
        }

        for (final Path configPath : resolveConfigCandidates()) {
            final Optional<String> configuredBaseUrl = readBaseUrl(configPath);
            if (configuredBaseUrl.isPresent()) {
                return normalizeBaseUrl(configuredBaseUrl.get());
            }
        }

        return DEFAULT_SERVER_BASE_URL;
    }

    private static List<Path> resolveConfigCandidates() {
        final Set<Path> candidates = new LinkedHashSet<>();

        final String explicitConfigPath = System.getProperty(CONFIG_FILE_PATH_PROPERTY);
        if (explicitConfigPath != null && !explicitConfigPath.isBlank()) {
            candidates.add(Path.of(explicitConfigPath).toAbsolutePath().normalize());
        }

        final Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        candidates.add(workingDirectory.resolve("config").resolve(CONFIG_FILE_NAME));

        final Optional<Path> runtimeHome = resolveRuntimeHome();
        runtimeHome.ifPresent(home -> {
            candidates.add(home.resolve("config").resolve(CONFIG_FILE_NAME));
            candidates.add(home.resolve(CONFIG_FILE_NAME));
            final Path parent = home.getParent();
            if (parent != null) {
                candidates.add(parent.resolve("config").resolve(CONFIG_FILE_NAME));
            }
        });

        return List.copyOf(candidates);
    }

    private static Optional<Path> resolveRuntimeHome() {
        final String jpackageAppPath = System.getProperty("jpackage.app-path");
        if (jpackageAppPath != null && !jpackageAppPath.isBlank()) {
            final Path launcherPath = Path.of(jpackageAppPath).toAbsolutePath().normalize();
            return Optional.of(Files.isDirectory(launcherPath) ? launcherPath : launcherPath.getParent());
        }

        try {
            final CodeSource codeSource = BackendServerEndpointResolver.class.getProtectionDomain().getCodeSource();
            if (codeSource == null) {
                return Optional.empty();
            }
            final URL location = codeSource.getLocation();
            if (location == null) {
                return Optional.empty();
            }
            final Path codePath = Path.of(location.toURI()).toAbsolutePath().normalize();
            if (Files.isDirectory(codePath)) {
                return Optional.of(codePath);
            }
            return Optional.ofNullable(codePath.getParent());
        } catch (URISyntaxException | IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private static Optional<String> readBaseUrl(final Path configPath) {
        if (configPath == null || !Files.isRegularFile(configPath)) {
            return Optional.empty();
        }
        try {
            for (final String row : Files.readAllLines(configPath, StandardCharsets.UTF_8)) {
                final String line = row.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith(";") || line.startsWith("[")) {
                    continue;
                }
                final int separatorIndex = line.indexOf('=');
                if (separatorIndex <= 0) {
                    continue;
                }
                final String key = line.substring(0, separatorIndex).trim();
                final String value = line.substring(separatorIndex + 1).trim();
                if (value.isBlank()) {
                    continue;
                }
                if ("server.base-url".equalsIgnoreCase(key) || "base-url".equalsIgnoreCase(key)) {
                    return Optional.of(value);
                }
            }
        } catch (Exception ignored) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    private static String normalizeBaseUrl(final String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return DEFAULT_SERVER_BASE_URL;
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
