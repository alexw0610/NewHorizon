package main.java;


import org.joml.Vector3f;

public class Grid {

    public static Mesh getGrid(){

        float[] vertices = new float[4608];

        int count = 0;

        for(int x = 0; x < 16; x++){
            for(int i = 0; i < 16; i++){
                vertices[count++] = 32*x;
                vertices[count++] = 32*i;
                vertices[count++] = 0;

                vertices[count++] = 32*x;
                vertices[count++] = 32*i;
                vertices[count++] = 512;
            }
        }
        for(int y = 0; y < 16; y++){
            for(int i = 0; i < 16; i++){
                vertices[count++] = 32*i;
                vertices[count++] = 0;
                vertices[count++] = 32*y;

                vertices[count++] = 32*i;
                vertices[count++] = 512;
                vertices[count++] = 32*y;
            }
        }
        for(int z = 0; z < 16; z++){
            for(int i = 0; i < 16; i++){
                vertices[count++] = 0;
                vertices[count++] = 32*z;
                vertices[count++] = 32*i;

                vertices[count++] = 512;
                vertices[count++] = 32*z;
                vertices[count++] = 32*i;
            }
        }

        int[] indices = new int[1536];

        for(int i = 0; i < 1536; i++){
            indices[i] = i;
        }

        return new Mesh(vertices,indices,new Vector3f(0,0,0));
    }
}
