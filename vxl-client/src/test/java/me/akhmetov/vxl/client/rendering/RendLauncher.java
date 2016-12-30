package me.akhmetov.vxl.client.rendering;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class RendLauncher {
    public static void main(String[] arg) throws Exception {
        ((LoggerContext) LogManager.getContext()).getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.WARN);
        ((LoggerContext) LogManager.getContext()).updateLoggers();
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 1000;
        config.height = 525;
        config.backgroundFPS = 20;
        config.title = "TEST";
        config.vSyncEnabled = false;
        config.foregroundFPS = 120;
        config.fullscreen = false;
        new LwjglApplication(new GameRenderer(), config);
    }
}
