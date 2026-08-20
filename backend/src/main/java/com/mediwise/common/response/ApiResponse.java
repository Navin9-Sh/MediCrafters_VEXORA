package com.mediwise.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final String code;
    private final String correlationId;
    private final Object errors;

    @Builder.Default
    private final Instant timestamp = Instant.now();

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    public static ApiResponse<Void> message(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .build();
    }

    public static ApiResponse<Void> error(String code, String message, String correlationId) {
        return ApiResponse.<Void>builder()
                .success(false)
                .code(code)
                .message(message)
                .correlationId(correlationId)
                .build();
    }

    public static ApiResponse<Void> error(String code, String message, String correlationId, Object errors) {
        return ApiResponse.<Void>builder()
                .success(false)
                .code(code)
                .message(message)
                .correlationId(correlationId)
                .errors(errors)
                .build();
    }
}
