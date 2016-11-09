package me.akhmetov.vxl.core;

import java.util.HashMap;

public class NodeMetadata {
    GameState state;
    // STOPSHIP FIXME TODO Allow overriding with a custom MapNode
    MapNode node; // Not serialized. The script needs to reconstruct this from the rest of the metadata.
    String metadataDecoder; // name of the metadata decoder in the metadata decoder registry
    Object metadata; // the actual data to be decoded by the metadata decoder.
    HashMap<String, Object> additionalData; // Any additional


    public MapNode getNode() {
        if(node==null){
            synchronized(this){
                if(node==null) node = state.getRegisty().getMetadataDecoder(metadataDecoder).decodeNode(metadata);
            }
        }
        return node;
    }
}
