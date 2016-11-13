package me.akhmetov.vxl.core;

import me.akhmetov.vxl.core.security.SecurityUtils;

class ChunkCorruptionException extends Exception {
    public ChunkCorruptionException(String message) {
        super(message);
        SecurityUtils.checkConstructException();

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
