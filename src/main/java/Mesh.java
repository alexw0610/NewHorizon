package main.java;

import org.joml.Vector3f;

import java.util.Arrays;
import java.util.Vector;

import static org.joml.Intersectionf.intersectRayTriangle;

public class Mesh {

    Vector3f position;
    float[] vertices;
    int[] indices;
    float[] normals;
    float[] colors;

    MeshType type = MeshType.TRIANGLE;

    int vaoID;

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
        Vector3f target = null;
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


            indices += 3;
            target = rayIntersectionTriangle(pointA,pointB,pointC,pos,dir);
            if(target == null){
                //col = new Collision();
                //col.position = target;
                //col.normal = new Vector3f(this.normals[this.indices[indices]*3],this.normals[this.indices[indices]*3+1],this.normals[this.indices[indices]*3+2]);
                break;
            }
        }
        return col;
    }
    public Vector3f getRayIntersection(Vector3f pos, Vector3f dir){
        Vector3f target = null;
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


            indices += 3;
            target = rayIntersectionTriangle(pointA,pointB,pointC,pos,dir);
            if(target == null){ //change to != to enable collision
                break;
            }
        }
        return target;
    }

    private Vector3f rayIntersectionTriangle(Vector3f a, Vector3f b, Vector3f c, Vector3f pos, Vector3f dir){


        float t = intersectRayTriangle(pos.x,pos.y,pos.z,dir.x,dir.y,dir.z,a.x,a.y,a.z,b.x,b.y,b.z,c.x,c.y,c.z,0.000000001f);

        if(t < 0.0f){
            return null;
        }else{
            pos.x = pos.x + dir.x*t;
            pos.y = pos.y + dir.y*t;
            pos.z = pos.z + dir.z*t;
            //return pos; <- enable collision
            return null;
        }

    }
    class Collision{
        public Vector3f position;
        public Vector3f normal;
    }
    public void loadMesh(){

        vaoID = Loader.loadToGPU(this);

    }
    public void unloadMesh(){
        Loader.unload(this);
    }



}
