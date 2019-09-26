package main.java;

import org.joml.Vector3f;

public class Mesh {

    Vector3f position;
    float[] vertices;
    short[] indices;
    float[] normals;

    int vaoID;


    Mesh(float[] vertices, short[] indices, float[] normals, Vector3f position){

        this.vertices = vertices;
        this.indices = indices;
        this.position = position;
        this.normals = normals;

        vaoID = Loader.loadToGPU(this);

    }

}
