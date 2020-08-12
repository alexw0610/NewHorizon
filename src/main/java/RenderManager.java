package main.java;

import java.util.LinkedList;

public class RenderManager {
    private static RenderManager instance = new RenderManager();
    private LinkedList<Planet> planetList = new LinkedList<>();
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

    protected LinkedList<Planet> getActivePlanets(){
        return planetList;
    }
    protected Camera getActiveCamera(){
        return this.activeCamera;
    }
}
