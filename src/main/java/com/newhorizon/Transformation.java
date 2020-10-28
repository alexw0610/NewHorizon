package com.newhorizon;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transformation {

    private static Matrix4f viewMatrix = new Matrix4f().identity();
    private static Matrix4f projectionMatrix = new Matrix4f().identity();
    private static Matrix4f modelMatrix = new Matrix4f().identity();

    private static float[] viewMatrixArr =  new float[16];
    private static float[] projectionMatrixArr = new float[16];
    private static float[] modelMatrixArr = new float[16];


    public static float[] getModelMatrix(Vector3f position, Vector3f rotation){

        modelMatrix.identity().translate(position).
                rotateX((float)Math.toRadians(rotation.x)).
                rotateY((float)Math.toRadians(rotation.y)).
                rotateZ((float)Math.toRadians(rotation.z)).
                scale(1.0f);

        modelMatrix.get(modelMatrixArr);

        return modelMatrixArr;
    }



}
