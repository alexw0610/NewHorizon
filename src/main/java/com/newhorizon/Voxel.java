package com.newhorizon;


import java.lang.Math;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.*;


public class Voxel {

    /*
    This method calculates mesh data on the CPU rather than with a compute shader. Used for collision detection.
     */
    public static Mesh getMesh(float[] voxelData,float[] voxelColor, int resolution, int chunkX, int chunkY, int chunkZ){
        Mesh temp = createMeshes(voxelData,voxelColor,(int)Math.cbrt(voxelData.length),0.5f,resolution);
        temp.position = new Vector3f(chunkX*LookupTable.CHUNKSIZE,chunkY*LookupTable.CHUNKSIZE,chunkZ*LookupTable.CHUNKSIZE);
        return temp;

    }

    private static Mesh createMeshes(float[] voxelData,float[] voxelColor, int dim, float isolevel, int resolution){

        List<Mesh> meshList = new LinkedList<>();

        double cube = Math.pow(dim,3);
        int square = (int)Math.pow(dim-1,2);
        for(int i = 0; i < cube; i++){


            Vector3i position = new Vector3i(i%(dim-1),(i/(square)%(dim-1)),(i/(dim-1))%(dim-1));
            Vector3f positionf = new Vector3f(position.x,position.y,position.z);
            positionf.add(new Vector3f(0.5f,0.5f,0.5f));
            positionf.mul(resolution);


            float[] data = getValues(position, dim, voxelData);

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

                vertices = interpVerts(vertices,indices,data,isolevel);
                vertices = scaleVertices(resolution, vertices);

                Mesh temp = new Mesh(vertices,indices,positionf);
                meshList.add(temp);

            }
        }
        return mergeMeshes(meshList);

    }

    private static float[] scaleVertices(int resolution, float[] vertices){

        for(int i = 0; i<vertices.length; i++){
            vertices[i] = vertices[i]* (float)resolution;
        }

        return vertices;

    }

    private static float[] getValues(Vector3i pos, int dim, float[] voxelData){

        float[] data = new float[8];
        int square = (int)Math.pow(dim,2);
        int start = pos.x + pos.z*(dim)+ pos.y*square;


        data[3] = voxelData[start];
        data[2] = voxelData[start+1];
        data[1] = voxelData[start+1+dim];
        data[0] = voxelData[start+dim];

        data[7] = voxelData[start+square];
        data[6] = voxelData[start+square+1];
        data[5] = voxelData[start+square+1+dim];
        data[4] = voxelData[start+square+dim];

        return data;
    }


    private static Mesh mergeMeshes(List<Mesh> meshList){
        LinkedList<Float> vertices = new LinkedList<>();
        LinkedList<Integer> indices = new LinkedList<>();
        LinkedList<Float> colors = new LinkedList<>();

        int indicesOffset = 0;
        int size = meshList.size();
        for(int i = 0; i < size;i++){

            Mesh mesh = meshList.get(i);
            int count = 0;
            int VertLength = mesh.vertices.length/3;
            for(int vert = 0; vert < VertLength;vert++){
                vertices.add(mesh.vertices[(vert)*3  ]+mesh.position.x);
                vertices.add(mesh.vertices[(vert)*3+1]+mesh.position.y);
                vertices.add(mesh.vertices[(vert)*3+2]+mesh.position.z);
                colors.add(mesh.colors[vert]);
                count++;
            }

            int indLength = mesh.indices.length;
            for(int idc = 0; idc < indLength;idc++){
                indices.add(mesh.indices[idc]+indicesOffset);
            }
            indicesOffset = indicesOffset + count;

        }

        float[] verticesArr = new float[vertices.size()];
        int[] indicesArr = new int[indices.size()];
        float[] colorsArr = new float[colors.size()];
        int count = 0;
        for(float value : vertices){
            verticesArr[count] = value;
            count++;
        }
        count = 0;

        for(int value : indices){
            indicesArr[count] = value;
            count++;
        }
        count = 0;

        for(float value : colors){
            colorsArr[count] = value;
            count++;
        }


        Mesh chunk = new Mesh(verticesArr,indicesArr,new Vector3f(0,0,0));
        chunk.setNormals(createNormals(chunk.vertices,chunk.indices));
        return chunk;

    }


    static class Object{
        public float[] vertices;
        public int[] indices;
        public float[] colors;
    }

    private static float[] interpVerts(float[] vertices, int[] indices, float[] data,float isolevel){

        for(int i=0; i < indices.length;i++){

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

        }
        return vertices;
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
        for(int vertPos = 0; vertPos < vertices.length;vertPos += 3){

            LinkedList<Triangle> tris = new LinkedList<>();

            for(int indPos = 0; indPos < indices.length;indPos += 3){
                if(indices[indPos] == vertPos/3 || indices[indPos+1] == vertPos/3|| indices[indPos+2] == vertPos/3){

                    Vector3f a = new Vector3f(vertices[indices[indPos  ]*3],vertices[indices[indPos  ]*3+1],vertices[indices[indPos  ]*3+2]);
                    Vector3f b = new Vector3f(vertices[indices[indPos+1]*3],vertices[indices[indPos+1]*3+1],vertices[indices[indPos+1]*3+2]);
                    Vector3f c = new Vector3f(vertices[indices[indPos+2]*3],vertices[indices[indPos+2]*3+1],vertices[indices[indPos+2]*3+2]);

                    tris.add(new Triangle(a,b,c));
                }
            }
            LinkedList<Vector3f> triNormals = new LinkedList<>();


            for(Triangle tri: tris){

                Vector3f e1 = new Vector3f(tri.a).sub(tri.b);
                Vector3f e2 = new Vector3f(tri.a).sub(tri.c);
                Vector3f normal = e1.cross(e2);


                triNormals.add(normal);

            }
            Vector3f targetNormal = new Vector3f(0,0,0);
            for(Vector3f normal : triNormals){
                targetNormal.add(normal);
            }
            targetNormal.normalize();

            normals[vertPos  ] = targetNormal.x;
            normals[vertPos+1] = targetNormal.y;
            normals[vertPos+2] = targetNormal.z;


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



}
