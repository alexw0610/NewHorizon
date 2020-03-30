package main.java;

import org.joml.Vector3f;


public class Camera {

    private Vector3f position;
    private Vector3f rotation;


    public Camera(){

        position = new Vector3f(0,0,0);
        rotation = new Vector3f(0,0,0);

    }

    public void movePosition(float offsetX, float offsetY, float offsetZ) {

        if ( offsetZ != 0 ) {
            position.x += (float)Math.sin(Math.toRadians(rotation.y)) * (float)Math.cos(Math.toRadians(rotation.x)) * -1.0f * offsetZ;
            position.z += (float)Math.cos(Math.toRadians(rotation.y)) * (float)Math.cos(Math.toRadians(rotation.x)) * offsetZ;
            position.y += (float)Math.sin(Math.toRadians(rotation.x)) * offsetZ;

        }
        if ( offsetX != 0) {
            position.x += (float)Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * offsetX;
            position.z += (float)Math.cos(Math.toRadians(rotation.y - 90)) * offsetX;

        }
        position.y += offsetY;


    }

    public Vector3f getPosition(){
        return position;
    }
    public Vector3f getRotation(){
        return rotation;
    }
}
