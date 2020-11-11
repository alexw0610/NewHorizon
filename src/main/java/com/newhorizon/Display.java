package com.newhorizon;

import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.Animator;
import java.io.BufferedReader;
import java.io.InputStreamReader;



public class Display implements GLEventListener {


    private Animator animator;
    private Camera camera;

    private float frameDelta = 0;


    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    Display(){
        setup();
    }

    private void setup(){
        System.out.println("setup");
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
        window.addKeyListener(Simulation.getInstance().input);
        window.setSize((int)Settings.WIDTH, (int)Settings.HEIGHT);
        window.setTitle("New Horizon");
        final GLAutoDrawable sharedDrawable = GLDrawableFactory.getFactory(glp).createDummyAutoDrawable(null, true, caps, null);
        sharedDrawable.display(); // triggers GLContext object creation and native realization.
        GLWindow glad = GLWindow.create(caps);
        glad.setVisible(true);
        glad.setVisible(false);
        animator.setRunAsFastAsPossible(true);
        animator.start();  // start the animator loop
        Planet test = new Planet(233,128);
        RenderManager.getInstance().addPlanet(test);
        camera = new Camera();
        camera.rotate(0,-45,0);
        RenderManager.getInstance().setCamera(camera);
        RenderManager.getInstance().setContext(glad.getContext());
        Simulation.getInstance().init();
        window.setVisible(true);
        window.setSize(1,1);
        window.setSize((int)Settings.WIDTH, (int)Settings.HEIGHT);



    }


    @Override
    public void init(GLAutoDrawable drawable) {
        System.out.println("init");
        GL4 gl = drawable.getGL().getGL4();
        gl.glEnable(gl.GL_DEPTH_TEST);
        gl.glDepthFunc(gl.GL_LESS);
        gl.glEnable (gl.GL_BLEND);
        gl.glBlendFunc (gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
        gl.glPolygonMode(gl.GL_FRONT_AND_BACK, gl.GL_FILL);
        gl.glEnable(gl.GL_CULL_FACE);
        gl.glCullFace(gl.GL_BACK);


    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        System.out.println("dispose");
    }

    private void update(){

        Simulation.getInstance().update(frameDelta);

    }

    @Override
    public void display(GLAutoDrawable drawable) {
        if(RenderManager.getInstance().getContext() != null){
            update();
        }


        long start = System.currentTimeMillis();

        Render.clear(drawable);
        Render.draw(drawable);

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
