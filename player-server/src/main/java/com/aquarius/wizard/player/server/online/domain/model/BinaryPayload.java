package com.aquarius.wizard.player.server.online.domain.model;

/**
 * Binary payload plus media type metadata.
 */
public record BinaryPayload(byte[] bytes, String mediaType) {
}
