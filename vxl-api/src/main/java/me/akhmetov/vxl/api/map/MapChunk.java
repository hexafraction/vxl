package me.akhmetov.vxl.api.map;

import me.akhmetov.vxl.api.VxlPluginExecutionException;

import java.io.Serializable;

/**
 * Represents a 16x16x16 portion of the map.
 */
public interface MapChunk {
    /**
     * Updates the chunk's data by setting a node.
     *
     * @param xP   The X coordinate <emph>within</emph> the chunk (i.e. for chunk with chunk coords (0, 1, 0)
     *             the point with world space node coordinates (2, 11, 5) has xP equal to 2.
     * @param yP   The Y coordinate <emph>within</emph> the chunk (i.e. for chunk with chunk coords (0, 1, 0)
     *             the point with world space node coordinates (2, 18, 5) has yP equal to 2 (because 2+16 == 18).
     * @param zP   The Z coordinate <emph>within</emph> the chunk (i.e. for chunk with chunk coords (0, 1, 0)
     *             the point with world space node coordinates (2, 11, 5) has zP equal to Z.
     * @param node The node to set.
     * @throws VxlPluginExecutionException If an issue occurs with storing the node. Typically (but not always) this
     *                                     occurs as a result of issues persisting a {@link MapNodeWithMetadata}.
     */
    void setNode(int xP, int yP, int zP, MapNode node) throws VxlPluginExecutionException;

    /**
     * Returns the node present at a location within the chunk.
     *
     * @param xP The X coordinate <emph>within</emph> the chunk (i.e. for chunk with chunk coords (0, 1, 0)
     *           the point with world space node coordinates (2, 11, 5) has xP equal to 2.
     * @param yP The Y coordinate <emph>within</emph> the chunk (i.e. for chunk with chunk coords (0, 1, 0)
     *           the point with world space node coordinates (2, 18, 5) has yP equal to 2 (because 2+16 == 18).
     * @param zP The Z coordinate <emph>within</emph> the chunk (i.e. for chunk with chunk coords (0, 1, 0)
     *           the point with world space node coordinates (2, 11, 5) has zP equal to Z.
     * @return The node at that location.
     * @throws VxlPluginExecutionException If an issue occurs with reading the node. Typically (but not always) an issue
     *                                     with decoding metadata.
     */
    MapNode getNode(int xP, int yP, int zP) throws VxlPluginExecutionException;

    /**
     * Returns the X chunk coordinate for this node. See {@link MapAPI} for details.
     */
    int getX();
    /**
     * Returns the Y chunk coordinate for this node. See {@link MapAPI} for details.
     */
    int getY();
    /**
     * Returns the Z chunk coordinate for this node. See {@link MapAPI} for details.
     */
    int getZ();

    /**
     * Puts a piece of node metadata.
     * @param key The string that identifies the metadata.
     * @param value The metadata to store.
     */
    Serializable put(String key, Serializable value);

    /**
     * Gets a piece of node metadata.
     * @param key The string that was passed as key when calling {@link MapChunk#put(String, Serializable)}.
     * @return The metadata that was stored, or null if no metadata with that name was stored for the chunk.
     */
    Serializable get(String key);
}
