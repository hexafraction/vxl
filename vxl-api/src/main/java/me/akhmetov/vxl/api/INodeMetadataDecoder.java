package me.akhmetov.vxl.api;

public interface INodeMetadataDecoder {
    MapNodeWithMetadata decodeNode(Object metadata) throws VxlPluginExecutionException;
}
