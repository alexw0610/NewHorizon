package com.newhorizon;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;

public class ComputeShader {

    public String name;
    public int program;
    int cs;

    public ComputeShader(String name, InputStream input){
        GL4 gl = GLContext.getCurrentGL().getGL4();


        //String[] temp = file.getName().split("\\.");
        //name = temp[0];
        name = name;
        program = gl.glCreateProgram();
        if(!gl.glIsProgram(program)){
            System.err.println("Could not create program");
        }

        cs = gl.glCreateShader(gl.GL_COMPUTE_SHADER);
        String[] content = new String[]{readfile(new InputStreamReader(input))};
        gl.glShaderSource(cs,1,content,null);
        gl.glCompileShader(cs);
        IntBuffer status = IntBuffer.allocate(1);
        gl.glGetShaderiv(cs,gl.GL_COMPILE_STATUS,status);
        if(status.get() == gl.GL_FALSE){
            System.err.println("Could not compile compute com.newhorizon.shader: " + name);
            printShaderErrLog(cs);
            System.exit(1);
        }


        gl.glAttachShader(program, cs);
        gl.glLinkProgram(program);

        status = IntBuffer.allocate(1);
        gl.glGetProgramiv(program,gl.GL_LINK_STATUS,status);
        if(status.get() == gl.GL_FALSE){
            System.err.println("Could not link program: " + name);
            status = IntBuffer.allocate(1);
            gl.glGetProgramiv(program,gl.GL_ATTACHED_SHADERS,status);
            System.out.println(status.get());
            printProgramErrLog(program);
            System.exit(1);
        }

        gl.glValidateProgram(program);
        status = IntBuffer.allocate(1);
        gl.glGetProgramiv(program,gl.GL_VALIDATE_STATUS,status);
        if(status.get() == gl.GL_FALSE){
            System.err.println("Could not validate program: " + name);
            printProgramErrLog(program);
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

    private void printShaderErrLog(int shader){
        GL4 gl = GLContext.getCurrentGL().getGL4();

        IntBuffer log_length = IntBuffer.allocate(1);
        ByteBuffer message = ByteBuffer.allocate(2048);
        gl.glGetShaderInfoLog(shader, 2048, log_length, message);
        byte[] bytes = message.array();
        String output = new String( bytes, StandardCharsets.UTF_8);
        System.out.println(output);

    }
    private void printProgramErrLog(int program){
        GL4 gl = GLContext.getCurrentGL().getGL4();

        IntBuffer log_length = IntBuffer.allocate(1);
        ByteBuffer message = ByteBuffer.allocate(1024);
        gl.glGetProgramInfoLog(program, 1024, log_length, message);
        byte[] bytes = message.array();
        String output = new String( bytes, StandardCharsets.UTF_8);
        System.out.println(output);

    }
}
