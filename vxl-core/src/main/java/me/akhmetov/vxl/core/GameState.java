package me.akhmetov.vxl.core;

/**
 * Encapsulates the entirety of the game state, as relevant to both a client and a server.
 */
public class GameState {
    /**
     * Provides access to the map for the game state.
     */
    IGameMap map;
    private ScriptRegistry registy;

    public ScriptRegistry getRegisty() {
        return registy;
    }
}
