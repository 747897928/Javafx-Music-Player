package com.aquarius.wizard.player.common.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Lightweight Jackson helper shared by desktop and server modules.
 */
public final class JacksonUtils {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .addModule(new JavaTimeModule())
        .build();

    private JacksonUtils() {
        throw new UnsupportedOperationException("JacksonUtils must not be instantiated");
    }

    public static ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }

    public static JsonNode readTree(final String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to parse JSON content.", exception);
        }
    }

    public static <T> T readValue(final String json, final Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to parse JSON into " + type.getName() + ".", exception);
        }
    }

    public static <T> T readValue(final String json, final TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to parse JSON content.", exception);
        }
    }

    public static String writeValue(final Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to serialize object to JSON.", exception);
        }
    }

    public static String writePrettyValue(final Object value) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to serialize object to pretty JSON.", exception);
        }
    }
}
