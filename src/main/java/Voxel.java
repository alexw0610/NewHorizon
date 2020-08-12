package main.java;


import java.lang.Math;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.*;


public class Voxel {

    public static Mesh getMesh(float[] voxelData,float[] voxelColor, int resolution, int chunkX, int chunkY, int chunkZ){
        Mesh temp = createMeshes(voxelData,voxelColor,(int)Math.cbrt(voxelData.length),0.5f,resolution);
        //entry point for compute shader (voxelData, indicesLookup, verticesLookup, resolution)
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
            //float[] colors = getColor(position, dim, voxelColor);

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
                //colors = interpColors(vertices,indices,colors);
                vertices = scaleVertices(resolution, vertices);
                //Object tempValues = optimizeVertices(vertices,indices,colors);
                //vertices = tempValues.vertices;
                //indices = tempValues.indices;
                //colors = tempValues.colors;



                //Mesh temp = new Mesh(vertices,colors,indices,positionf);
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

    private static float[] getColor(Vector3i pos, int dim, float[] voxelColor){
        float[] data = new float[8];
        int square = (int)Math.pow(dim,2);
        int start = pos.x + pos.z*dim+ pos.y*square;

        data[3] = voxelColor[start];
        data[2] = voxelColor[start+1];
        data[1] = voxelColor[start+1+dim];
        data[0] = voxelColor[start+dim];

        data[7] = voxelColor[start+square];
        data[6] = voxelColor[start+square+1];
        data[5] = voxelColor[start+square+1+dim];
        data[4] = voxelColor[start+square+dim];

        return data;
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
        //TODO: Optimize
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


        //Mesh chunk = optimizeMesh(new Mesh(verticesArr,colorsArr,indicesArr,new Vector3f(0,0,0)));
        Mesh chunk = new Mesh(verticesArr,indicesArr,new Vector3f(0,0,0));
        chunk.setNormals(createNormals(chunk.vertices,chunk.indices));
        //chunk.loadMesh();
        return chunk;

    }

    /*private static Mesh optimizeMesh(Mesh mesh){

       LinkedList<Vector3f> visited = new LinkedList<>();
       LinkedList<Float> colors = new LinkedList<>();

       for(int i = 0; i<mesh.indices.length;i++){

           float x = mesh.vertices[mesh.indices[i]*3];
           float y = mesh.vertices[mesh.indices[i]*3+1];
           float z = mesh.vertices[mesh.indices[i]*3+2];
           boolean found = false;

           for(Vector3f tempvert: visited){
               if(tempvert.x == x && tempvert.y == y && tempvert.z == z){
                   mesh.indices[i] = visited.indexOf(tempvert);
                   found = true;
                   break;
               }
           }
           if(!found){
               visited.add(new Vector3f(x,y,z));
               colors.add(mesh.colors[mesh.indices[i]]);
               mesh.indices[i] = visited.size()-1;

           }

       }
       float[] newVertices = new float[visited.size()*3];
       int count = 0;
       for(Vector3f temp: visited){
            newVertices[count] = temp.x;
            count++;
            newVertices[count] = temp.y;
            count++;
            newVertices[count] = temp.z;
            count++;
       }
       float[] newColors = new float[colors.size()];
       count = 0;
       for(float color: colors){
           newColors[count] = color;
           count++;

       }
       mesh.vertices = newVertices;
       mesh.colors = newColors;

       return mesh;
    }*/


    /*private static Object optimizeVertices(float[] vertices, int[] indices, float[] colors){

        LinkedList<Float> verticesList = new LinkedList<>();
        LinkedList<Integer> indicesList = new LinkedList<>();
        LinkedList<Float> colorsList = new LinkedList<>();
        LinkedList<Integer> idc = new LinkedList<>();

        int count = 0;
        for(int i = 0; i < indices.length;i++){
            int value = indices[i];
            if(!indicesList.contains(value)){
                indicesList.add(value);

                verticesList.add(vertices[value*3]);
                verticesList.add(vertices[value*3+1]);
                verticesList.add(vertices[value*3+2]);
                idc.add(count);
                colorsList.add(colors[value]);
                count++;
            }else{
                idc.add(indicesList.indexOf(value));
            }
        }

        Object temp = new Object();
        float[] tempVertices = new float[verticesList.size()];
        int[] tempIndices = new int[idc.size()];
        float[] tempColors = new float[colorsList.size()];
        count = 0;

        for(float value : verticesList){
            tempVertices[count] = value;
            count++;
        }
        count = 0;

        for(int value : idc){
            tempIndices[count] = value;
            count++;
        }
        count = 0;

        for(float color : colorsList){
            tempColors[count] = color;
            count++;
        }

        temp.vertices = tempVertices;
        temp.indices = tempIndices;
        temp.colors = tempColors;


        return temp;
    }*/

    static class Object{
        public float[] vertices;
        public int[] indices;
        public float[] colors;
    }

    private static float[] interpVerts(float[] vertices, int[] indices, float[] data,float isolevel){

        //#TODO: Optimize


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

    /*private static float[] interpColors(float[] vertices,int[] indices, float[] col){

        float[] colors = new float[12];
        for(int i=0; i < indices.length;i++){

            if(indices[i] == 0){
                colors[indices[i]] = vertices[indices[i]*3] > 0? col[1] : col[0];

            }else if(indices[i] == 1){
                colors[indices[i]] = vertices[indices[i]*3] > 0? col[1] : col[2];


            }else if(indices[i] == 2){
                colors[indices[i]] = vertices[indices[i]*3] > 0? col[2] : col[3];

            }else if(indices[i] == 3){
                colors[indices[i]] = vertices[indices[i]*3] > 0? col[0] : col[3];

            }else if(indices[i] == 4){
                colors[indices[i]] = vertices[indices[i]*3] > 0? col[5] : col[4];

            }else if(indices[i] == 5){
                colors[indices[i]] = vertices[indices[i]*3] > 0? col[5] : col[6];

            }else if(indices[i] == 6){
                colors[indices[i]] = vertices[indices[i]*3] > 0? col[6] : col[7];

            }else if(indices[i] == 7){
                colors[indices[i]] = vertices[indices[i]*3] > 0? col[4] : col[7];

            }else if(indices[i] == 8){
                colors[indices[i]] = vertices[indices[i]*3] > 0? col[4] : col[0];

            }else if(indices[i] == 9){
                colors[indices[i]] = vertices[indices[i]*3] > 0? col[5] : col[1];

            }else if(indices[i] == 10){
                colors[indices[i]] = vertices[indices[i]*3] > 0? col[6] : col[2];

            }else if(indices[i] == 11){
                colors[indices[i]] = vertices[indices[i]*3] > 0? col[7] : col[3];

            }

        }

        return colors;
    }*/

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
