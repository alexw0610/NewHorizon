package com.newhorizon;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;


public class Loader {


    public static BufferObject loadToGPU(Mesh mesh){


        GL4 gl = GLContext.getCurrentGL().getGL4();
        int[] vaoids = new int[1];
        gl.glGenVertexArrays(1,vaoids,0);

        int[] vboids = new int[4];
        gl.glGenBuffers(4,vboids,0);

        gl.glBindVertexArray(vaoids[0]);

        FloatBuffer verticesBuffer = FloatBuffer.allocate(mesh.vertices.length);
        verticesBuffer.put(mesh.vertices);
        verticesBuffer.flip();

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, vboids[0]);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, mesh.vertices.length * 4 ,verticesBuffer,gl.GL_STATIC_DRAW);
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, gl.GL_FLOAT, false, 0, 0);
        verticesBuffer.clear();
        verticesBuffer = null;

        //normal buffer
        FloatBuffer normalBuffer = FloatBuffer.allocate(mesh.normals.length);
        normalBuffer.put(mesh.normals);
        normalBuffer.flip();

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, vboids[1]);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, mesh.normals.length * 4 ,normalBuffer,gl.GL_STATIC_DRAW);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, gl.GL_FLOAT, false, 0, 0);
        normalBuffer.clear();
        normalBuffer = null;


        //color buffer

        FloatBuffer colorBuffer = FloatBuffer.allocate(mesh.colors.length);
        colorBuffer.put(mesh.colors);
        colorBuffer.flip();

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, vboids[2]);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, mesh.colors.length * 2 ,colorBuffer,gl.GL_STATIC_DRAW);
        gl.glEnableVertexAttribArray(2);

        gl.glVertexAttribIPointer(2, 1, gl.GL_SHORT, 0, 0);
        colorBuffer.clear();
        colorBuffer = null;

        IntBuffer indicesBuffer = IntBuffer.allocate(mesh.indices.length);
        indicesBuffer.put(mesh.indices);
        indicesBuffer.flip();

        gl.glBindBuffer(gl.GL_ELEMENT_ARRAY_BUFFER, vboids[3]);
        gl.glBufferData(gl.GL_ELEMENT_ARRAY_BUFFER, mesh.indices.length * 4 ,indicesBuffer,gl.GL_STATIC_DRAW);
        indicesBuffer.clear();
        indicesBuffer = null;

        gl.glBindVertexArray(0);

        BufferObject bo = new BufferObject(vaoids[0],vboids);


        return bo;

    }

    public static class BufferObject{
        int vaoID;
        int[] vboID;
        public BufferObject(int vaoID,int[]vboID){
            this.vaoID = vaoID;
            this.vboID = vboID;
        }
    }
    public static void unload(Mesh mesh){

        GL4 gl = GLContext.getCurrentGL().getGL4();
        gl.glDeleteVertexArrays(1,new int[]{mesh.vaoID},0);
        gl.glDeleteBuffers(4, mesh.vboID, 0);
    }

}
