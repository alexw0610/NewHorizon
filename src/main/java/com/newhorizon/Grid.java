package com.newhorizon;


import org.joml.Vector3f;

public class Grid {

    public static Mesh getGrid(){

        float[] vertices = new float[4608];

        int count = 0;

        for(int x = 0; x < 16; x++){
            for(int i = 0; i < 16; i++){
                vertices[count++] = LookupTable.CHUNKSIZE*x;
                vertices[count++] = LookupTable.CHUNKSIZE*i;
                vertices[count++] = 0;

                vertices[count++] = LookupTable.CHUNKSIZE*x;
                vertices[count++] = LookupTable.CHUNKSIZE*i;
                vertices[count++] = LookupTable.CHUNKSIZE*LookupTable.CHUNKSIZE;
            }
        }
        for(int y = 0; y < 16; y++){
            for(int i = 0; i < 16; i++){
                vertices[count++] = LookupTable.CHUNKSIZE*i;
                vertices[count++] = 0;
                vertices[count++] = LookupTable.CHUNKSIZE*y;

                vertices[count++] = LookupTable.CHUNKSIZE*i;
                vertices[count++] = LookupTable.CHUNKSIZE*LookupTable.CHUNKSIZE;
                vertices[count++] = LookupTable.CHUNKSIZE*y;
            }
        }
        for(int z = 0; z < 16; z++){
            for(int i = 0; i < 16; i++){
                vertices[count++] = 0;
                vertices[count++] = LookupTable.CHUNKSIZE*z;
                vertices[count++] = LookupTable.CHUNKSIZE*i;

                vertices[count++] = LookupTable.CHUNKSIZE*LookupTable.CHUNKSIZE;
                vertices[count++] = LookupTable.CHUNKSIZE*z;
                vertices[count++] = LookupTable.CHUNKSIZE*i;
            }
        }

        int[] indices = new int[1536];

        for(int i = 0; i < 1536; i++){
            indices[i] = i;
        }

        float[] colors = new float[3*1536];

        for(int i = 0; i < 1536;i++){
            colors[i*3]      = i<512 ?  (short)1 : 0;
            colors[i*3+1]    = i<1024 && i>512 ?  (short)1 : 0;
            colors[i*3+2]    = i>1024 ?  (short)1 : 0;
        }

        return new Mesh(vertices,colors,indices,new Vector3f(0,0,0));
    }
}
