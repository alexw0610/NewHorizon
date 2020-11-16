package com.newhorizon;

import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.Arrays;

import static org.joml.Intersectionf.testLineSegmentTriangle;

public class Mesh {

    Vector3f position;
    float[] vertices;
    int[] indices;
    float[] normals;
    float[] colors;
    String id;

    MeshType type = MeshType.TRIANGLE;

    int vaoID;
    int[] vboID;

    Mesh(float[] vertices, float[] colors, int[] indices, float[] normals, Vector3f position){

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
        colors = new float[vertices.length/3];
        Arrays.fill(colors,(short)0);


    }
    Mesh(float[] vertices, float[] colors, int[] indices, Vector3f position){

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
        colors = new float[vertices.length/3];
        Arrays.fill(colors,(short)0);

    }



    public void setNormals(float[] normals){
        this.normals = normals;
    }

    public Collision getRayIntersectionWithNormal(Vector3f pos, Vector3f dir) {
        boolean intersect;
        Collision col = null;
        for(int indices = 0;indices<this.indices.length;){

            Vector3f pointA = new Vector3f();
            pointA.x = this.vertices[this.indices[indices]*3];
            pointA.y = this.vertices[this.indices[indices]*3+1];
            pointA.z = this.vertices[this.indices[indices]*3+2];
            Vector3f pointB = new Vector3f();
            pointB.x = this.vertices[this.indices[indices+1]*3];
            pointB.y = this.vertices[this.indices[indices+1]*3+1];
            pointB.z = this.vertices[this.indices[indices+1]*3+2];
            Vector3f pointC = new Vector3f();
            pointC.x = this.vertices[this.indices[indices+2]*3];
            pointC.y = this.vertices[this.indices[indices+2]*3+1];
            pointC.z = this.vertices[this.indices[indices+2]*3+2];
            pointA.add(position);
            pointB.add(position);
            pointC.add(position);

            intersect = rayIntersectionTriangle(pointA,pointB,pointC,pos,dir);
            if(intersect){
                col = new Collision();
                col.distance = 0.0f;
                col.normal = new Vector3f(this.normals[this.indices[indices]*3],this.normals[this.indices[indices]*3+1],this.normals[this.indices[indices]*3+2]);
                break;
            }
            indices += 3;
        }
        return col;
    }


    private boolean rayIntersectionTriangle(Vector3f a, Vector3f b, Vector3f c, Vector3f pos, Vector3f dir){

        return testLineSegmentTriangle(pos,new Vector3f(pos).add(dir),a,b,c,0.0000000001f);

    }
    class Collision{
        public float distance;
        public Vector3f normal;
    }
    public void loadMesh(){

        Loader.BufferObject bo = Loader.loadToGPU(this);
        this.vaoID = bo.vaoID;
        this.vboID = bo.vboID;

    }
    public void unloadMesh(){
        Loader.unload(this);
    }



}
