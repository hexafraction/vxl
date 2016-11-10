package me.akhmetov.vxl.core;

public class VxlPluginExecutionException extends Exception{
    public VxlPluginExecutionException() {
    }

    public VxlPluginExecutionException(String message) {
        super(message);
    }

    public VxlPluginExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public VxlPluginExecutionException(Throwable cause) {
        super(cause);
    }

    public VxlPluginExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
