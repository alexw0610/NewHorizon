package com.newhorizon;

import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.Animator;


public class NewHorizon implements GLEventListener {


    private Animator animator;
    private Camera camera;
    private float frameDelta = 0;

    NewHorizon(){
        setup();
    }

    private void setup(){

        GLProfile glp = GLProfile.get("GL4");
        GLCapabilities caps = new GLCapabilities(glp);
        GLWindow window = GLWindow.create(caps);
        this.animator = new Animator(window);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDestroyNotify(WindowEvent arg0) {
                new Thread(() -> {
                    if (animator.isStarted())
                        animator.stop();
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
        //TODO: Hotfix for a display error.
        window.setSize(1,1);
        window.setSize((int)Settings.WIDTH, (int)Settings.HEIGHT);



    }


    @Override
    public void init(GLAutoDrawable drawable) {

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

    public static void main(String[] args) {
        new NewHorizon();
    }

}
