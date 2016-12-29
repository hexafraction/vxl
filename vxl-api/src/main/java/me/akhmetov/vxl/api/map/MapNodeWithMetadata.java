package me.akhmetov.vxl.api.map;

import me.akhmetov.vxl.api.rendering.NodeAppearance;

public abstract class MapNodeWithMetadata extends MapNode {
    public MapNodeWithMetadata(String name, NodeAppearance appearance) {
        super(name, appearance);
    }

    /**
     * Persists the state of this node to metadata that will be serialized to disk. Note that this method cannot simply
     * return the {@link MapNodeWithMetadata} instance itself, since it may not be serialized due to security restrictions.
     * @return The serializable object form of this node.
     */
    public abstract Object storeToMetadata();

    public abstract String getDecoderId();
}
