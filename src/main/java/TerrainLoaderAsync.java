package main.java;

import org.joml.Vector3f;

import java.util.LinkedList;

public class TerrainLoaderAsync implements Runnable{

    private Thread t;
    private boolean updateRequest = false;
    private boolean updated = false;
    private Vector3f position;
    private Octree tree;
    private Planet planet;
    private LinkedList<Mesh> meshes;


    TerrainLoaderAsync(){
        this.position = new Vector3f(0,0,0);
        this.planet = new Planet(123,1024);
        this.meshes = new LinkedList<>();
        this.tree = new Octree((short)1024);
        t = new Thread(this,"TerrainGenerator");
        t.start();

    }

    @Override
    public void run() {
        while(t.isAlive()){
            if(updateRequest && !updated){
                System.out.println("...");
                long start = System.currentTimeMillis();
                LinkedList<Octree.Node> struct = tree.searchChunk(position);
                long search = System.currentTimeMillis();
                meshes.clear();

                long dataTime = 0;
                long meshTime = 0;
                for(Octree.Node node : struct){
                    long loopstart = System.currentTimeMillis();
                    float[] data = planet.getData(node.indexX,node.indexY,node.indexZ,node.span,node.span);
                    long loopmiddle = System.currentTimeMillis();
                    meshes.add(Voxel.getMesh(data, node.span, node.indexX,node.indexY,node.indexZ));
                    long loopend = System.currentTimeMillis();
                    dataTime += loopmiddle-loopstart;
                    meshTime += loopend -loopmiddle;
                }

                updated = true;
                updateRequest = false;
                System.out.println((search-start)/1000.0f+" for getting the right chunks from octree!");
                System.out.println((dataTime)/1000.0f+" for getting the data for the chunks!");
                System.out.println((meshTime)/1000.0f+" for creating the meshes from the data!");

            }
        }


    }

    public boolean requestTerrain(Vector3f position){
        if(!updateRequest && !updated){
            this.position = new Vector3f(position);
            updateRequest = true;
            return true;
        }
        return false;
    }
    public LinkedList<Mesh> getRequestedTerrain(){
        updated = false;
        LinkedList<Mesh> temp = (LinkedList)meshes.clone();
        return temp;

    }

    public boolean hasUpdated(){
            return updated;
    }

    public Vector3f getPosition(){
        return this.position;
    }
}
