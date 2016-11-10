package me.akhmetov.vxl.core;

public class ChunkCorruptionException extends Exception {
    public ChunkCorruptionException(String message) {
        super(message);
    }

    public ChunkCorruptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChunkCorruptionException(Throwable cause) {
        super(cause);
    }

    public ChunkCorruptionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ChunkCorruptionException() {
    }
}
