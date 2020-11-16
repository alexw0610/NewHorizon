package com.newhorizon;

import com.jogamp.opengl.GLContext;

import java.util.LinkedList;

public class RenderManager {

    private static RenderManager instance = new RenderManager();
    private LinkedList<Planet> planetList = new LinkedList<>();
    private GLContext context;
    private Camera activeCamera;

    private RenderManager(){}

    public static RenderManager getInstance(){
        return instance;
    }

    protected void setCamera(Camera camera){
        this.activeCamera = camera;
    }

    protected void addPlanet(Planet planet){
        synchronized(planetList){
            planetList.add(planet);
        }

    }
    public void setContext(GLContext context){
        this.context = context;
    }
    public GLContext getContext(){
        return this.context;
    }
    protected LinkedList<Planet> getActivePlanets(){
        return planetList;
    }
    protected Camera getActiveCamera(){
        return this.activeCamera;
    }
}
