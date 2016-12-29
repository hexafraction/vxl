package me.akhmetov.vxl.api.rendering;

public class BlockNode extends NodeAppearance {
    //FIXME light emission and stuff
    private Texture top, bottom, north, south, east, west;

    public BlockNode(Texture top, Texture bottom, Texture north, Texture south, Texture east, Texture west) {
        this.top = top;
        this.bottom = bottom;
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
    }
}
