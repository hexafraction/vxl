package me.akhmetov.vxl.api.rendering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Describes when during a rendering operation a given entity should be drawn.
 */
public enum RenderBucket {
    /**
     * The earliest draw phase. Appropriate for textures that have an alpha of one everywhere.
     */
    OPAQUE(0),
    /**
     * A draw phase after opaque items are drawn. Appropriate for textures that have an alpha of either zero or one
     * everywhere. Alpha values between zero and one usually behave like the closer of zero or one, but this is not
     * guaranteed due to the effects of texture filtering. The depth buffer is written to if the fragment is rendered.
     * Backfaces are culled.
     */
    TRANSPARENT_CULLED(1),

    /**
     * Same as transparent, but backfaces are not culled. Choosing this over TRANSPARENT_CULLED is dependent on which
     * of the behaviors makes the node look "better" in the plugin developer's view. There may be a slight performance
     * degradation if TRANSPARENT_NO_CULL is used, due to the higher number of faces actually drawn.
     */
    TRANSPARENT_NO_CULL(2),
    /**
     *  Used for translucent materials that get a "waving liquids" geometry shader effect. Due to the way blending
     *  works, and because depth sorting is not implemented (although it would not be difficult to do in most cases
     *  due to the grid-like structure of the game) it cannot be used for general-case translucent objects. Currently,
     *  it uses the depth test for somewhat usable results--if a fragment fails the depth test it was behind a closer
     *  fragment and thus the liquid color is not blended in. If the depth test succeeds, then the color gets drawn--but
     *  the depth buffer is not written. Any other liquid front-faces (i.e. due to glass) need to be preserved properly.
     *  Rendering the liquid bucket does not cause the depth buffer to be updated as a result (so a farther-back frontface
     *  of water can be drawn and blended in).
     *
     *  This may be revisited if it is found that unacceptable behavior arises when different-looking liquids render in
     *  inconsistent orders.
     */
    LIQUID_TRANSLUCENT(3);

    public final int ordinal;

    RenderBucket(int ordinal) {
        this.ordinal = ordinal;
    }

    public static final List<RenderBucket> inRenderOrder;
    static {
        ArrayList<RenderBucket> list = new ArrayList<RenderBucket>();
        list.add(OPAQUE);
        list.add(TRANSPARENT_CULLED);
        list.add(TRANSPARENT_NO_CULL);
        list.add(LIQUID_TRANSLUCENT);
        inRenderOrder = Collections.unmodifiableList(list);
    }
}
