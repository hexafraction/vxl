package me.akhmetov.vxl.api.rendering;

/**
 * Describes the appearance of a node as a 1m*1m*1m "block", with textures of various types. Useful for most nodes.
 */
public class BlockNodeAppearance extends NodeAppearance {
    //FIXME light emission and stuff
    private Texture posX;
    private Texture negX;
    private Texture posY;

    public Texture getPosX() {
        return posX;
    }

    public Texture getNegX() {
        return negX;
    }

    public Texture getPosY() {
        return posY;
    }

    public Texture getNegY() {
        return negY;
    }

    public Texture getPosZ() {
        return posZ;
    }

    public Texture getNegZ() {
        return negZ;
    }

    private Texture negY;
    private Texture posZ;
    private Texture negZ;

    public BlockNodeAppearance(Texture posX, Texture negX, Texture posY, Texture negY, Texture posZ, Texture negZ) {
        this.posX = posX;
        this.negX = negX;
        this.posY = posY;
        this.negY = negY;
        this.posZ = posZ;
        this.negZ = negZ;
    }
}
