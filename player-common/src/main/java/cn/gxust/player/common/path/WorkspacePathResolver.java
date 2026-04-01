package cn.gxust.player.common.path;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Resolves configured paths against the current working directory.
 * The configuration remains relative, while the runtime can still work
 * with normalized absolute paths.
 */
public final class WorkspacePathResolver {

    private final Path workingDirectory;

    public WorkspacePathResolver(final Path workingDirectory) {
        this.workingDirectory = Objects.requireNonNull(workingDirectory, "workingDirectory must not be null")
            .toAbsolutePath()
            .normalize();
    }

    public static WorkspacePathResolver fromCurrentDirectory() {
        return new WorkspacePathResolver(Path.of("").toAbsolutePath());
    }

    public Path workingDirectory() {
        return this.workingDirectory;
    }

    public Path resolve(final String configuredPath) {
        if (configuredPath == null || configuredPath.isBlank()) {
            throw new IllegalArgumentException("configuredPath must not be blank");
        }
        return resolve(Path.of(configuredPath));
    }

    public Path resolve(final Path configuredPath) {
        final Path normalizedPath = Objects.requireNonNull(configuredPath, "configuredPath must not be null")
            .normalize();
        return normalizedPath.isAbsolute() ? normalizedPath : this.workingDirectory.resolve(normalizedPath).normalize();
    }
}
