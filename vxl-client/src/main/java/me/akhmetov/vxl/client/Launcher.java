package me.akhmetov.vxl.client;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Launcher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 1000;
        config.height = 525;
        config.backgroundFPS = 20;
        config.title = "TEST";
        config.fullscreen = false;
        new LwjglApplication(new GameMain(), config);
    }
}
