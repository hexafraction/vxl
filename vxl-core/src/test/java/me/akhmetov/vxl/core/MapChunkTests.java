package me.akhmetov.vxl.core;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

@SuppressWarnings("ALL")
public class MapChunkTests {

    static boolean testEquals(NodeMetadata m1, NodeMetadata m2){
        return (m1==m2 || (m1.metadata.equals(m2.metadata) && m1.metadataDecoder.equals(m2.metadataDecoder)));
    }

    @Test
    public void testSerDes() throws Exception {
        Random r = new Random();
        for(int i = 0; i < 100; i++){
            iterate(r, i);
        }
        long nanoStart = System.nanoTime();
        for(int i = 0; i < 100000; i++){
            iterate(r, i);
        }
        long nanoEnd = System.nanoTime();
        System.out.println("nanos per cycle: " + (nanoEnd-nanoStart) / 100000.0);
        System.out.println("chunks per second: " + 1000000000/((nanoEnd-nanoStart) / 100000.0));

    }

    private void iterate(Random r, int i) throws IOException, ChunkCorruptionException {
        MapChunk c = generateChunk(r, i);
        byte[] buf = c.serialize();
        MapChunk c2 = new MapChunk(null, 0, 0, 0, null);
        c2.deserialize(buf);
        Assert.assertTrue("Chunk data does not match", customDeepEquals(c.chunkData, c2.chunkData));
        Assert.assertTrue("Extended data does not match", c.extendedNodes.equals(c2.extendedNodes));
    }

    private boolean customDeepEquals(int[][][] chunkData, int[][][] chunkData1) {
        boolean m = true;
        for(int i = 0; i < 16; i++){
            for(int j = 0; j < 16; j++){
                for(int k = 0; k < 16; k++){
                    if(chunkData[i][j][k]!=chunkData1[i][j][k]){
                        m = false;
                        System.out.println(i+","+j+","+k);
                    }
                }
            }
        }
        return m;
    }

    private MapChunk generateChunk(Random r, int i) {
        MapChunk c = new MapChunk(null, 0, 0, 0, null);
        for(int j = 0; j < 16; j++){
            int x = r.nextInt(1), y = r.nextInt(1), z = r.nextInt(1);
            if(r.nextFloat()>0.3) x = (x+1)%16;
            if(r.nextFloat()>0.3) y = (x+1)%16;
            if(r.nextFloat()>0.1) z = (x+1)%16;
            c.setNode(x, y, z, r.nextInt(100000));
            if(r.nextFloat()<0.5) {
                int rx = r.nextInt();
                int ry = r.nextInt();
                MapNodeWithMetadata mn = new MapNodeWithMetadata("test") {
                    @Override
                    Object storeToMetadata() {
                        return Integer.toHexString(rx);
                    }

                    @Override
                    public String getDecoderId() {
                        return Integer.toHexString(ry);
                    }
                };
                c.setNode(x, y, z, mn);
            }
        }
        return c;
    }
}
