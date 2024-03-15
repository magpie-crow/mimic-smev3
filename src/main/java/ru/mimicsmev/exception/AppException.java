package ru.mimicsmev.exception;

public class AppException extends Exception {
    public AppException(String s, Throwable t) {
        super(s, t);
    }

    public AppException(String s) {
        super(s);
    }

    public AppException(Throwable t) {
        super(t);
    }
}
