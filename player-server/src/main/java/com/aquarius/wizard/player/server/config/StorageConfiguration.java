package com.aquarius.wizard.player.server.config;

import com.aquarius.wizard.player.common.path.WorkspacePathResolver;
import com.aquarius.wizard.player.infra.storage.StorageLayout;
import com.aquarius.wizard.player.infra.storage.StorageLayoutResolver;
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

