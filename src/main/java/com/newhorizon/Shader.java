package com.newhorizon;


import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class Shader {

    public String name;
    public int program;
    int vs;
    int fs;
    private final Map<String, Integer> uniforms;

    public Shader(String name, InputStream file){

        GL4 gl = GLContext.getCurrentGL().getGL4();
        this.name = name;
        program = gl.glCreateProgram();
        if(!gl.glIsProgram(program)){
            System.err.println("Could not create test.Shader Program");

        }
        uniforms = new HashMap<>();

        //Vertex test.Shader
        vs = gl.glCreateShader(gl.GL_VERTEX_SHADER);

        String[] content = new String[]{readfile(new InputStreamReader(file))};

        gl.glShaderSource(vs,1,content,null);
        gl.glCompileShader(vs);

        IntBuffer status = IntBuffer.allocate(1);
        gl.glGetShaderiv(vs,gl.GL_COMPILE_STATUS,status);
        if(status.get() == gl.GL_FALSE){
            System.err.println("Could not compile Vertex com.newhorizon.shader: " + name);
            printErrLog(vs);
            System.exit(1);
        }

        //Fragment test.Shader
        fs = gl.glCreateShader(gl.GL_FRAGMENT_SHADER);
        InputStream fragInput = this.getClass().getResourceAsStream("/shader/"+this.name+".fs");
        content = new String[]{readfile(new InputStreamReader(fragInput))};

        gl.glShaderSource(fs, 1,content,null);
        gl.glCompileShader(fs);

        status = IntBuffer.allocate(1);
        gl.glGetShaderiv(fs,gl.GL_COMPILE_STATUS,status);
        if(status.get() == gl.GL_FALSE){
            System.err.println("Could not compile Fragment com.newhorizon.shader: " + name);
            printErrLog(fs);
            System.exit(1);
        }


        gl.glAttachShader(program, vs);
        gl.glAttachShader(program, fs);


        status = IntBuffer.allocate(1);
        gl.glGetProgramiv(program,gl.GL_ATTACHED_SHADERS,status);
        gl.glBindAttribLocation(program,0,"vertex");


        gl.glLinkProgram(program);
        status = IntBuffer.allocate(1);
        gl.glGetProgramiv(program,gl.GL_LINK_STATUS,status);
        if(status.get() == gl.GL_FALSE){
            System.err.println("Could not Link test.Shader: " + name);
            printErrLog(fs);
            printErrLog(vs);
            System.exit(1);
        }

        gl.glValidateProgram(program);
        status = IntBuffer.allocate(1);
        gl.glGetProgramiv(program,gl.GL_VALIDATE_STATUS,status);
        if(status.get() == gl.GL_FALSE){
            System.err.println("Could not validate test.Shader: " + name);
            printErrLog(program);
            System.exit(1);
        }


    }

    private String readfile(InputStreamReader file){
        StringBuilder shadercode = new StringBuilder();
        BufferedReader br;
        try{
            br = new BufferedReader(file);
            String line;
            while((line = br.readLine()) != null){
                shadercode.append(line);
                shadercode.append("\n");

            }
            br.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        return shadercode.toString();


    }

    private void printErrLog(int shader){
        GL4 gl = GLContext.getCurrentGL().getGL4();

        IntBuffer log_length = IntBuffer.allocate(1);
        ByteBuffer message = ByteBuffer.allocate(1024);
        gl.glGetShaderInfoLog(shader, 1024, log_length, message);
        byte[] bytes = message.array();
        String output = new String( bytes, StandardCharsets.UTF_8);
        System.out.println(output);

    }

}

