package main.java;



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

        position = new Vector3f(1,1,1);
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

        ;

        normalize();


        updateViewMatrix();

    }

    public void applyMomentum(){

        RenderManager rm = RenderManager.getInstance();

        Vector3i chunk = getChunk();
        //Mesh terrain = RenderManager.getInstance().getActivePlanets().get(0).getMesh(chunk.x,chunk.y,chunk.z,1);

        if(forwardMomentum!=0.0f){
            //Mesh.Collision collision = terrain.getRayIntersectionWithNormal(new Vector3f(this.position),new Vector3f(this.direction));
            Vector3f testPosition = new Vector3f(position).add(new Vector3f(direction).mul(forwardMomentum));
            Mesh.Collision collision = null;
            if(collision != null){
                if(collision.position.length() <  testPosition.length()){
                    this.forwardMomentum = 0.0f;
                    System.out.println(collision.normal.x+" "+collision.normal.y+" "+collision.normal.z);
                }else {
                    this.position.add(new Vector3f(direction).mul(forwardMomentum));
                }
            }else{
                this.position.add(new Vector3f(direction).mul(forwardMomentum));
            }
            forwardMomentum = forwardMomentum*0.75f;
            if(Math.abs(forwardMomentum) < 0.0001f){
                forwardMomentum = 0.0f;
            }
        }
        if(sideMomentum!=0.0f){
            //Vector3f intersection = terrain.getRayIntersection(new Vector3f(this.position),new Vector3f(this.side));
            Vector3f testPosition = new Vector3f(position).add(new Vector3f(side).mul(sideMomentum));
            Vector3f intersection = null;
            if(intersection != null){
                if(intersection.length() <  testPosition.length()){
                    this.sideMomentum = 0.0f;
                }else {
                    this.position.add(new Vector3f(side).mul(sideMomentum));
                }
            }else{
                this.position.add(new Vector3f(side).mul(sideMomentum));
            }
            sideMomentum = sideMomentum*0.75f;
            if(Math.abs(sideMomentum) < 0.0001f){
                sideMomentum = 0.0f;
            }
        }
        if(gravityMomentum!=0.0f){
            //Vector3f intersection = terrain.getRayIntersection(new Vector3f(this.position),new Vector3f(this.up).negate());
            Vector3f testPosition = new Vector3f(position).add(new Vector3f(up).negate().mul(gravityMomentum));
            Vector3f intersection = null;
            if(intersection != null){
                if(intersection.length() <  testPosition.length()){
                    this.gravityMomentum = 0.0f;
                }else {
                    this.position.add(new Vector3f(up).mul(gravityMomentum));
                }
            }else{
                this.position.add(new Vector3f(up).mul(gravityMomentum));
            }
            gravityMomentum = gravityMomentum*0.75f;
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
