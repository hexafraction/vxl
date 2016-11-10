package me.akhmetov.vxl.core;

public abstract class MapNodeWithMetadata extends MapNode {
    /**
     * Persists the state of this node to metadata that will be serialized to disk. Note that this method cannot simply
     * return the {@link MapNodeWithMetadata} instance itself, since it may not be serialized due to security restrictions.
     * @return
     */
    abstract Object storeToMetadata();

    public abstract String getDecoderId();
}
