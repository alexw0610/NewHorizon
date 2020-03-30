package main.java;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transformation {

    private static Matrix4f viewMatrix = new Matrix4f().identity();
    private static Matrix4f projectionMatrix = new Matrix4f().identity();
    private static Matrix4f modelMatrix = new Matrix4f().identity();

    private static float[] viewMatrixArr =  new float[16];
    private static float[] projectionMatrixArr = new float[16];
    private static float[] modelMatrixArr = new float[16];

    public static float[] getProjectionMatrix(){

        float aspectRatio = Settings.WIDTH / Settings.HEIGHT;
        projectionMatrix.identity().perspective(75.0f, aspectRatio, 1.0f, 100000.0f);
        projectionMatrix.get(projectionMatrixArr);

        return projectionMatrixArr;
    }

    public static float[] getModelMatrix(Vector3f position, Vector3f rotation){

        modelMatrix.identity().translate(position).
                rotateX((float)Math.toRadians(rotation.x)).
                rotateY((float)Math.toRadians(rotation.y)).
                rotateZ((float)Math.toRadians(rotation.z)).
                scale(1.0f);

        modelMatrix.get(modelMatrixArr);

        return modelMatrixArr;
    }

    public static float[] getViewMatrix(Camera camera){

        viewMatrix.identity().rotate((float) Math.toRadians(camera.getRotation().x), new Vector3f(1, 0, 0))
                .rotate((float) Math.toRadians(camera.getRotation().y), new Vector3f(0, 1, 0));
        viewMatrix.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);

        viewMatrix.get(viewMatrixArr);

        return viewMatrixArr;
    }


}
