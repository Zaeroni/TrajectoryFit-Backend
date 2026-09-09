package com.trajectoryfit.web;

/** Thrown for semantically invalid requests that bean validation can't express. Maps to HTTP 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
