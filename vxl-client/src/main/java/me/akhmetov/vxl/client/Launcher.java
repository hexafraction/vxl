package me.akhmetov.vxl.client;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class Launcher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(1000,525);

        config.setIdleFPS(20);
        config.setTitle("TEST");
        config.setResizable( true);
        //config.samples = 4;
        config.useVsync(false);
        config.setHdpiMode(Lwjgl3ApplicationConfiguration.HdpiMode.Logical);
        //System.loadLibrary("nvapi");
        new Lwjgl3Application(new GameMain(), config);

    }
}
