package main.java;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;


public class Loader {


    public static int loadToGPU(Mesh mesh){


        GL4 gl = GLContext.getCurrentGL().getGL4();

        int[] vaoids = new int[1];
        gl.glGenVertexArrays(1,vaoids,0);


        int[] vboids = new int[3];
        gl.glGenBuffers(3,vboids,0);

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

        IntBuffer indicesBuffer = IntBuffer.allocate(mesh.indices.length);
        indicesBuffer.put(mesh.indices);
        indicesBuffer.flip();

        gl.glBindBuffer(gl.GL_ELEMENT_ARRAY_BUFFER, vboids[1]);
        gl.glBufferData(gl.GL_ELEMENT_ARRAY_BUFFER, mesh.indices.length * 4 ,indicesBuffer,gl.GL_STATIC_DRAW);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, mesh.type.equals(MeshType.TRIANGLE) ? 3 : mesh.type.equals(MeshType.LINE) ? 2 : mesh.type.equals(MeshType.POINT) ? 1:0, gl.GL_UNSIGNED_INT, false, 0, 0);

        indicesBuffer.clear();
        indicesBuffer = null;

        FloatBuffer normalBuffer = FloatBuffer.allocate(mesh.normals.length);
        normalBuffer.put(mesh.normals);
        normalBuffer.flip();

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, vboids[2]);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, mesh.normals.length * 4 ,normalBuffer,gl.GL_STATIC_DRAW);
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 3, gl.GL_FLOAT, false, 0, 0);

        normalBuffer.clear();
        normalBuffer = null;

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER,0);
        gl.glBindVertexArray(0);


        return vaoids[0];

    }

    public static void unload(Mesh mesh){

        GL4 gl = GLContext.getCurrentGL().getGL4();
        gl.glDeleteVertexArrays(1,new int[]{mesh.vaoID},0);
    }

}
