package me.akhmetov.vxl.core;

import me.akhmetov.vxl.api.NodeAPI;

/**
 * Encapsulates the entirety of the game state, as relevant to both a client and a server.
 */
class GameState {
    /**
     * Provides access to the map for the game state.
     */
    private final IGameMap map;
    private final NodeAPI nodeAPI;
    public GameState(IGameMap map, NodeAPI nodeAPI, boolean isAuthoritative) {
        this.map = map;
        this.nodeAPI = nodeAPI;
        this.isAuthoritative = isAuthoritative;
    }

    public IGameMap getMap() {
        return map;
    }

    public NodeAPI getNodeAPI() {
        return nodeAPI;
    }
    private final boolean isAuthoritative;

    public boolean isAuthoritative() {
        return isAuthoritative;
    }
}
