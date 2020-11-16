package com.newhorizon;


import com.jogamp.opengl.*;
import org.joml.Vector3f;


import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import java.util.LinkedList;

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
    private int[] persistentSSBOHandles;



    TerrainLoaderAsync(){

        renderManager = RenderManager.getInstance();
        t = new Thread(this,"TerrainGenerator");
        t.start();

    }

    private void setUpPersistentSSBO(){

        gl.glUseProgram(compute.program);

        persistentSSBOHandles = new int[8];
        gl.glGenBuffers(8,persistentSSBOHandles,0);

        int[] indicesLookup = LookupTable.getAllIndices();
        IntBuffer indicesLookupBuffer = IntBuffer.allocate(indicesLookup.length);
        indicesLookupBuffer.put(indicesLookup);
        indicesLookupBuffer.rewind();

        float[] verticesLookup = LookupTable.getVertices();
        FloatBuffer verticesLookupBuffer = FloatBuffer.allocate(verticesLookup.length);
        verticesLookupBuffer.put(verticesLookup);
        verticesLookupBuffer.rewind();

        //indicesLookup
        gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER,persistentSSBOHandles[0]);
        gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER,indicesLookup.length*4,indicesLookupBuffer,gl.GL_STATIC_READ);
        gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER,1,persistentSSBOHandles[0]);

        //verticesLookup
        gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER,persistentSSBOHandles[1]);
        gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER,verticesLookup.length*4,verticesLookupBuffer,gl.GL_STATIC_READ);
        gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER,2,persistentSSBOHandles[1]);

        //vertices out
        gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, persistentSSBOHandles[2]);
        gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER, ((36 * (16 * 16 * 16)) * 4), null, gl.GL_DYNAMIC_DRAW);
        gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 5, persistentSSBOHandles[2]);

        //indices out
        gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, persistentSSBOHandles[3]);
        gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER, ((15 * (16 * 16 * 16)) * 4), null, gl.GL_DYNAMIC_DRAW);
        gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 6, persistentSSBOHandles[3]);

        //normals out
        gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, persistentSSBOHandles[4]);
        gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER, ((36 * (16 * 16 * 16)) * 4), null, gl.GL_DYNAMIC_DRAW);
        gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 7, persistentSSBOHandles[4]);

        //voxel data
        gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, persistentSSBOHandles[5]);
        gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER, (17 * 17 * 17) * 4,null, gl.GL_STATIC_READ);
        gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 4, persistentSSBOHandles[5]);

        //resolution in
        gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, persistentSSBOHandles[6]);
        gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER, 4, null, gl.GL_STATIC_READ);
        gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 3, persistentSSBOHandles[6]);

        //count out
        gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, persistentSSBOHandles[7]);
        gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER, (16 * 16 * 16) * 4, null, gl.GL_DYNAMIC_DRAW);
        gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 8, persistentSSBOHandles[7]);


    }
    @Override
    public void run() {

        GLContext context = RenderManager.getInstance().getContext();
        context.makeCurrent();
        gl = context.getGL().getGL4();

        compute = new ComputeShader("computShader",this.getClass().getResourceAsStream("/shader/computeShader/computeShader.cs"));
        repack = new ComputeShader("repackShader",this.getClass().getResourceAsStream("/shader/computeShader/repackShader.cs"));

        setUpPersistentSSBO();
        LinkedList<Mesh> generatedMeshes = new LinkedList<>();
        float average = 0.0f;
        long count = 0;

        while(t.isAlive()) {
            if (updateRequest) {

                for (Planet planet : renderManager.getActivePlanets()) {
                    long start = System.currentTimeMillis();

                    LinkedList<Octree.Node> structOriginal = planet.tree.searchChunk(lastPositionGenerated);

                    LinkedList<Octree.Node> structCopy = (LinkedList<Octree.Node>) structOriginal.clone();
                    LinkedList<Octree.Node> struct = planet.filterExisting(structOriginal);

                    for (Octree.Node node : struct) {

                        Planet.VoxelGroup group = planet.getVoxelData(node.indexX, node.indexY, node.indexZ, node.span, node.span);

                        if(!group.empty) {

                            gl.glUseProgram(compute.program);

                            int[] ssbo = new int[3];
                            gl.glGenBuffers(3, ssbo, 0);

                            FloatBuffer voxelBuffer = FloatBuffer.allocate(group.data.length);
                            voxelBuffer.put(group.data);
                            voxelBuffer.rewind();

                            IntBuffer spanBuffer = IntBuffer.allocate(1);
                            spanBuffer.put(node.span);
                            spanBuffer.rewind();

                            //voxelData in
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, persistentSSBOHandles[5]);
                            gl.glBufferSubData(gl.GL_SHADER_STORAGE_BUFFER,0,(17 * 17 * 17) * 4,voxelBuffer);

                            //resolution in
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, persistentSSBOHandles[6]);
                            gl.glBufferSubData(gl.GL_SHADER_STORAGE_BUFFER,0,4,spanBuffer);

                            //count out
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, persistentSSBOHandles[7]);
                            gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER, (16 * 16 * 16) * 4, null, gl.GL_DYNAMIC_DRAW);

                                gl.glDispatchCompute(16, 16, 16);
                                gl.glMemoryBarrier(gl.GL_SHADER_STORAGE_BARRIER_BIT);

                            IntBuffer indcount = IntBuffer.allocate(16 * 16 * 16);
                            gl.glGetNamedBufferSubData(persistentSSBOHandles[7], 0, (16 * 16 * 16) * 4, indcount);

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

                            //vert out
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, ssbo[0]);
                            gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER, ((verSize * 36) * 4), null, gl.GL_DYNAMIC_DRAW);
                            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 9, ssbo[0]);

                            //ind out
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, ssbo[1]);
                            gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER, (indSize * 4), null, gl.GL_DYNAMIC_DRAW);
                            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 10, ssbo[1]);

                            //normal out
                            gl.glBindBuffer(gl.GL_SHADER_STORAGE_BUFFER, ssbo[2]);
                            gl.glBufferData(gl.GL_SHADER_STORAGE_BUFFER, ((verSize * 36) * 4), null, gl.GL_DYNAMIC_DRAW);
                            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 11, ssbo[2]);

                                gl.glDispatchCompute(1, 1, 1);
                                gl.glMemoryBarrier(gl.GL_SHADER_STORAGE_BARRIER_BIT);

                            FloatBuffer vertbuffer = FloatBuffer.allocate((verSize * 36));
                            gl.glGetNamedBufferSubData(ssbo[0], 0, ((verSize * 36) * 4), vertbuffer);

                            IntBuffer indbuffer = IntBuffer.allocate((indSize));
                            gl.glGetNamedBufferSubData(ssbo[1], 0, (indSize * 4), indbuffer);

                            FloatBuffer normalBuffer = FloatBuffer.allocate((verSize * 36));
                            gl.glGetNamedBufferSubData(ssbo[2], 0, (verSize * 36) * 4, normalBuffer);


                            if (vertbuffer.array().length > 0 && indbuffer.array().length > 0 && normalBuffer.array().length > 0) {

                                Mesh mesh = new Mesh(vertbuffer.array(), indbuffer.array(), normalBuffer.array(), new Vector3f(
                                        node.indexX * LookupTable.CHUNKSIZE,
                                        node.indexY * LookupTable.CHUNKSIZE,
                                        node.indexZ * LookupTable.CHUNKSIZE));

                                mesh.id = node.id;
                                generatedMeshes.add(mesh);

                            }
                            gl.glDeleteBuffers(3, ssbo, 0);
                        }

                    }
                    planet.setUpdatedMesh(generatedMeshes,structCopy);
                    long end = System.currentTimeMillis();
                    System.out.println((end - start) / 1000.0f + " for generating all chunks! Rolling average: "+average/count+" "+ Thread.currentThread());
                    average +=(end - start) / 1000.0f;
                    count++;
                    if(count > 10){
                        count = 0;
                        average = 0;
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
