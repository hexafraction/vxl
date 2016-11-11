package me.akhmetov.vxl.core;

import me.akhmetov.vxl.core.security.VxlPermission;

import java.util.HashSet;
import java.util.Set;

public final class Logging {
    private Logging() {
    }

    enum Severity {
        FATAL, ERROR, WARNING, INFO, DEBUG
    }

    interface ILogger {

        void log(Severity sev, String message);
    }
    private static final Set<ILogger> loggers = new HashSet<>();
    public static void registerLogger(ILogger logger){
        System.getSecurityManager().checkPermission(new VxlPermission("logging"));
        loggers.add(logger);
    }
    public static void log(Severity sv, String message){
        for(ILogger log : loggers){
            log.log(sv, message);
        }
    }


}
