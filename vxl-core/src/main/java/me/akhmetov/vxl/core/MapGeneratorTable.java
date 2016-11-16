package me.akhmetov.vxl.core;

import me.akhmetov.vxl.api.VxlPluginExecutionException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Internally used to ensure that a chunk's base data is generated with the same map generator code,
 * even if new map generators are added, or if map generators are updated to new versions.
 */
public class MapGeneratorTable implements Serializable {
    private CopyOnWriteArrayList<String[]> table = new CopyOnWriteArrayList<>();
    void getMapGeneratorTable(int stepping) throws VxlCoreException {
        if(stepping >= (table.size())){
            throw new VxlCoreException("Attempted to get a map generator table with too high a stepping");
        }
    }

    /**
     * Inserts the generator set into the table if necessary, returning the set of generators to be used.
     * @param generators The set of generators for this session.
     * @return The numerical ID to be stored in chunks.
     */
    int initializeStepping(String[] generators){
        String[] prevGenerators = table.get(table.size()-1);
        if(Arrays.equals(generators, prevGenerators)){
            return table.size()-1;
        } else {
            table.add(generators);
            return table.size()-1;
        }
    }

}
