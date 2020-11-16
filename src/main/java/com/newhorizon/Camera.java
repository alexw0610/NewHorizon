package com.newhorizon;


import com.newhorizon.util.Chunkify;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;


public class Camera {

    Vector3f position;
    Vector3f direction;
    Vector3f up;
    Vector3f side;

    private Matrix4f viewMatrix;
    private Matrix4f projectionMatrix;

    private float[] viewMatrixArr;
    private float[] projectionMatrixArr;

    float forwardMomentum = 0.0f;
    float sideMomentum = 0.0f;
    float gravityMomentum = 0.0f;


    public Camera(){

        position = new Vector3f(310,310,310);
        direction = new Vector3f(0,1,0);
        up = new Vector3f(0,0,1);
        side = new Vector3f(1,0,0);

        projectionMatrix = new Matrix4f().identity();
        viewMatrix = new Matrix4f().identity();

        projectionMatrixArr = new float[16];
        viewMatrixArr = new float[16];

        udpateProjection();
        updateViewMatrix();

    }


    public void udpateProjection(){

        float aspectRatio = Settings.WIDTH / Settings.HEIGHT;
        projectionMatrix.identity().perspective((float)Math.toRadians(-45.0f), aspectRatio, 0.1f, 10000.0f);
        projectionMatrix.get(projectionMatrixArr);

    }

    public float[] getProjectionMatrix(){

        return projectionMatrixArr;
    }


    private void updateViewMatrix(){

        viewMatrix.identity();
        viewMatrix.lookAt(new Vector3f(position),new Vector3f(position).add(new Vector3f(direction).mul(2.0f)),new Vector3f(up));
        viewMatrix.get(viewMatrixArr);


    }

    public float[] getViewMatrix(){
        return viewMatrixArr;
    }

    public void addMomentumVector(Vector3f vector){

        Vector3f forward = new Vector3f(this.direction);
        Vector3f sideward = new Vector3f(this.side);
        Vector3f upward = new Vector3f(this.up);

        Vector3f target = new Vector3f(0,0,0);

        target.x = forward.dot(vector);
        target.y = sideward.dot(vector);
        target.z = upward.dot(vector);
        target.normalize();

        addMomentum(target.y,target.z*-1.0f,target.x);

    }

    public void addMomentum(float deltaX, float deltaY, float deltaZ) {

        if(deltaZ!=0){
            forwardMomentum += deltaZ;
        }
        if(deltaX!=0){
            sideMomentum += deltaX;
        }
        if(deltaY!=0){
            gravityMomentum += deltaY;
        }

    }

    public void rotate(float deltaX, float deltaY, float deltaZ){

        if(deltaX!=0.0f){
            direction.rotateAxis((float)Math.toRadians(deltaX), side.x,side.y,side.z);
            up.rotateAxis((float)Math.toRadians(deltaX),side.x,side.y,side.z);
        }
        if(deltaY!=0.0f){
            direction.rotateAxis((float)Math.toRadians(deltaY), up.x,up.y,up.z);
            side.rotateAxis((float)Math.toRadians(deltaY),up.x,up.y,up.z);
        }
        if(deltaZ!=0.0f){
            side.rotateAxis((float)Math.toRadians(deltaZ), direction.x,direction.y,direction.z);
            up.rotateAxis((float)Math.toRadians(deltaZ),direction.x,direction.y,direction.z);
        }

        normalize();
        updateViewMatrix();

    }

    public void applyMomentum(){

        Vector3f testPosition = new Vector3f(0,0,0);

        Vector3f forward = new Vector3f(this.direction).mul(forwardMomentum);
        Vector3f sideward = new Vector3f(this.side).mul(sideMomentum);
        Vector3f upward = new Vector3f(this.up).negate().mul(gravityMomentum);

        testPosition.add(forward).add(sideward).add(upward);


        if(!testPosition.equals(0,0,0)){

            this.position.add(new Vector3f(testPosition));

            forwardMomentum = forwardMomentum*0.75f;
            sideMomentum = sideMomentum*0.75f;
            gravityMomentum = gravityMomentum*0.75f;

            if(Math.abs(forwardMomentum) < 0.0001f){
                forwardMomentum = 0.0f;
            }
            if(Math.abs(sideMomentum) < 0.0001f){
                sideMomentum = 0.0f;
            }
            if(Math.abs(gravityMomentum) < 0.0001f){
                gravityMomentum = 0.0f;
            }


        }

        updateViewMatrix();

    }

    private void normalize(){
        this.direction.normalize();
        this.side.normalize();
        this.up.normalize();
    }

    public Vector3f getPosition(){
        return new Vector3f(position);
    }
    public Vector3i getChunk(){
        return Chunkify.getChunk(this.position);
    }
    public void printLoc(){

        System.out.println("--Rotation--");
        System.out.println("dir: "+direction.x+" "+direction.y+" "+direction.z);
        System.out.println("side:"+side.x+" "+side.y+" "+side.z);
        System.out.println("up: "+up.x+" "+up.y+" "+up.z);
        System.out.println("--Position--");
        System.out.println("pos: "+position.x+" "+position.y+" "+position.z);


    }
}
