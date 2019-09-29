package main.java;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.Animator;
import org.joml.Vector3f;

import java.util.BitSet;



public class Display implements GLEventListener, KeyListener {


    private int vaoID;

    private BitSet keyStates = new BitSet(512);
    private Animator animator;

    private Camera camera;
    private Voxel voxel;

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
        gl.glEnable(gl.GL_DEPTH);
        gl.glEnable(gl.GL_DEPTH_TEST);

        camera = new Camera();

        long start = System.currentTimeMillis();
        voxel = new Voxel(32,0.5f);
        long end = System.currentTimeMillis();
        System.out.println("Time to create: "+(end-start)/1000.0f);


    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    private void update(){

        float delta = -0.2f;

        if(getKey(KeyEvent.VK_W)){
            camera.getPosition().z += delta;
        }
        if(getKey(KeyEvent.VK_S)){
            camera.getPosition().z -= delta;
        }

        if(getKey(KeyEvent.VK_A)){
            camera.getPosition().x -= delta;
        }
        if(getKey(KeyEvent.VK_D)){
            camera.getPosition().x += delta;
        }

        if(getKey(KeyEvent.VK_Q)){
            camera.getPosition().y += delta;
        }
        if(getKey(KeyEvent.VK_E)){
            camera.getPosition().y -= delta;
        }

        if(getKey(KeyEvent.VK_LEFT)){
            camera.getRotation().y -= delta*2;
        }
        if(getKey(KeyEvent.VK_RIGHT)){
            camera.getRotation().y += delta*2;
        }

        if(getKey(KeyEvent.VK_UP)){
            camera.getRotation().x -= delta*2;
        }
        if(getKey(KeyEvent.VK_DOWN)){
            camera.getRotation().x += delta*2;
        }
        if(getKey(KeyEvent.VK_N)){
            Loader.unload(voxel.chunk);
            voxel = new Voxel(32,0.5f);
        }


    }
    boolean rendered = false;
    @Override
    public void display(GLAutoDrawable drawable) {

        long start = System.currentTimeMillis();
        Render.clear(drawable);

        Render.draw(voxel.chunk,camera,drawable);


        long end = System.currentTimeMillis();

        if(!rendered){
            System.out.println("Time to render once: "+(end-start)/1000.0f);
            rendered = true;

        }


        update();

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

    }

}
