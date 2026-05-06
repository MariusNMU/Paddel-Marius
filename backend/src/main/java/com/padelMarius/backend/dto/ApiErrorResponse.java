package com.padelMarius.backend.dto;

public record ApiErrorResponse(
        String code,
        String message
) {
}
