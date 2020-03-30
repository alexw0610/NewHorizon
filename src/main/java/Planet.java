package main.java;

import main.java.noise.OpenSimplexNoise;
import org.joml.Vector3f;

public class Planet {
    int seed;
    int diameter;
    OpenSimplexNoise osn;
    // diameter is in chunks
    public Planet(int seed, int diameter){

        this.seed = seed;
        this.diameter = diameter;
        osn = new OpenSimplexNoise(this.seed);

    }

    public float[] getData(int chunkX, int chunkY, int chunkZ, int span,int resolution){


        int side = LookupTable.CHUNKSIZE;


        float[] voxelData = new float[(int)Math.pow(side+1,3)];


        for(int i = 0; i < (Math.pow(side+1,3));i++){

            Vector3f pos = new Vector3f(
                    (i%(side+1)),
                    ((i/((side+1)*(side+1))%(side+1))),
                    ((i/(side+1) )%(side+1))
                    );

            pos = pos.mul(resolution);
            pos.x = pos.x + (chunkX*LookupTable.CHUNKSIZE);
            pos.y = pos.y + (chunkY*LookupTable.CHUNKSIZE);
            pos.z = pos.z + (chunkZ*LookupTable.CHUNKSIZE);

            Vector3f mid = new Vector3f((diameter*LookupTable.CHUNKSIZE)/2,(diameter*LookupTable.CHUNKSIZE)/2,(diameter*LookupTable.CHUNKSIZE)/2);

            float stretchBig = 0.001f;
            float valueBig = (float)osn.noise(pos.x*stretchBig, pos.y*stretchBig,pos.z*stretchBig);
            float stretchSmall = 0.01f;
            float valueSmall = (float)osn.noise(pos.x*stretchSmall, pos.y*stretchSmall,pos.z*stretchSmall);


            float dist = mid.distance(pos);


            voxelData[i] = 506*LookupTable.CHUNKSIZE-dist+(valueBig*100)-(valueSmall*10);



        }
        return voxelData;
    }

}
