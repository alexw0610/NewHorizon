package com.newhorizon;

import com.newhorizon.noise.OpenSimplexNoise;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.LinkedList;

public class Planet {

    Vector3f position = new Vector3f(1024,1024,1024);

    int seed;
    int diameter;    // diameter is in chunks
    int radius;
    boolean wireframe = false;
    public Octree tree;
    private LinkedList<Mesh> currentMeshes = new LinkedList<>();
    private LinkedList<Mesh> updatedMeshes = new LinkedList<>();
    private LinkedList<String> fullNodeIds = new LinkedList<>();

    private boolean fullNodeIdsUpdated = false;
    private boolean hasUpdated = false;

    public float atmoDensity = 0.003f;
    public Vector3f atmoColor = new Vector3f(0.1f,0.2f,0.5f);

    OpenSimplexNoise osn;


    public Planet(int seed, int diameter){

        this.seed = seed;
        this.diameter = diameter;
        this.radius = diameter/3;
        tree = new Octree((short)diameter);
        osn = new OpenSimplexNoise(this.seed);

    }

    public Mesh getMesh(int chunkX, int chunkY, int chunkZ, int span){
        VoxelGroup temp = getVoxelData(chunkX,chunkY,chunkZ,span,span);
        return Voxel.getMesh(temp.data,temp.color,span,chunkX,chunkY,chunkZ);
    }

    public VoxelGroup getVoxelDataCube(int cubeX, int cubeY, int cubeZ){

        float[] voxelData = new float[8];
        float[] voxelColor = new float[8];
        boolean isEmpty = true;

        for(int i = 0; i < 8;i++){

            Vector3f pos = new Vector3f(
                    (i%(2)),
                    ((i/((2)*(2))%(2))),
                    ((i/(2) )%(2))
            );

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

            pos.x += cubeX;
            pos.y += cubeY;
            pos.z += cubeZ;

            voxelData[i] = getDataPoint(pos);
            voxelColor[i] = 1;
            if(isEmpty && voxelData[i] > LookupTable.ISOLEVEL){
                isEmpty = false;
            }

        }

        return new VoxelGroup(voxelData,voxelColor,isEmpty);
    }

    public VoxelGroup getVoxelData(int chunkX, int chunkY, int chunkZ, int span,int resolution){


        int side = LookupTable.CHUNKSIZE;

        float[] voxelData = new float[(int)Math.pow(side+1,3)];
        float[] voxelColor = new float[(int)Math.pow(side+1,3)];

        double cube = Math.pow(side+1,3);

        float chunkPosX = chunkX*LookupTable.CHUNKSIZE;
        float chunkPosY = chunkY*LookupTable.CHUNKSIZE;
        float chunkPosZ = chunkZ*LookupTable.CHUNKSIZE;

        boolean isEmpty = true;

        for(int i = 0; i < cube;i++){

            Vector3f pos = new Vector3f(
                    (i%(side+1)),
                    ((i/((side+1)*(side+1))%(side+1))),
                    ((i/(side+1) )%(side+1))
            );


            pos = pos.mul(resolution);
            pos.x += chunkPosX;
            pos.y += chunkPosY;
            pos.z += chunkPosZ;



            voxelData[i] = getDataPoint(pos);

            if(isEmpty && voxelData[i] > LookupTable.ISOLEVEL){
                isEmpty = false;
            }

            voxelColor[i] = 1;

        }
        return new VoxelGroup(voxelData,voxelColor,isEmpty);
    }

    private float getDataPoint(Vector3f pos){
        float stretchBig = 0.0085f;
        float stretchMedium = 0.04f;
        float stretchSmall = 0.02f;
        float offset = 1231241.0f;

        float value1 = (float)osn.noise((pos.x()+offset)*stretchSmall, (pos.y()+offset)*stretchSmall,(pos.z()+offset)*stretchSmall);
        float value2 = (float)osn.noise(pos.x()*stretchSmall, pos.y()*stretchSmall,pos.z()*stretchSmall);
        float value3 = (float)osn.noise(pos.x()*stretchBig, pos.y()*stretchBig,pos.z()*stretchBig);
        float value4 = (float)osn.noise(pos.x()*stretchMedium, pos.y()*stretchMedium,pos.z()*stretchMedium);
        float region = (float)osn.noise((pos.x()+offset*2)*stretchMedium, (pos.y()+offset*2)*stretchMedium,(pos.z()+offset*2)*stretchMedium);
        float ore = (float)osn.noise((pos.x()+offset*3)*0.003, (pos.y()+offset*3)*0.003,(pos.z()+offset*3)*0.003);
        float dist = this.position.distance(pos);
        region = Math.max(region,0);

        return  value1*(6)*(region+0.1f)+
                value2*(14)*(region+0.1f)+
                value3*(8)+
                value4*(0.2f)*(region)+
                ((float)radius*(float)LookupTable.CHUNKSIZE-dist);
    }

    public void setUpdatedMesh(LinkedList<Mesh> meshes){
            synchronized (updatedMeshes){
                this.updatedMeshes = (LinkedList<Mesh>) meshes.clone();
                hasUpdated = true;
            }
    }

    public LinkedList<Mesh> getCurrentMeshes(){
        return this.currentMeshes;
    }

    public void updateMeshes(){
        if(hasUpdated){

            synchronized (fullNodeIds){
                if(fullNodeIdsUpdated){

                    synchronized(this.updatedMeshes){
                        for(Mesh mesh : this.updatedMeshes){
                            mesh.loadMesh();
                        }
                        this.currentMeshes.addAll(this.updatedMeshes);
                        this.updatedMeshes.clear();
                    }

                    synchronized (this.currentMeshes){
                        LinkedList<Mesh> tempList = new LinkedList<>();
                        for(Mesh mesh: this.currentMeshes){
                            if(!this.fullNodeIds.contains(mesh.id)){
                                mesh.unloadMesh();
                                tempList.add(mesh);
                            }
                        }
                        this.currentMeshes.removeAll(tempList);
                    }
                    fullNodeIdsUpdated = false;
                }

            }
            hasUpdated = false;
        }
    }
    public class VoxelGroup{

        float[] data;
        float[] color;
        boolean empty;
        public VoxelGroup(float[] data, float[]color, boolean empty){
            this.data = data;
            this.color = color;
            this.empty = empty;
        }
    }

    public LinkedList<Octree.Node> filterExisting(LinkedList<Octree.Node> input){

        input.removeIf(node -> this.fullNodeIds.contains(node.id));
        return input;
    }


    public void setFullNodeIds(LinkedList<Octree.Node> input){
        synchronized (fullNodeIds){
            this.fullNodeIds.clear();
            for(Octree.Node node : input){
                this.fullNodeIds.add(node.id);
            }
            this.fullNodeIdsUpdated = true;
        }

    }

}
