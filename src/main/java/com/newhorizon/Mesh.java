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
    Mesh(Planet.VoxelGroup voxelGroup, Vector3i position){

        float[] voxelData = voxelGroup.data;
        float[] data = new float[8];

        float isolevel = LookupTable.ISOLEVEL;
        short square = 2;
        short dim = 4;
        int start = 0;

         /*

            0   0,0,0
            1   1,0,0
            2   0,0,1
            3   1,0,1
            4   0,1,0
            5   1,1,0
            6   0,1,1
            7   1,1,1

             */

        data[3] = voxelData[start];
        data[2] = voxelData[start+1];
        data[1] = voxelData[start+1+dim];
        data[0] = voxelData[start+dim];

        data[7] = voxelData[start+square];
        data[6] = voxelData[start+square+1];
        data[5] = voxelData[start+square+1+dim];
        data[4] = voxelData[start+square+dim];

        String binary = (data[7] >isolevel ? 0: 1)+""
                +(data[6] >isolevel ? 0: 1)+""
                +(data[5] >isolevel ? 0: 1)+""
                +(data[4] >isolevel ? 0: 1)+""
                +(data[3] >isolevel ? 0: 1)+""
                +(data[2] >isolevel ? 0: 1)+""
                +(data[1] >isolevel ? 0: 1)+""
                +(data[0] >isolevel ? 0: 1);

        int index = Integer.parseInt(binary, 2);
        int[] indices = LookupTable.getIndices(index);
        float[] vertices = LookupTable.getVertices();

        if(indices.length > 0){

            /*for(int i=0; i < indices.length;i++){

                if(indices[i] == 0){
                    vertices[indices[i]*3] =  getInterpCoord(data[0],data[1],isolevel);

                }else if(indices[i] == 1){
                    vertices[indices[i]*3+2] =  getInterpCoord(data[2],data[1],isolevel);

                }else if(indices[i] == 2){
                    vertices[indices[i]*3] =  getInterpCoord(data[3],data[2],isolevel);

                }else if(indices[i] == 3){
                    vertices[indices[i]*3+2] =  getInterpCoord(data[3],data[0],isolevel);

                }else if(indices[i] == 4){
                    vertices[indices[i]*3] =  getInterpCoord(data[4],data[5],isolevel);

                }else if(indices[i] == 5){
                    vertices[indices[i]*3+2] =  getInterpCoord(data[6],data[5],isolevel);

                }else if(indices[i] == 6){
                    vertices[indices[i]*3] =  getInterpCoord(data[7],data[6],isolevel);

                }else if(indices[i] == 7){
                    vertices[indices[i]*3+2] = getInterpCoord(data[7],data[4],isolevel);

                }else if(indices[i] == 8){
                    vertices[indices[i]*3+1] =  getInterpCoord(data[0],data[4],isolevel);

                }else if(indices[i] == 9){
                    vertices[indices[i]*3+1] =  getInterpCoord(data[1],data[5],isolevel);

                }else if(indices[i] == 10){
                    vertices[indices[i]*3+1] =  getInterpCoord(data[2],data[6],isolevel);

                }else if(indices[i] == 11){
                    vertices[indices[i]*3+1] =  getInterpCoord(data[3],data[7],isolevel);

                }

            }*/
        }
        for(int i = 0; i < vertices.length;i++){
            vertices[i] += 0.5f;
        }
        this.vertices = vertices;
        this.indices = indices;
        this.normals = createNormals(this.vertices,this.indices);
        this.position = new Vector3f(position.x,position.y,position.z);
        colors = new float[vertices.length/3];
        Arrays.fill(colors,(short)0);



    }
    private static float getInterpCoord(float a, float b, float iso){

        float ad = iso-a;
        float bd = iso-b;
        float adAbs = Math.abs(ad);
        float range = adAbs+Math.abs(bd);
        return (adAbs*(1.0f/range))-0.5f;

    }
    private static float[] createNormals(float[] vertices, int[] indices){

        float[] normals = new float[vertices.length];
        for(int i = 0; i < indices.length/3;i++){
            Vector3f pointA = new Vector3f(vertices[(indices[i*3]*3)],vertices[(indices[i*3]*3)+1],vertices[(indices[i*3]*3)+2]);
            Vector3f pointB = new Vector3f(vertices[(indices[i*3+1]*3)],vertices[(indices[+i*3+1]*3)+1],vertices[(indices[i*3+1]*3)+2]);
            Vector3f pointC = new Vector3f(vertices[(indices[i*3+2]*3)],vertices[+(indices[i*3+2]*3)+1],vertices[(indices[i*3+2]*3)+2]);

            Vector3f e1 = new Vector3f(pointA).sub(pointB);
            Vector3f e2 = new Vector3f(pointA).sub(pointC);
            Vector3f normal = e1.cross(e2);
            normal = normal.normalize();


            normals[(indices[i*3]*3)] = (normal.x);
            normals[(indices[i*3]*3)+1] = (normal.y);
            normals[(indices[i*3]*3)+2] = (normal.z);

            normals[(indices[i*3+1]*3)] = (normal.x);
            normals[(indices[i*3+1]*3)+1] = (normal.y);
            normals[(indices[i*3+1]*3)+2] = (normal.z);

            normals[(indices[i*3+2]*3)] = (normal.x);
            normals[(indices[i*3+2]*3)+1] = (normal.y);
            normals[(indices[i*3+2]*3)+2] = (normal.z);

        }
        return normals;
    }



    private static class Triangle{
        Vector3f a;
        Vector3f b;
        Vector3f c;
        public Triangle(Vector3f a, Vector3f b, Vector3f c){
            this.a = a;
            this.b = b;
            this.c = c;
        }
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


        //float t = intersectRayTriangle(pos.x,pos.y,pos.z,dir.x,dir.y,dir.z,a.x,a.y,a.z,b.x,b.y,b.z,c.x,c.y,c.z,0.0000000001f);
        return testLineSegmentTriangle(pos,new Vector3f(pos).add(dir),a,b,c,0.0000000001f);

        /*if(t < 0.0f){
            return null;

        }else{
            //pos.x = pos.x + dir.x*t;
            //pos.y = pos.y + dir.y*t;
            //pos.z = pos.z + dir.z*t;
            return t;

        }*/

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
