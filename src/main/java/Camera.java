package main.java;

import org.joml.Vector3f;

public class Camera {

    private Vector3f position;
    private Vector3f rotation;


    public Camera(){

        position = new Vector3f(8,6,50);
        rotation = new Vector3f(0,0,0);

    }

    public Vector3f getPosition(){
        return position;
    }
    public Vector3f getRotation(){
        return rotation;
    }
}
