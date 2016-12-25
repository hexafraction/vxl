package me.akhmetov.vxl.api.map;

import me.akhmetov.vxl.api.VxlPluginExecutionException;
import me.akhmetov.vxl.api.map.MapNodeWithMetadata;

public interface INodeMetadataDecoder {
    MapNodeWithMetadata decodeNode(Object metadata) throws VxlPluginExecutionException;
}
