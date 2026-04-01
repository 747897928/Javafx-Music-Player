package cn.gxust.player.server.config;

import cn.gxust.player.common.path.WorkspacePathResolver;
import cn.gxust.player.infra.storage.StorageLayout;
import cn.gxust.player.infra.storage.StorageLayoutResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Registers resolved storage layout as a singleton bean.
 */
@Configuration(proxyBeanMethods = false)
public class StorageConfiguration {

    @Bean
    StorageLayoutResolver storageLayoutResolver() {
        return new StorageLayoutResolver(WorkspacePathResolver.fromCurrentDirectory());
    }

    @Bean
    StorageLayout storageLayout(
        final StorageLayoutResolver storageLayoutResolver,
        final PlayerStorageProperties storageProperties
    ) throws IOException {
        final StorageLayout storageLayout = storageLayoutResolver.resolve(
            storageProperties.getRoot(),
            storageProperties.getDatabaseFileName(),
            storageProperties.getMusicDirectory(),
            storageProperties.getLyricsDirectory(),
            storageProperties.getCoversDirectory(),
            storageProperties.getCacheDirectory()
        );
        storageLayoutResolver.prepareDirectories(storageLayout);
        return storageLayout;
    }
}
