package ru.mimicsmev.exception;

public class SmevInvalidContentException extends Exception {
    public SmevInvalidContentException(String s, Throwable t) {
        super(s, t);
    }

    public SmevInvalidContentException(String s) {
        super(s);
    }
}
