package main.java;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import org.joml.Vector3f;


import java.io.File;


public class Render {

    private static Shader shader = new Shader(new File("src\\main\\java\\shader\\shader.vs"));

    public static void draw(Mesh mesh, Camera camera, GLAutoDrawable context){
        GL4 gl = context.getGL().getGL4();

        gl.glUseProgram(shader.program);
        setupUniforms(mesh,camera, context);

        draw(mesh.vaoID,mesh.indices.length,context);
    }

    private static void draw(int vao,int count,GLAutoDrawable context){

        GL4 gl = context.getGL().getGL4();


        gl.glUseProgram(shader.program);

        gl.glBindVertexArray(vao);

        gl.glEnableVertexAttribArray(0);
        gl.glEnableVertexAttribArray(1);
        gl.glEnableVertexAttribArray(2);

        gl.glDrawElements(gl.GL_TRIANGLES,count,gl.GL_UNSIGNED_SHORT,0);

        gl.glDisableVertexAttribArray(2);
        gl.glDisableVertexAttribArray(1);
        gl.glDisableVertexAttribArray(0);

        gl.glBindVertexArray(0);

    }

    public static  void clear(GLAutoDrawable context){

        GL4 gl = context.getGL().getGL4();
        gl.glClear(gl.GL_COLOR_BUFFER_BIT);
        gl.glClear(gl.GL_DEPTH_BUFFER_BIT);
        gl.glClearColor(1f,1f,1f,0);

    }


    private static void setupUniforms(Mesh mesh, Camera camera, GLAutoDrawable context){

        GL4 gl = context.getGL().getGL4();

        int loc = gl.glGetUniformLocation(shader.program, "viewMatrix");
        gl.glUniformMatrix4fv(loc,1,false,Transformation.getViewMatrix(camera),0);

        loc = gl.glGetUniformLocation(shader.program, "projectionMatrix");
        gl.glUniformMatrix4fv(loc,1,false,Transformation.getProjectionMatrix(),0);

        loc = gl.glGetUniformLocation(shader.program, "modelMatrix");
        gl.glUniformMatrix4fv(loc,1,false,Transformation.getModelMatrix(mesh.position,new Vector3f(0,0,0)),0);

    }
}
