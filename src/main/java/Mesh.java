package main.java;

import org.joml.Vector3f;

import java.util.Arrays;

public class Mesh {

    Vector3f position;
    float[] vertices;
    int[] indices;
    float[] normals;
    short[] colors;

    MeshType type = MeshType.TRIANGLE;

    int vaoID;

    Mesh(float[] vertices, short[] colors, int[] indices, float[] normals, Vector3f position){

        this.vertices = vertices;
        this.indices = indices;
        this.position = position;
        this.normals = normals;
        this.colors = colors;



    }
    Mesh(float[] vertices, int[] indices, float[] normals, Vector3f position){

        this.vertices = vertices;
        this.indices = indices;
        this.position = position;
        this.normals = normals;
        colors = new short[vertices.length/3];
        Arrays.fill(colors,(short)0);


    }
    Mesh(float[] vertices, short[] colors, int[] indices, Vector3f position){

        this.vertices = vertices;
        this.indices = indices;
        this.position = position;
        this.colors = colors;
        normals = new float[vertices.length];
        Arrays.fill(normals,0);


    }

    Mesh(float[] vertices, int[] indices, Vector3f position){

        this.vertices = vertices;
        this.indices = indices;
        this.position = position;
        normals = new float[vertices.length];
        Arrays.fill(normals,0);
        colors = new short[vertices.length/3];
        Arrays.fill(colors,(short)0);

    }

    public void setNormals(float[] normals){
        this.normals = normals;
    }

    public void loadMesh(){

        vaoID = Loader.loadToGPU(this);

    }
    public void unloadMesh(){
        Loader.unload(this);
    }

}
