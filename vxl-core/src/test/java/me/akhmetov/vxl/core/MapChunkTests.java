package me.akhmetov.vxl.core;

import me.akhmetov.vxl.api.MapNodeWithMetadata;
import me.akhmetov.vxl.api.VxlPluginExecutionException;
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
        MapChunkImpl c = generateChunk(r, 0);
        byte[] buf = c.serialize();
        System.out.println("size of a chunk: " + buf.length);
        System.out.println("Expected chunks per 4 GB: " + 4294967296.0 / (buf.length));
    }

    @Test
    public void testSerDesHvy() throws Exception {
        Random r = new Random();
        for(int i = 0; i < 100; i++){
            iterateHeavy(r, i);
        }
        long nanoStart = System.nanoTime();
        for(int i = 0; i < 1000; i++){
            iterateHeavy(r, i);
        }
        long nanoEnd = System.nanoTime();
        System.out.println("nanos per cycle (heavy): " + (nanoEnd-nanoStart) / 1000.0);
        System.out.println("chunks per second (heavy): " + 1000000000/((nanoEnd-nanoStart) / 1000.0));
        MapChunkImpl d = generateChunkHeavy(r, 0);
        byte[] buf2 = d.serialize();
        System.out.println("size of a heavy chunk: " + buf2.length);
        System.out.println("Expected heavy chunks per 4 GB: " + 4294967296.0 / (buf2.length));
    }

    @Test
    public void testSerDesPacked() throws Exception {
        Random r = new Random();
        for(int i = 0; i < 100; i++){
            iteratePacked(r, i);
        }
        long nanoStart = System.nanoTime();
        for(int i = 0; i < 10000; i++){
            iteratePacked(r, i);
        }
        long nanoEnd = System.nanoTime();
        System.out.println("nanos per cycle (packed): " + (nanoEnd-nanoStart) / 10000.0);
        System.out.println("chunks per second (packed): " + 1000000000/((nanoEnd-nanoStart) / 10000.0));
        MapChunkImpl d = generateChunkHeavy(r, 0);
        byte[] buf2 = d.serialize();
        System.out.println("size of a packed chunk: " + buf2.length);
        System.out.println("Expected packed chunks per 4 GB: " + 4294967296.0 / (buf2.length));
    }


    private MapChunkImpl generateChunkHeavy(Random r, int i) throws VxlPluginExecutionException {
        MapChunkImpl c = new MapChunkImpl(null, 0, 0, 0, null);
        for(int j = 0; j < 1024; j++){
            int x = r.nextInt(16), y = r.nextInt(16), z = r.nextInt(16);
            c.setNode(x, y, z, r.nextInt(100000));
            if(r.nextFloat()<0.5) {
                int rx = r.nextInt();
                int ry = r.nextInt();
                MapNodeWithMetadata mn = new MapNodeWithMetadata("testasdfdsafsdafjdsfjds;lkjds" +
                        "adsfdsafdsagdsagjsdafjdsajfkldsfjdsajlf;dsjlfka" +
                        "dsfdsafjdsfjds;lfj;ldsafj;dsadaj;glkdsajlg;dsjf;ldsaj;lfds;jfslkdfj;ads" +
                        "dsafjdsfj;ldsfjldsafj;ldsfj;ldsjf;ldsjf;ldsfjldsjf;lds;lfjdsafj;ldsaf;jkds" +
                        "dsafjdsfj;ldsfj;lsdfjl;dsjf;ldsajkf;ldsjf" +
                        "dfjdkfjlkds"+r.nextInt()) {
                    @Override
                    public Object storeToMetadata() {
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

    private void iterate(Random r, int i) throws IOException, ChunkCorruptionException, VxlPluginExecutionException {
        MapChunkImpl c = generateChunk(r, i);
        byte[] buf = c.serialize();
        MapChunkImpl c2 = new MapChunkImpl(null, 0, 0, 0, null);
        c2.deserialize(buf);
        Assert.assertEquals(c.getModificationBitfield(), c2.getModificationBitfield());
        //System.out.println(Long.toBinaryString(c.getModificationBitfield()));
        Assert.assertTrue("Chunk data does not match", customDeepEquals(c.chunkData, c2.chunkData));
        Assert.assertTrue("Extended data does not match", c.extendedNodes.equals(c2.extendedNodes));
    }

    private void iterateHeavy(Random r, int i) throws IOException, ChunkCorruptionException, VxlPluginExecutionException {
        MapChunkImpl c = generateChunkHeavy(r, i);
        byte[] buf = c.serialize();
        MapChunkImpl c2 = new MapChunkImpl(null, 0, 0, 0, null);
        c2.deserialize(buf);
        Assert.assertEquals(c.getModificationBitfield(), c2.getModificationBitfield());
        //System.out.println(Long.toBinaryString(c.getModificationBitfield()));
        Assert.assertTrue("Chunk data does not match", customDeepEquals(c.chunkData, c2.chunkData));
        Assert.assertTrue("Extended data does not match", c.extendedNodes.equals(c2.extendedNodes));
    }


    private void iteratePacked(Random r, int i) throws IOException, ChunkCorruptionException, VxlPluginExecutionException {
        MapChunkImpl c = generateChunkPacked(r, i);
        byte[] buf = c.serialize();
        MapChunkImpl c2 = new MapChunkImpl(null, 0, 0, 0, null);
        c2.deserialize(buf);
        Assert.assertEquals(c.getModificationBitfield(), c2.getModificationBitfield());
        //System.out.println(Long.toBinaryString(c.getModificationBitfield()));
        Assert.assertTrue("Chunk data does not match", customDeepEquals(c.chunkData, c2.chunkData));
        Assert.assertTrue("Extended data does not match", c.extendedNodes.equals(c2.extendedNodes));
    }

    private MapChunkImpl generateChunkPacked(Random r, int q) {
        MapChunkImpl c = new MapChunkImpl(null, 0, 0, 0, null);
        for(int i = 0 ; i < 16; i++){
            for(int j = 0; j < 16; j++){
                for(int k = 0; k < 16; k++){
                    c.setNode(k, j, i, r.nextInt(10000));
                }
            }
        }
        return c;
    }


    private boolean customDeepEquals(int[][][] chunkData, int[][][] chunkData1) {
        boolean m = true;
        for(int i = 0; i < 16; i++){
            for(int j = 0; j < 16; j++){
                for(int k = 0; k < 16; k++){
                    if(chunkData[i][j][k]!=chunkData1[i][j][k]){
                        m = false;
                        System.out.println(i+","+j+","+k+":"+chunkData[i][j][k]+" vs "+chunkData1[i][j][k]);
                    }
                }
            }
        }
        return m;
    }

    private MapChunkImpl generateChunk(Random r, int i) throws VxlPluginExecutionException {
        MapChunkImpl c = new MapChunkImpl(null, 0, 0, 0, null);

        int x = r.nextInt(16), y = r.nextInt(16), z = r.nextInt(16);
        for(int j = 0; j < 16; j++){
            if(r.nextFloat()>0.3) x = (x+1)%16;
            if(r.nextFloat()>0.3) y = (x+1)%16;
            if(r.nextFloat()>0.1) z = (x+1)%16;
            c.setNode(x, y, z, r.nextInt(100000));
            if(r.nextFloat()<0.5) {
                int rx = r.nextInt();
                int ry = r.nextInt();
                MapNodeWithMetadata mn = new MapNodeWithMetadata("testasdffdsafdsflkdsj;dgksadf;lkdsj;lkjlsadhjij;"+r.nextInt()) {
                    @Override
                    public Object storeToMetadata() {
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
