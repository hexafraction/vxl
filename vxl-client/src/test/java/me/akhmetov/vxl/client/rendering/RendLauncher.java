package me.akhmetov.vxl.client.rendering;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class RendLauncher {
    public static void main(String[] arg) throws Exception {
        ((LoggerContext) LogManager.getContext()).getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.WARN);
        ((LoggerContext) LogManager.getContext()).updateLoggers();
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(1000,525);

        config.setIdleFPS(20);
        config.setTitle("TEST");
        config.setResizable( true);
        //config.samples = 4;
        config.useVsync(false);
        config.setHdpiMode(Lwjgl3ApplicationConfiguration.HdpiMode.Logical);
        config.enableGLDebugOutput(true, System.out);
        //config.setBackBufferConfig(8,8,8,8,16,4,3);
        //System.loadLibrary("nvapi");
        new Lwjgl3Application(new GameRenderer(), config);
    }
}
