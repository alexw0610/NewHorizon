package main.java;

import org.joml.Vector3f;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class TerrainLoaderAsync implements Runnable{

    private Thread t;
    private boolean updateRequest = false;
    private boolean updated = false;
    private Vector3f position;
    private Octree tree;
    private Planet planet;
    private LinkedList<Mesh> meshes;
    private ExecutorService service;


    TerrainLoaderAsync(){
        this.position = new Vector3f(0,0,0);
        this.planet = new Planet(123,128);
        this.meshes = new LinkedList<>();
        this.tree = new Octree((short)128);
        t = new Thread(this,"TerrainGenerator");
        service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()/2);
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


                List<Callable<Mesh>> tasklist = new LinkedList<>();
                for(Octree.Node node : struct){
                    Callable<Mesh> callableTask = () -> {
                        return planet.getMesh(node.indexX,node.indexY,node.indexZ,node.span);
                    };
                    tasklist.add(callableTask);
                }


                long dataTime = 0;
                long meshTime = 0;
                long count = 0;

                try
                {
                    List<Future<Mesh>> returns = service.invokeAll(tasklist);
                    List<Future<Mesh>> temp = new LinkedList<>();
                    while(!returns.isEmpty()){
                        for(Future<Mesh> mesh : returns) {
                            if(mesh.isDone()){
                                meshes.add(mesh.get());
                                temp.add(mesh);
                            }
                        }
                        returns.removeAll(temp);
                    }


                }
                catch (InterruptedException | ExecutionException e1)
                {
                    e1.printStackTrace();
                }

                long loopend = System.currentTimeMillis();

                /*for(Octree.Node node : struct){

                    long loopstart = System.currentTimeMillis();
                    float[] data = planet.getData(node.indexX,node.indexY,node.indexZ,node.span,node.span);
                    long loopmiddle = System.currentTimeMillis();
                    meshes.add(Voxel.getMesh(data, node.span, node.indexX,node.indexY,node.indexZ));
                    long loopend = System.currentTimeMillis();
                    dataTime += loopmiddle-loopstart;
                    meshTime += loopend -loopmiddle;
                    count++;
                }*/

                updated = true;
                updateRequest = false;
                System.out.println((search-start)/1000.0f+" for getting the right chunks from octree!");
                System.out.println((loopend-search)/1000.0f+" for getting the meshes from the planet in threads!");
                //System.out.println((dataTime)/1000.0f+"s for getting the data for the chunks! avrg: "+(((float)dataTime/(float)count)+"micros"));
                //System.out.println((meshTime)/1000.0f+"s for creating the meshes from the data! avrg: "+(((float)meshTime/(float)count)+"micros"));

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
