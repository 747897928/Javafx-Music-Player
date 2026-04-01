package cn.gxust.player.common.api;

import java.time.Instant;

/**
 * Simple API response envelope shared by desktop and server modules.
 *
 * @param success whether the request completed successfully
 * @param message human-readable status message
 * @param data    response payload
 * @param time    response timestamp in UTC
 * @param <T>     payload type
 */
public record ApiResponse<T>(boolean success, String message, T data, Instant time) {

    /**
     * Builds a successful response payload.
     *
     * @param message status message
     * @param data    payload body
     * @param <T>     payload type
     * @return successful response wrapper
     */
    public static <T> ApiResponse<T> ok(final String message, final T data) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }

    /**
     * Builds a failure response payload.
     *
     * @param message status message
     * @return failed response wrapper without body
     */
    public static ApiResponse<Void> failure(final String message) {
        return new ApiResponse<>(false, message, null, Instant.now());
    }
}
