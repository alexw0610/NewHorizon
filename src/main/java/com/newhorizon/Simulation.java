package com.newhorizon;

import com.jogamp.newt.event.KeyEvent;
import com.newhorizon.util.Chunkify;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class Simulation {

    public static Simulation instance = new Simulation();
    public TerrainLoaderAsync terrainLoader;
    public static Input input = new Input();

    private Simulation(){

    }
    public void init(){
        terrainLoader = new TerrainLoaderAsync();
    }
    public static Simulation getInstance(){
        return instance;
    }



    public void update(float frameDelta){
        updatePlanets();
        updateCameraGravity(frameDelta);
        updateCameraInput(frameDelta);
        RenderManager.getInstance().getActiveCamera().applyMomentum();




    }

    private void updatePlanets(){
        Camera camera = RenderManager.getInstance().getActiveCamera();
        Vector3i newChunk = camera.getChunk();
        Vector3i oldChunk = Chunkify.getChunk(terrainLoader.getPosition());

        if(!newChunk.equals(oldChunk)){
            if(terrainLoader.requestTerrain(camera.getPosition())){
            }
        }
    }

    private void updateCameraInput(float frameDelta){

        Camera camera = RenderManager.getInstance().getActiveCamera();

        float delta = 0.05f*frameDelta;
        float viewDelta = -0.1f*frameDelta;


        if(input.getKey(KeyEvent.VK_W)){
            camera.addMomentum(0,0,delta);
        }
        if(input.getKey(KeyEvent.VK_S)){
            camera.addMomentum(0,0,-delta);
        }
        if(input.getKey(KeyEvent.VK_A)){
            camera.addMomentum(delta,0,0);
        }
        if(input.getKey(KeyEvent.VK_D)){
            camera.addMomentum(-delta,0,0);
        }

        //ROTATION

        if(input.getKey(KeyEvent.VK_Q)){
            camera.rotate(0,0,viewDelta);
        }
        if(input.getKey(KeyEvent.VK_E)){
            camera.rotate(0,0,-viewDelta);
        }
        if(input.getKey(KeyEvent.VK_LEFT)){
            camera.rotate(0,viewDelta,0);
        }
        if(input.getKey(KeyEvent.VK_RIGHT)){
            camera.rotate(0,-viewDelta,0);
        }
        if(input.getKey(KeyEvent.VK_UP)){
            camera.rotate(viewDelta,0,0);
        }
        if(input.getKey(KeyEvent.VK_DOWN)){
            camera.rotate(-viewDelta,0,0);
        }


    }

    private void updateCameraGravity(float frameDelta){
        Camera camera = RenderManager.getInstance().getActiveCamera();
        Planet planet = RenderManager.getInstance().getActivePlanets().get(0);
        Vector3f gravVector = new Vector3f(planet.position).sub(camera.getPosition());
        gravVector.normalize().mul(0.00001f);
        //camera.addMomentumVector(gravVector.mul(frameDelta));
    }

}
