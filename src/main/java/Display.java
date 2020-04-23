package main.java;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.Animator;
import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.BitSet;
import java.util.LinkedList;


public class Display implements GLEventListener, KeyListener {


    private BitSet keyStates = new BitSet(512);
    private Animator animator;
    private Camera camera;

    private LinkedList<Mesh> meshList = new LinkedList<>();

    private Mesh grid;
    private float frameDelta;
    private TerrainLoaderAsync terrainLoader;
    long lastRequestMade = 0;
    Display(){
        setup();
    }

    private void setup(){

        // Get the default OpenGL profile, reflecting the best for your running platform
        GLProfile glp = GLProfile.get("GL4");
        // Specifies a set of OpenGL capabilities, based on your profile.
        GLCapabilities caps = new GLCapabilities(glp);
        // Create the OpenGL rendering canvas
        GLWindow window = GLWindow.create(caps);
        // Create a animator that drives canvas' display() at the specified FPS.
        this.animator = new Animator(window);

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDestroyNotify(WindowEvent arg0) {
                // Use a dedicate thread to run the stop() to ensure that the
                // animator stops before program exits.
                new Thread(() -> {
                    if (animator.isStarted())
                        animator.stop();    // stop the animator loop
                    System.exit(0);
                }).start();
            }
        });

        window.addGLEventListener(this);
        window.addKeyListener(this);
        window.setSize((int)Settings.WIDTH, (int)Settings.HEIGHT);
        window.setTitle("New Horizon");
        window.setVisible(true);
        animator.setRunAsFastAsPossible(true);
        animator.start();  // start the animator loop

    }


    @Override
    public void keyPressed(KeyEvent e) {
        if(!e.isAutoRepeat()){
            setKey(e.getKeyCode());
        }
    }

    private void setKey(short code){
        keyStates.set(code,true);

    }

    private void unsetKey(short code){
        keyStates.set(code,false);
    }

    private boolean getKey(short code){
        return keyStates.get(code);
    }
    @Override
    public void keyReleased(KeyEvent e) {
        if(!e.isAutoRepeat()){
            unsetKey(e.getKeyCode());
        }
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL4 gl = drawable.getGL().getGL4();
        gl.glEnable(gl.GL_DEPTH_TEST);
        gl.glDepthFunc(gl.GL_LESS);
        gl.glEnable (gl.GL_BLEND);
        gl.glBlendFunc (gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
        gl.glPolygonMode( gl.GL_FRONT, gl.GL_FILL );

        terrainLoader = new TerrainLoaderAsync();
        camera = new Camera();
        grid = Grid.getGrid();
        grid.type = MeshType.LINE;
        grid.loadMesh();


    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    private void update(){


        float delta = -1.0f*frameDelta;
        float viewDelta = -0.1f*frameDelta;


        if(getKey(KeyEvent.VK_W)){
            camera.movePosition(0,0,delta);
        }
        if(getKey(KeyEvent.VK_S)){
            camera.movePosition(0,0,-delta);
        }

        if(getKey(KeyEvent.VK_A)){
            camera.movePosition(-delta,0,0);
        }
        if(getKey(KeyEvent.VK_D)){
            camera.movePosition(delta,0,0);
        }

        if(getKey(KeyEvent.VK_Q)){
            camera.movePosition(0,delta,0);
        }
        if(getKey(KeyEvent.VK_E)){
            camera.movePosition(0,-delta,0);
        }

                //ROTATION

        if(getKey(KeyEvent.VK_LEFT)){
            camera.getRotation().y -= viewDelta;
        }
        if(getKey(KeyEvent.VK_RIGHT)){
            camera.getRotation().y += viewDelta;
        }
        if(getKey(KeyEvent.VK_UP)){
            camera.getRotation().x -= viewDelta;
        }
        if(getKey(KeyEvent.VK_DOWN)){
            camera.getRotation().x += viewDelta;
        }

                //DEBUG

        if(getKey(KeyEvent.VK_R)){

        }
        if(getKey(KeyEvent.VK_T)){

        }

        Vector3i newChunk = new Vector3i((int)Math.floor(camera.getPosition().x/LookupTable.CHUNKSIZE),(int)Math.floor(camera.getPosition().y/LookupTable.CHUNKSIZE),(int)Math.floor(camera.getPosition().z/LookupTable.CHUNKSIZE));
        Vector3f oldPosition = terrainLoader.getPosition();
        Vector3i oldChunk = new Vector3i((int)Math.floor(oldPosition.x/LookupTable.CHUNKSIZE),(int)Math.floor(oldPosition.y/LookupTable.CHUNKSIZE),(int)Math.floor(oldPosition.z/LookupTable.CHUNKSIZE));
        if(oldChunk.x != newChunk.x || oldChunk.y != newChunk.y || oldChunk.z != newChunk.z){

            if(terrainLoader.requestTerrain(camera.getPosition())){
                lastRequestMade = System.currentTimeMillis();
            }
        }
        if(terrainLoader.hasUpdated()){
            long update = System.currentTimeMillis();
            for(Mesh mesh : meshList){
                mesh.unloadMesh();

            }
            this.meshList = terrainLoader.getRequestedTerrain();
            for(Mesh mesh : meshList){
                mesh.loadMesh();

            }
            long current = System.currentTimeMillis();
            System.out.println((current-update)/1000.0f+" seconds for uploading Terrain to gpu!");
            System.out.println((current-lastRequestMade)/1000.0f+" seconds for updating Terrain!");
        }


    }

    @Override
    public void display(GLAutoDrawable drawable) {

        update();

        long start = System.currentTimeMillis();

        Render.clear(drawable);

        for(Mesh mesh: meshList){
            Render.draw(mesh,camera,drawable);
        }
        Render.drawAxisGrid(drawable,camera,grid);


        while((System.currentTimeMillis()-start)/1000.0f < 0.01f){
            try {
                Thread.sleep(0,1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        frameDelta = System.currentTimeMillis()-start;

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

    }

}
