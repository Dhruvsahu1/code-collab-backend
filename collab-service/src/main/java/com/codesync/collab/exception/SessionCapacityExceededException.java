package com.codesync.collab.exception;
 
public class SessionCapacityExceededException extends RuntimeException {
    public SessionCapacityExceededException(String message) {
        super(message);
    }
}
 