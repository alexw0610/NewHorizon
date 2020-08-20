package main.java;

import main.java.noise.OpenSimplexNoise;
import org.joml.Vector3f;

import java.util.LinkedList;

public class Planet {

    int seed;
    int diameter;    // diameter is in chunks
    int radius;

    public Octree tree;
    private LinkedList<Mesh> currentMeshes = new LinkedList<>();
    private LinkedList<Mesh> updatedMeshes = new LinkedList<>();
    private LinkedList<String> currentNodeIds = new LinkedList<>();
    private LinkedList<String> fullNodeIds = new LinkedList<>();

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
    public VoxelGroup getVoxelData(int chunkX, int chunkY, int chunkZ, int span,int resolution){


        int side = LookupTable.CHUNKSIZE;
        float[] voxelData = new float[(int)Math.pow(side+1,3)];
        float[] voxelColor = new float[(int)Math.pow(side+1,3)];
        double cube = Math.pow(side+1,3);
        float chunkPosX = chunkX*LookupTable.CHUNKSIZE;
        float chunkPosY = chunkY*LookupTable.CHUNKSIZE;
        float chunkPosZ = chunkZ*LookupTable.CHUNKSIZE;
        Vector3f mid = new Vector3f((diameter*LookupTable.CHUNKSIZE)/2,(diameter*LookupTable.CHUNKSIZE)/2,(diameter*LookupTable.CHUNKSIZE)/2);

        float stretchMedium = 0.015f;
        float stretchSmall = 0.05f;
        float offset = 1231241.0f;

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

            float value1 = (float)osn.noise((pos.x+offset)*stretchSmall, (pos.y+offset)*stretchSmall,(pos.z+offset)*stretchSmall);
            float value2 = (float)osn.noise(pos.x*stretchSmall, pos.y*stretchSmall,pos.z*stretchSmall);
            float region = (float)osn.noise((pos.x+offset*2)*stretchMedium, (pos.y+offset*2)*stretchMedium,(pos.z+offset*2)*stretchMedium);
            float ore = (float)osn.noise((pos.x+offset*3)*0.003, (pos.y+offset*3)*0.003,(pos.z+offset*3)*0.003);
            float dist = mid.distance(pos);
            region = Math.max(region,0);

            voxelData[i] = value1*(3)*region+((float)radius*(float)LookupTable.CHUNKSIZE-dist)
                    +value2*(5)*region;
            //voxelData[i] = pos.x < 3 ? pos.y < 3 ? pos.z < 3 ? 1 : 0 :0:0;
            //voxelColor[i] = ore < 0.0f ? (short)2 : 1 ;//0R 1G 2B

            voxelColor[i] = ore;
        }
        return new VoxelGroup(voxelData,voxelColor);
    }

    public void setUpdatedMeshes(LinkedList<Mesh> meshes, LinkedList<Octree.Node> fullNodeIds){
        if(!hasUpdated){
            synchronized (updatedMeshes){
                synchronized (this.fullNodeIds){
                    this.updatedMeshes = (LinkedList<Mesh>)meshes.clone();
                    this.fullNodeIds.clear();
                    for(Octree.Node node : fullNodeIds){
                        this.fullNodeIds.add(node.id);
                    }


                    hasUpdated = true;
                }

            }
        }

    }

    public LinkedList<Mesh> getCurrentMeshes(){
        return this.currentMeshes;
    }

    public void updateMeshes(){
        if(hasUpdated){
            long start = System.currentTimeMillis();
            if(!currentMeshes.isEmpty()){
                LinkedList<Mesh> tempList = new LinkedList<>();
                for(Mesh mesh: this.currentMeshes){
                    if(!this.fullNodeIds.contains(mesh.id)){
                        mesh.unloadMesh();
                        tempList.add(mesh);
                    }
                }
                this.currentMeshes.removeAll(tempList);

            }
            //this.currentMeshes = (LinkedList<Mesh>) this.updatedMeshes.clone();
            for(Mesh mesh : this.updatedMeshes){
                mesh.loadMesh();
                this.currentMeshes.add(mesh);
            }
            long end = System.currentTimeMillis();
            System.out.println((end - start) / 1000.0f + " for updating all meshes! "+ Thread.currentThread());
            hasUpdated = false;
        }
    }
    public class VoxelGroup{

        float[] data;
        float[] color;

        public VoxelGroup(float[] data, float[]color){
            this.data = data;
            this.color = color;
        }
    }

    public LinkedList<Octree.Node> filterExisting(LinkedList<Octree.Node> input){
        input.removeIf(node -> this.currentNodeIds.contains(node.id));
        return input;
    }

    public void setCurrentNodeIds(LinkedList<Octree.Node> input){
        this.currentNodeIds.clear();
        for(Octree.Node node : input){
            this.currentNodeIds.add(node.id);
        }
    }


}
