package main.java;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import org.joml.Vector3f;


import java.io.File;


public class Render {

    private static Shader terrainShader = new Shader(new File("src\\main\\java\\shader\\terrain.vs"));
    private static Shader defaultShader = new Shader(new File("src\\main\\java\\shader\\shader.vs"));

    public static void draw(GLAutoDrawable context){
        GL4 gl = context.getGL().getGL4();

        RenderManager renderManager = RenderManager.getInstance();
        gl.glUseProgram(terrainShader.program);
        for(Planet planet : renderManager.getActivePlanets()){
            planet.updateMeshes();
            for(Mesh mesh : planet.getCurrentMeshes()){
                setupUniforms(mesh,planet,renderManager.getActiveCamera(), context);
                draw(mesh.vaoID,mesh.indices.length,context);
            }

        }

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


    public static  void clear(GLAutoDrawable context){
        GL4 gl = context.getGL().getGL4();

        gl.glClear(gl.GL_COLOR_BUFFER_BIT);
        gl.glClear(gl.GL_DEPTH_BUFFER_BIT);
        gl.glClearColor(0.8f,0.8f,0.8f,0);

    }


    private static void setupUniforms(Mesh mesh, Planet planet, Camera camera, GLAutoDrawable context){
        GL4 gl = context.getGL().getGL4();

        int loc = gl.glGetUniformLocation(terrainShader.program, "viewMatrix");
        gl.glUniformMatrix4fv(loc,1,false,camera.getViewMatrix(),0);
        loc = gl.glGetUniformLocation(terrainShader.program, "projectionMatrix");
        gl.glUniformMatrix4fv(loc,1,false,camera.getProjectionMatrix(),0);
        loc = gl.glGetUniformLocation(terrainShader.program, "modelMatrix");
        gl.glUniformMatrix4fv(loc,1,false,Transformation.getModelMatrix(mesh.position,new Vector3f(0,0,0)),0);
        loc = gl.glGetUniformLocation(terrainShader.program, "cameraPos");
        gl.glUniform3f(loc,camera.getPosition().x,camera.getPosition().y,camera.getPosition().z);
        loc = gl.glGetUniformLocation(terrainShader.program, "atmoDensity");
        gl.glUniform1f(loc,planet.atmoDensity);
        loc = gl.glGetUniformLocation(terrainShader.program, "atmoColor");
        gl.glUniform3f(loc, planet.atmoColor.x, planet.atmoColor.y,planet.atmoColor.z);


    }
}
