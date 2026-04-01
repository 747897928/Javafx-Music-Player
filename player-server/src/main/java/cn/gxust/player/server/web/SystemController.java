package cn.gxust.player.server.web;

import cn.gxust.player.common.api.ApiResponse;
import cn.gxust.player.infra.storage.StorageLayout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.time.Instant;

/**
 * Exposes lightweight endpoints used by the desktop client during the rebuild.
 */
@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final StorageLayout storageLayout;
    private final String applicationName;
    private final Instant startedAt = Instant.now();

    public SystemController(
        final StorageLayout storageLayout,
        @Value("${spring.application.name:wizard-music-server}") final String applicationName
    ) {
        this.storageLayout = storageLayout;
        this.applicationName = applicationName;
    }

    @GetMapping("/summary")
    public ApiResponse<SystemSummaryResponse> summary() {
        final Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        final SystemSummaryResponse payload = new SystemSummaryResponse(
            this.applicationName,
            workingDirectory,
            this.storageLayout.rootDirectory(),
            this.storageLayout.databaseFile(),
            this.startedAt
        );
        return ApiResponse.ok("Backend skeleton is running.", payload);
    }
}
