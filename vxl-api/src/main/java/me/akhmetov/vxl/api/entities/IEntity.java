package me.akhmetov.vxl.api.entities;

/**
 * Represents an in-game entity (that is, an object that can be interacted with but is not a node).
 * An entity has the following elements:
 * <ul><li>A representation that describes its appearance</li><li>
 * physical interaction models that describe its behavior/physics
 * at multiple levels of fidelity (for example, realtime smooth animation, animation for distant entities, and animation
 * for entities that cannot be currently seen due to occlusion)
 * </li>
 * <li>A deterministic interaction model that determines its eventual location and interaction with the game world.</li>
 * </ul>
 */
public interface IEntity {
}
