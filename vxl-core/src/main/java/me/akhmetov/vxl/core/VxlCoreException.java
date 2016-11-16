package me.akhmetov.vxl.core;

public class VxlCoreException extends Exception {
    public VxlCoreException() {
    }

    public VxlCoreException(String message) {
        super(message);
    }

    public VxlCoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public VxlCoreException(Throwable cause) {
        super(cause);
    }

    public VxlCoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
