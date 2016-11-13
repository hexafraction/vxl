package me.akhmetov.vxl.core;

import me.akhmetov.vxl.api.LoggingManager;
import me.akhmetov.vxl.core.security.VxlPermission;

import java.util.HashSet;
import java.util.Set;

class SystemLogger implements LoggingManager {
    private final Set<LoggingManager.ILogger> loggers = new HashSet<>();
    private static volatile SystemLogger instance;

    static SystemLogger getInstance() {
        if(instance==null){
            synchronized(SystemLogger.class){
                if(instance==null){
                    instance = new SystemLogger();
                }
            }
        }
        return instance;
    }

    public void registerLogger(LoggingManager.ILogger logger){
        System.getSecurityManager().checkPermission(new VxlPermission("logging"));
        loggers.add(logger);
    }
    public void log(LoggingManager.Severity sv, String message){
        for(LoggingManager.ILogger logger : loggers){
            logger.log(sv, message);
        }
    }


}
