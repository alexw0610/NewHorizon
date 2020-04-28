package main.java;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import org.joml.Vector3f;


import java.io.File;


public class Render {

    private static Shader terrainShader = new Shader(new File("src\\main\\java\\shader\\terrain.vs"));
    private static Shader defaultShader = new Shader(new File("src\\main\\java\\shader\\shader.vs"));

    public static void draw(Mesh mesh, Camera camera, GLAutoDrawable context){
        GL4 gl = context.getGL().getGL4();

        gl.glUseProgram(terrainShader.program);
        setupUniforms(mesh,camera, context);
        draw(mesh.vaoID,mesh.indices.length,context);
    }

    private static void draw(int vao,int count,GLAutoDrawable context){
        GL4 gl = context.getGL().getGL4();

        gl.glUseProgram(terrainShader.program);

        gl.glBindVertexArray(vao);
        gl.glEnableVertexAttribArray(0);
        gl.glEnableVertexAttribArray(1);
        gl.glEnableVertexAttribArray(2);


        gl.glDrawElements(gl.GL_TRIANGLES,count,gl.GL_UNSIGNED_INT,0);


        gl.glDisableVertexAttribArray(2);
        gl.glDisableVertexAttribArray(1);
        gl.glDisableVertexAttribArray(0);
        gl.glBindVertexArray(0);

    }

    public static void drawAxisGrid(GLAutoDrawable context, Camera camera, Mesh grid){
        GL4 gl = context.getGL().getGL4();

        gl.glUseProgram(defaultShader.program);

        int loc = gl.glGetUniformLocation(defaultShader.program, "viewMatrix");
        gl.glUniformMatrix4fv(loc,1,false,Transformation.getViewMatrix(camera),0);
        loc = gl.glGetUniformLocation(defaultShader.program, "projectionMatrix");
        gl.glUniformMatrix4fv(loc,1,false,Transformation.getProjectionMatrix(),0);
        loc = gl.glGetUniformLocation(defaultShader.program, "camera");
        gl.glUniform3f(loc,camera.getPosition().x,camera.getPosition().y,camera.getPosition().z);

        gl.glBindVertexArray(grid.vaoID);
        gl.glEnableVertexAttribArray(0);
        gl.glEnableVertexAttribArray(1);
        gl.glEnableVertexAttribArray(2);


        gl.glDrawElements(gl.GL_LINES,grid.indices.length,gl.GL_UNSIGNED_INT,0);


        gl.glDisableVertexAttribArray(2);
        gl.glDisableVertexAttribArray(1);
        gl.glDisableVertexAttribArray(0);
        gl.glBindVertexArray(0);

    }

    public static  void clear(GLAutoDrawable context){
        GL4 gl = context.getGL().getGL4();

        gl.glClear(gl.GL_COLOR_BUFFER_BIT);
        gl.glClear(gl.GL_DEPTH_BUFFER_BIT);
        gl.glClearColor(0.01f,0.01f,0.03f,0);

    }


    private static void setupUniforms(Mesh mesh, Camera camera, GLAutoDrawable context){
        GL4 gl = context.getGL().getGL4();

        int loc = gl.glGetUniformLocation(terrainShader.program, "viewMatrix");
        gl.glUniformMatrix4fv(loc,1,false,Transformation.getViewMatrix(camera),0);
        loc = gl.glGetUniformLocation(terrainShader.program, "projectionMatrix");
        gl.glUniformMatrix4fv(loc,1,false,Transformation.getProjectionMatrix(),0);
        loc = gl.glGetUniformLocation(terrainShader.program, "modelMatrix");
        gl.glUniformMatrix4fv(loc,1,false,Transformation.getModelMatrix(mesh.position,new Vector3f(0,0,0)),0);

    }
}
