package me.akhmetov.vxl.api;

public interface LoggingManager {

    public enum Severity {
        FATAL, ERROR, WARNING, INFO, DEBUG
    }

    public interface ILogger {

        void log(Severity sev, String message);
    }


}
