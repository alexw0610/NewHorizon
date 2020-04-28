package main.java;

import main.java.noise.OpenSimplexNoise;
import org.joml.Vector3f;

public class Planet {
    int seed;
    int diameter;
    int radius;
    OpenSimplexNoise osn;
    // diameter is in chunks
    public Planet(int seed, int diameter){

        this.seed = seed;
        this.diameter = diameter;
        this.radius = diameter/3;
        osn = new OpenSimplexNoise(this.seed);

    }

    public Mesh getMesh(int chunkX, int chunkY, int chunkZ, int span){
        VoxelGroup temp = getData(chunkX,chunkY,chunkZ,span,span);
        return Voxel.getMesh(temp.data,temp.color,span,chunkX,chunkY,chunkZ);
    }
    private VoxelGroup getData(int chunkX, int chunkY, int chunkZ, int span,int resolution){


        int side = LookupTable.CHUNKSIZE;
        float[] voxelData = new float[(int)Math.pow(side+1,3)];
        short[] voxelColor = new short[(int)Math.pow(side+1,3)];
        double cube = Math.pow(side+1,3);
        float chunkPosX = chunkX*LookupTable.CHUNKSIZE;
        float chunkPosY = chunkY*LookupTable.CHUNKSIZE;
        float chunkPosZ = chunkZ*LookupTable.CHUNKSIZE;
        Vector3f mid = new Vector3f((diameter*LookupTable.CHUNKSIZE)/2,(diameter*LookupTable.CHUNKSIZE)/2,(diameter*LookupTable.CHUNKSIZE)/2);

        float stretchMedium = 0.009f;
        float stretchSmall = 0.025f;
        float offset = 1231241.0f;

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

            float value1 = (float)osn.noise((pos.x+offset)*stretchSmall, (pos.y+offset)*stretchSmall,(pos.z+offset)*stretchSmall);
            float value2 = (float)osn.noise(pos.x*stretchSmall, pos.y*stretchSmall,pos.z*stretchSmall);
            float region = (float)osn.noise((pos.x+offset*2)*stretchMedium, (pos.y+offset*2)*stretchMedium,(pos.z+offset*2)*stretchMedium);
            float ore = (float)osn.noise((pos.x+offset*3)*0.003, (pos.y+offset*3)*0.003,(pos.z+offset*3)*0.003);
            float dist = mid.distance(pos);
            region = Math.max(region,0);
            float oredist = new Vector3f(LookupTable.CHUNKSIZE*64,LookupTable.CHUNKSIZE*32,LookupTable.CHUNKSIZE*32).distance(pos);

            //float baseShape = ((float)radius*(float)LookupTable.CHUNKSIZE+(valueMedium*400.0f*region))-dist;
            voxelData[i] = value1*(18)*region+((float)radius*(float)LookupTable.CHUNKSIZE-dist)
                    +value2*(18)*region;
            //voxelData[i] = Math.min(Math.max(((float)radius*(float)LookupTable.CHUNKSIZE)-dist,-1),1);
            voxelColor[i] = ore < 0.0f ? (short)2 : 1 ;//0R 1G 2B
        }
        return new VoxelGroup(voxelData,voxelColor);
    }

    private class VoxelGroup{

        float[] data;
        short[] color;

        public VoxelGroup(float[] data, short[]color){
            this.data = data;
            this.color = color;
        }
    }

}
