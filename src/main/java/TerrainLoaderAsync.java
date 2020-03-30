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
                System.out.println("working");
                LinkedList<Octree.Node> struct = tree.searchChunk(position);

                meshes.clear();

                for(Octree.Node node : struct){

                    float[] data = planet.getData(node.indexX,node.indexY,node.indexZ,node.span,node.span);
                    meshes.add(Voxel.getMesh(data, node.span, node.indexX,node.indexY,node.indexZ));
                }

                updated = true;
                updateRequest = false;
                System.out.println("finished");
            }
        }


    }

    public void requestTerrain(Vector3f position){
        if(!updateRequest && !updated){
            this.position = new Vector3f(position);
            updateRequest = true;
        }

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
