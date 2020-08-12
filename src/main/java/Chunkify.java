package main.java;

import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.Vector;

public class Chunkify {

    public static Vector3i getChunk(Vector3f input){
        return new Vector3i((int)Math.floor(input.x/LookupTable.CHUNKSIZE),(int)Math.floor(input.y/LookupTable.CHUNKSIZE),(int)Math.floor(input.z/LookupTable.CHUNKSIZE));
    }
}
