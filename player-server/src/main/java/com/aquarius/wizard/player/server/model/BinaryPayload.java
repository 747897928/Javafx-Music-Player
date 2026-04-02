package com.aquarius.wizard.player.server.model;

/**
 * Binary payload plus media type metadata.
 */
public record BinaryPayload(byte[] bytes, String mediaType) {
}

