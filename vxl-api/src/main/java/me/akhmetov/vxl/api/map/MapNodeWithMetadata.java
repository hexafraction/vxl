package me.akhmetov.vxl.api.map;

public abstract class MapNodeWithMetadata extends MapNode {
    public MapNodeWithMetadata(String name) {
        super(name);
    }

    /**
     * Persists the state of this node to metadata that will be serialized to disk. Note that this method cannot simply
     * return the {@link MapNodeWithMetadata} instance itself, since it may not be serialized due to security restrictions.
     * @return The serializable object form of this node.
     */
    public abstract Object storeToMetadata();

    public abstract String getDecoderId();
}