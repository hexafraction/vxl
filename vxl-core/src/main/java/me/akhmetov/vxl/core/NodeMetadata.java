package me.akhmetov.vxl.core;

import me.akhmetov.vxl.api.map.MapNode;
import me.akhmetov.vxl.api.map.MapNodeWithMetadata;
import me.akhmetov.vxl.api.VxlPluginExecutionException;
import me.akhmetov.vxl.core.security.ScriptMaySerialize;
import org.nustaq.serialization.annotations.Serialize;
import org.nustaq.serialization.annotations.Transient;

import java.io.Serializable;

@Transient
@ScriptMaySerialize
final class NodeMetadata implements Serializable {
    final GameState state;

    volatile MapNodeWithMetadata node; // Not serialized. The script needs to reconstruct this from the rest of the metadata.
    @Serialize
    final String metadataDecoder; // name of the metadata decoder in the metadata decoder registry
    @Serialize
    Object metadata; // the actual data to be decoded by the metadata decoder.

    public NodeMetadata(GameState state, MapNodeWithMetadata node, String metadataDecoder, Object metadata) {
        this.state = state;
        this.node = node;
        this.metadataDecoder = metadataDecoder;
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeMetadata that = (NodeMetadata) o;

        if (metadataDecoder != null ? !metadataDecoder.equals(that.metadataDecoder) : that.metadataDecoder != null)
            return false;
        return metadata != null ? metadata.equals(that.metadata) : that.metadata == null;

    }

    @Override
    public int hashCode() {
        int result = metadataDecoder != null ? metadataDecoder.hashCode() : 0;
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        return result;
    }

    public MapNode getNode() throws VxlPluginExecutionException {
        if (node == null) {
            synchronized (lock) {
                if (node == null) {
                    if (state.getNodeAPI().getMetadataDecoder(metadataDecoder) == null) {
                        throw new VxlPluginExecutionException("Could not find metadata decoder with name " + metadataDecoder);
                    }
                    node = state.getNodeAPI().getMetadataDecoder(metadataDecoder).decodeNode(metadata);
                }
            }
        }
        return node;
    }

    private Object lock = new Object();
}
