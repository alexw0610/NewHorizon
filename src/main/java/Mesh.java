package main.java;

import org.joml.Vector3f;

import java.util.Arrays;

public class Mesh {

    Vector3f position;
    float[] vertices;
    int[] indices;
    float[] normals;

    int vaoID;


    Mesh(float[] vertices, int[] indices, float[] normals, Vector3f position){

        this.vertices = vertices;
        this.indices = indices;
        this.position = position;
        this.normals = normals;


    }

    Mesh(float[] vertices, int[] indices, Vector3f position){

        this.vertices = vertices;
        this.indices = indices;
        this.position = position;
        normals = new float[vertices.length];
        Arrays.fill(normals,0);

    }

    public void loadMesh(){

        vaoID = Loader.loadToGPU(this);

    }

}
