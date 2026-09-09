package com.trajectoryfit.web;

/** Thrown when a requested resource (typically a user) does not exist. Maps to HTTP 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
