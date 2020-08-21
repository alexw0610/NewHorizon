package main.java;

import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import org.joml.Vector3f;


import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.*;

public class TerrainLoaderAsync implements Runnable{

    private Thread t;
    private boolean updateRequest = false;
    private boolean updated = false;
    private Vector3f lastPositionGenerated = new Vector3f(0,0,0);
    private ComputeShader compute;
    private ComputeShader repack;
    private ExecutorService service;
    private RenderManager renderManager;
    private boolean firstTime = true;
    private GL4 gl;


    TerrainLoaderAsync(){

        renderManager = RenderManager.getInstance();
        t = new Thread(this,"TerrainGenerator");
        t.start();
        System.out.println("terrainloader inited");

    }

    private void setUpPersistentSSBO(){

        gl.glUseProgram(compute.program);

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
    @Override
    public void run() {
        if(firstTime){
            GLContext context = RenderManager.getInstance().getContext();
            while(context == null){
                context = RenderManager.getInstance().getContext();
                System.out.println("no context found");

            }
            System.out.println("found context");
            System.out.println(context.isCreated()+" "+context.isCurrent());
            context.makeCurrent();
            System.out.println(context.isCreated()+" "+context.isCurrent());
            gl = context.getGL().getGL4();
            compute = new ComputeShader(new File("src\\main\\java\\shader\\computeShader\\computeShader.cs"));
            repack = new ComputeShader(new File("src\\main\\java\\shader\\computeShader\\repackShader.cs"));
            //setUpPersistentSSBO();
            firstTime = false;
        }

        setUpPersistentSSBO();
        System.out.println("running compute thread");
        while(t.isAlive()) {
            if (updateRequest) {

                System.out.println("update handling "+ Thread.currentThread());
                for (Planet planet : renderManager.getActivePlanets()) {
                    long start = System.currentTimeMillis();

                    LinkedList<Octree.Node> structOriginal = planet.tree.searchChunk(lastPositionGenerated);
                    System.out.println(structOriginal.get(0).id);
                    LinkedList<Octree.Node>struct = planet.filterExisting((LinkedList<Octree.Node>) structOriginal.clone());


                    planet.setFullNodeIds(structOriginal);//List of all chunks that are needed.
                    planet.setCurrentNodeIds(struct);//List of all the *new chunks* that will be added.

                    for (Octree.Node node : struct) {

                        Planet.VoxelGroup group = planet.getVoxelData(node.indexX, node.indexY, node.indexZ, node.span, node.span);

                        if(!group.empty) {

                            gl.glUseProgram(compute.program);
                            int[] ssbo = new int[10];
                            gl.glGenBuffers(10, ssbo, 0);

                            FloatBuffer voxelBuffer = FloatBuffer.allocate(group.data.length);
                            voxelBuffer.put(group.data);
                            voxelBuffer.rewind();

                            IntBuffer spanBuffer = IntBuffer.allocate(1);
                            spanBuffer.put(node.span);
                            spanBuffer.rewind();

                            //resolution in
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, ssbo[3]);
                            gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER, 4, spanBuffer, gl.GL_STATIC_READ);
                            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 3, ssbo[3]);

                            //voxelData in
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, ssbo[2]);
                            gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER, (17 * 17 * 17) * 4, voxelBuffer, gl.GL_STATIC_READ);
                            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 4, ssbo[2]);

                            //vertices out
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, ssbo[0]);
                            gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER, ((36 * (16 * 16 * 16)) * 4), null, gl.GL_DYNAMIC_DRAW);
                            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 5, ssbo[0]);

                            //indices out
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, ssbo[1]);
                            gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER, ((15 * (16 * 16 * 16)) * 4), null, gl.GL_DYNAMIC_DRAW);
                            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 6, ssbo[1]);

                            //normals out
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, ssbo[9]);
                            gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER, ((36 * (16 * 16 * 16)) * 4), null, gl.GL_DYNAMIC_DRAW);
                            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 7, ssbo[9]);

                            //count out
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, ssbo[7]);
                            gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER, (16 * 16 * 16) * 4, null, gl.GL_DYNAMIC_DRAW);
                            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 8, ssbo[7]);
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, 0);


                            gl.glDispatchCompute(16, 16, 16);


                            gl.glMemoryBarrier(gl.GL_SHADER_STORAGE_BARRIER_BIT);

                            IntBuffer indcount = IntBuffer.allocate(16 * 16 * 16);
                            gl.glGetNamedBufferSubData(ssbo[7], 0, (16 * 16 * 16) * 4, indcount);

                            int indSize = 0;
                            int verSize = 0;
                            int[] indCountArr = indcount.array();

                            for (int i = 0; i < indCountArr.length; i++) {
                                if (indCountArr[i] > 0) {
                                    indSize += indCountArr[i];
                                    verSize += 1;
                                }
                            }

                            gl.glUseProgram(repack.program);

                            //vertices in
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, ssbo[0]);
                            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 5, ssbo[0]);
                            //indices in
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, ssbo[1]);
                            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 6, ssbo[1]);
                            //normals in
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, ssbo[9]);
                            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 7, ssbo[9]);
                            //count in
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, ssbo[7]);
                            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 8, ssbo[7]);

                            //vert out
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, ssbo[5]);
                            gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER, ((verSize * 36) * 4), null, gl.GL_DYNAMIC_DRAW);
                            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 9, ssbo[5]);
                            //ind out
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, ssbo[6]);
                            gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER, (indSize * 4), null, gl.GL_DYNAMIC_DRAW);
                            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 10, ssbo[6]);
                            //normal out
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, ssbo[8]);
                            gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER, ((verSize * 36) * 4), null, gl.GL_DYNAMIC_DRAW);
                            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 11, ssbo[8]);

                            gl.glDispatchCompute(1, 1, 1);

                            gl.glMemoryBarrier(gl.GL_SHADER_STORAGE_BARRIER_BIT);

                            FloatBuffer vertbuffer = FloatBuffer.allocate((verSize * 36));
                            gl.glGetNamedBufferSubData(ssbo[5], 0, ((verSize * 36) * 4), vertbuffer);

                            IntBuffer indbuffer = IntBuffer.allocate((indSize));
                            gl.glGetNamedBufferSubData(ssbo[6], 0, (indSize * 4), indbuffer);

                            FloatBuffer normalBuffer = FloatBuffer.allocate((verSize * 36));
                            gl.glGetNamedBufferSubData(ssbo[8], 0, (verSize * 36) * 4, normalBuffer);


                            if (vertbuffer.array().length > 0 && indbuffer.array().length > 0 && normalBuffer.array().length > 0) {

                                Mesh mesh = new Mesh(vertbuffer.array(), indbuffer.array(), normalBuffer.array(), new Vector3f(
                                        node.indexX * LookupTable.CHUNKSIZE,
                                        node.indexY * LookupTable.CHUNKSIZE,
                                        node.indexZ * LookupTable.CHUNKSIZE)
                                );
                                mesh.id = node.id;
                                planet.setUpdatedMesh(mesh);


                            }
                        }

                    }

                    planet.finishedChunk();
                    long end = System.currentTimeMillis();
                    System.out.println((end - start) / 1000.0f + " for adding all chunks! "+ Thread.currentThread());
                    Map<Integer,Integer> detail = new LinkedHashMap<>();
                    for(Octree.Node node :structOriginal){
                        if(detail.containsKey(node.id.length())){
                            detail.put(node.id.length(), detail.get(node.id.length())+1);
                        }else{
                            detail.put(node.id.length(), 1);
                        }
                    }
                    for(Integer key : detail.keySet()){
                        System.out.println("Detail "+key+" : "+detail.get(key)+" Chunks");
                    }

                }
                updateRequest = false;

            }
        }

    }

    public boolean requestTerrain(Vector3f position){
        if(!updateRequest){
            this.lastPositionGenerated = new Vector3f(position);
            updateRequest = true;
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
