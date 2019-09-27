package main.java;



import java.lang.Math;

import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class Voxel {

    List<Mesh> meshList = new LinkedList<>();
    float[] voxelset;
    int dim;

    public Voxel(int dim, float isolevel){

        this.dim = dim;
        voxelset = new float[(int)Math.pow(dim,3)];
        makeRandom();
        createMeshes(isolevel);


    }

    public void makeRandom(){

        PerlinNoise3d perlin = new PerlinNoise3d(5);

        for(int i = 0; i < (Math.pow(dim,3));i++){

            Vector3f pos = new Vector3f((i%dim),((i/dim)%dim),((i/(dim*dim))%dim));
            //Vector3f middle = new Vector3f(dim/2,dim/2,dim/2);
            //float value = Vector3f.distance(pos.x,pos.y,pos.z,middle.x,middle.y,middle.z);
            float value = perlin.getValue(new Vector3f(pos.x/4,pos.y/4,pos.z/4));
            //value = (float)Math.random();
            voxelset[i] = value;

        }

    }
    public float[] getValues(Vector3i pos){

        float[] data = new float[8];

        int start = pos.x + pos.z*(dim)+ pos.y*((int)Math.pow(dim,2));

        int square = (int)Math.pow(dim,2);


        data[3] = voxelset[start];
        data[2] = voxelset[start+1];
        data[1] = voxelset[start+1+dim];
        data[0] = voxelset[start+dim];

        data[7] = voxelset[start+square];
        data[6] = voxelset[start+square+1];
        data[5] = voxelset[start+square+1+dim];
        data[4] = voxelset[start+square+dim];

        return data;
    }
    private void createMeshes(float isolevel){


        for(int i = 0; i <(Math.pow(dim-1,3)); i++){


            Vector3i position = new Vector3i(i%(dim-1),(i/((int)Math.pow(dim-1,2))%(dim-1)),(i/(dim-1))%(dim-1));
            Vector3f positionf = new Vector3f(position.x,position.y,position.z);

            float[] data = getValues(position);

            String binary = (data[7] >isolevel ? 0: 1)+""
                    +(data[6] >isolevel ? 0: 1)+""
                    +(data[5] >isolevel ? 0: 1)+""
                    +(data[4] >isolevel ? 0: 1)+""
                    +(data[3] >isolevel ? 0: 1)+""
                    +(data[2] >isolevel ? 0: 1)+""
                    +(data[1] >isolevel ? 0: 1)+""
                    +(data[0] >isolevel ? 0: 1);

            int index = Integer.parseInt(binary, 2);

            short[] indices = LookupTable.getIndices(index);
            float[] vertices = LookupTable.getVertices();
            vertices = interpVerts(vertices,indices,data,isolevel);
            Object tempValues = optimizeVertices(vertices,indices);

            vertices = tempValues.vertices;
            indices = tempValues.indices;

            float[] normals = createNormals(vertices,indices);

            Mesh temp = new Mesh(vertices,indices,normals,positionf);
            this.meshList.add(temp);

        }

    }


    private Object optimizeVertices(float[] vertices, short[] indices){

        LinkedList<Float> verticesList = new LinkedList<>();
        LinkedList<Short> indicesList = new LinkedList<>();
        LinkedList<Short> idc = new LinkedList<>();

        short count = 0;
        for(int i = 0; i < indices.length;i++){
            short value = indices[i];
            if(!indicesList.contains(value)){
                indicesList.add(value);

                verticesList.add(vertices[value*3]);
                verticesList.add(vertices[value*3+1]);
                verticesList.add(vertices[value*3+2]);
                idc.add(count);
                count++;
            }else{
                idc.add((short)indicesList.indexOf(value));
            }
        }

        Object temp = new Object();
        float[] tempVertices = new float[verticesList.size()];
        short[] tempIndices = new short[idc.size()];
        count = 0;

        for(Float value : verticesList){
            tempVertices[count] = value;
            count++;
        }
        count = 0;

        for(Short value : idc){
            tempIndices[count] = value;
            count++;
        }
        temp.vertices = tempVertices;
        temp.indices = tempIndices;

        return temp;
    }

    class Object{
        public float[] vertices;
        public short[] indices;
    }

    private float[] interpVerts(float[] vertices, short[] indices, float[] data,float isolevel){

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

    private float getInterpCoord(float a, float b, float iso){

        float ad = iso-a;
        float bd = iso-b;
        float range = Math.abs(ad)+Math.abs(bd);
        return (Math.abs(ad)*(1.0f/range))-0.5f;

    }

    private float[] createNormals(float[] vertices, short[] indices){

        float[] normals = new float[vertices.length];

        for(int i = 0; i < indices.length;i = i+3) {

            Vector3f v1 = new Vector3f(vertices[indices[i  ]*3],vertices[indices[i  ]*3+1],vertices[indices[i  ]*3+2]);
            Vector3f v2 = new Vector3f(vertices[indices[i+1]*3],vertices[indices[i+1]*3+1],vertices[indices[i+1]*3+2]);
            Vector3f v3 = new Vector3f(vertices[indices[i+2]*3],vertices[indices[i+2]*3+1],vertices[indices[i+2]*3+2]);

            Vector3f e1 = new Vector3f(v1).sub(v2);
            Vector3f e2 = new Vector3f(v1).sub(v3);

            Vector3f normal = e1.cross(e2);
            normal.normalize();

            normals[indices[i  ]*3] = normal.x;
            normals[indices[i  ]*3+1] = normal.y;
            normals[indices[i  ]*3+2] = normal.z;

            normals[indices[i+1]*3] = normal.x;
            normals[indices[i+1]*3+1] = normal.y;
            normals[indices[i+1]*3+2] = normal.z;

            normals[indices[i+2]*3] = normal.x;
            normals[indices[i+2]*3+1] = normal.y;
            normals[indices[i+2]*3+2] = normal.z;



        }



        return normals;

    }

    private float[] createSmoothNormals(float[] vertices, short[] indices){

        float[] normals = new float[vertices.length];

        for(int i = 0; i < vertices.length/3;i = i+3){
            //per Vertex
            short vertNumber = (short)(i/3);

            LinkedList<Vector3f> tempNormals = new LinkedList<>();

            for(int x = 0;x < indices.length;x = x+3) {



                if(indices[x] == vertNumber || indices[x+1] == vertNumber || indices[x+2] == vertNumber){

                    Vector3f v1 = new Vector3f(vertices[indices[x  ]*3],vertices[indices[x  ]*3+1],vertices[indices[x  ]*3+2]);
                    Vector3f v2 = new Vector3f(vertices[indices[x+1]*3],vertices[indices[x+1]*3+1],vertices[indices[x+1]*3+2]);
                    Vector3f v3 = new Vector3f(vertices[indices[x+2]*3],vertices[indices[x+2]*3+1],vertices[indices[x+2]*3+2]);

                    Vector3f e1 = new Vector3f(v1).sub(v2);
                    Vector3f e2 = new Vector3f(v1).sub(v3);

                    Vector3f normal = e1.cross(e2);
                    normal.normalize();
                    tempNormals.add(normal);

                }


            }

            Vector3f normal = new Vector3f(0,0,0);
            for(Vector3f temp : tempNormals){

                normal = normal.add(temp);

            }

            normal.div(tempNormals.size());
            normal.normalize();

            normals[vertNumber*3  ] = normal.x;
            normals[vertNumber*3+1] = normal.y;
            normals[vertNumber*3+2] = normal.z;
        }

        return normals;


    }

    public List<Mesh> getMeshes(){

        return this.meshList;
    }

}
