package me.akhmetov.vxl.core;

import me.akhmetov.vxl.core.security.VxlPermission;

/**
 * Encapsulates the entirety of the game state, as relevant to both a client and a server.
 */
public class GameState {
    /**
     * Provides access to the map for the game state.
     */
    private final IGameMap map;
    private final ScriptRegistry registry;

    public GameState(IGameMap map, ScriptRegistry registry, boolean isAuthoritative) {
        System.getSecurityManager().checkPermission(new VxlPermission("corestate"));
        this.map = map;
        this.registry = registry;
        this.isAuthoritative = isAuthoritative;
    }

    public IGameMap getMap() {
        return map;
    }

    public ScriptRegistry getRegistry() {
        return registry;
    }
    private final boolean isAuthoritative;

    public boolean isAuthoritative() {
        return isAuthoritative;
    }
}
