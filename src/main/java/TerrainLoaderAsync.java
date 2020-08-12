package main.java;

import com.jogamp.common.nio.Buffers;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import org.joml.Vector3f;

import java.io.File;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class TerrainLoaderAsync{

    private Thread t;
    private boolean updateRequest = false;
    private boolean updated = false;
    private Vector3f lastPositionGenerated = null;
    private ComputeShader compute;
    private ExecutorService service;
    private RenderManager renderManager;


    TerrainLoaderAsync(){

        GL4 gl = GLContext.getCurrentGL().getGL4();
        renderManager = RenderManager.getInstance();
        compute = new ComputeShader(new File("src\\main\\java\\shader\\computeShader\\computeShader.cs"));
        setUpPersistentSSBO();
        /*t = new Thread(this,"TerrainGenerator");
        service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()/2);
        t.start();*/


    }

    private void setUpPersistentSSBO(){
        GL4 gl = GLContext.getCurrentGL().getGL4();
        //gl.glUseProgram(compute.program);

        int[] ssbo = new int[2];
        gl.glGenBuffers(2,ssbo,0);

        int[] indicesLookup = LookupTable.getAllIndices();
        IntBuffer indicesLookupBuffer = IntBuffer.allocate(indicesLookup.length);
        indicesLookupBuffer.put(indicesLookup);
        indicesLookupBuffer.rewind();

        float[] verticesLookup = LookupTable.getVertices();
        FloatBuffer verticesLookupBuffer = FloatBuffer.allocate(verticesLookup.length);
        verticesLookupBuffer.put(verticesLookup);
        verticesLookupBuffer.rewind();

        //indicesLookup
        gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER,ssbo[0]);
        gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER,indicesLookup.length*4,indicesLookupBuffer,gl.GL_STATIC_READ);
        gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER,1,ssbo[0]);
        gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER,0);

        //verticesLookup
        gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER,ssbo[1]);
        gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER,verticesLookup.length*4,verticesLookupBuffer,gl.GL_STATIC_READ);
        gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER,2,ssbo[1]);
        gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER,0);

    }

    public void run() {
        GL4 gl = GLContext.getCurrentGL().getGL4();
        System.out.println("updating chunks");
        //while(t.isAlive()) {
            if (updateRequest) {


                for (Planet planet : renderManager.getActivePlanets()) {

                    LinkedList<Mesh> meshes = new LinkedList<>();
                    LinkedList<Octree.Node> struct = planet.tree.searchChunk(lastPositionGenerated);
                    long start = System.currentTimeMillis();
                    for (Octree.Node node : struct) {

                        gl.glUseProgram(compute.program);
                        int[] ssbo = new int[4];
                        gl.glGenBuffers(4,ssbo,0);

                        Planet.VoxelGroup group = planet.getVoxelData(node.indexX,node.indexY,node.indexZ,node.span,node.span);
                        FloatBuffer voxelBuffer = FloatBuffer.allocate(group.data.length);
                        voxelBuffer.put(group.data);
                        voxelBuffer.rewind();

                        IntBuffer spanBuffer = IntBuffer.allocate(1);
                        spanBuffer.put(node.span);
                        spanBuffer.rewind();

                        //resolution in
                        gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER,ssbo[3]);
                        gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER,4,spanBuffer,gl.GL_STATIC_READ);
                        gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER,3,ssbo[3]);

                        //voxelData in
                        gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER,ssbo[2]);
                        gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER,(17*17*17)*4,voxelBuffer,gl.GL_STATIC_READ);
                        gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER,4,ssbo[2]);

                        //vertices out
                        gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER,ssbo[0]);
                        gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER,((36*(16*16*16))*4),null,gl.GL_DYNAMIC_DRAW);
                        gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER,5,ssbo[0]);

                        //indices out
                        gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER,ssbo[1]);
                        gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER,((15*(16*16*16))*4),null,gl.GL_DYNAMIC_DRAW);
                        gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER,6,ssbo[1]);

                        gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER,0);
                        gl.glDispatchCompute(16,16,16);

                        gl.glMemoryBarrier(gl.GL_SHADER_STORAGE_BARRIER_BIT);

                        FloatBuffer vertbuffer = FloatBuffer.allocate(36*(16*16*16));
                        gl.glGetNamedBufferSubData(ssbo[0],0,(36*(16*16*16))*4,vertbuffer);

                        IntBuffer indbuffer = IntBuffer.allocate((15*(16*16*16)));
                        gl.glGetNamedBufferSubData(ssbo[1],0,(15*(16*16*16)*4),indbuffer);

                        meshes.add(new Mesh(vertbuffer.array(), indbuffer.array(),new Vector3f(
                                node.indexX*LookupTable.CHUNKSIZE,
                                node.indexY*LookupTable.CHUNKSIZE,
                                node.indexZ*LookupTable.CHUNKSIZE)
                                )
                        );

                    }
                    long end = System.currentTimeMillis();
                    System.out.println((end-start)/1000.0f+" for adding all chunks!");
                    planet.setUpdatedMeshes(meshes);
                }
                updateRequest = false;

            }

        //multithread CPU terrain generation below
        /*while(t.isAlive()){
            if(updateRequest){
                for(Planet planet : renderManager.getActivePlanets()){
                    LinkedList<Mesh> meshes = new LinkedList<>();
                    long start = System.currentTimeMillis();
                    LinkedList<Octree.Node> struct = planet.tree.searchChunk(lastPositionGenerated);
                    long search = System.currentTimeMillis();

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

                    planet.setUpdatedMeshes(meshes);

                }



                updateRequest = false;

                //System.out.println((search-start)/1000.0f+" for getting the right chunks from octree!");
                //System.out.println((loopend-search)/1000.0f+" for getting the meshes from the planet in threads!");
                //System.out.println((dataTime)/1000.0f+"s for getting the data for the chunks! avrg: "+(((float)dataTime/(float)count)+"micros"));
                //System.out.println((meshTime)/1000.0f+"s for creating the meshes from the data! avrg: "+(((float)meshTime/(float)count)+"micros"));

            }
        }*/


    }

    public boolean requestTerrain(Vector3f position){
        if(!updateRequest){
            this.lastPositionGenerated = new Vector3f(position);
            updateRequest = true;
            run();
            return true;
        }
        return false;
    }

    public boolean hasUpdated(){
            return updated;
    }

    public Vector3f getPosition(){
        return this.lastPositionGenerated;
    }
}
