package ru.mimicsmev.exception;

public class SmevSignatureVerificationException extends Exception {
    public SmevSignatureVerificationException(String s, Throwable t) {
        super(s, t);
    }

    public SmevSignatureVerificationException(String s) {
        super(s);
    }
}
