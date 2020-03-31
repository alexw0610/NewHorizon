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

        double cube = Math.pow(side+1,3);
        float chunkPosX = chunkX*LookupTable.CHUNKSIZE;
        float chunkPosY = chunkY*LookupTable.CHUNKSIZE;
        float chunkPosZ = chunkZ*LookupTable.CHUNKSIZE;
        Vector3f mid = new Vector3f((diameter*LookupTable.CHUNKSIZE)/2,(diameter*LookupTable.CHUNKSIZE)/2,(diameter*LookupTable.CHUNKSIZE)/2);
        float stretchBig = 0.00061f;
        float stretchMedium = 0.0061f;
        float stretchSmall = 0.061f;

        for(int i = 0; i < cube;i++){

            Vector3f pos = new Vector3f(
                        (i%(side+1)),
                        ((i/((side+1)*(side+1))%(side+1))),
                        ((i/(side+1) )%(side+1))
                    );

            pos = pos.mul(resolution);
            pos.x += chunkPosX;
            pos.y += chunkPosY;
            pos.z += chunkPosZ;

            float valueBig = (float)osn.noise(pos.x*stretchBig, pos.y*stretchBig,pos.z*stretchBig);
            float valueMedium = (float)osn.noise(pos.x*stretchMedium, pos.y*stretchMedium,pos.z*stretchMedium);
            float valueSmall = (float)osn.noise(pos.x*stretchSmall, pos.y*stretchSmall,pos.z*stretchSmall);

            float dist = mid.distance(pos);

            voxelData[i] = ((477)*LookupTable.CHUNKSIZE)-dist+(valueBig*500)+(valueMedium*5)+(valueSmall*0.5f);

        }
        return voxelData;
    }

}
