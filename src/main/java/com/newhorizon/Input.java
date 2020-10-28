package com.newhorizon;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;

import java.util.BitSet;

public class Input implements KeyListener {

    private final BitSet keyStates = new BitSet(512);

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_H){
            System.out.println("h");
            RenderManager.getInstance().getActivePlanets().get(0).wireframe = !RenderManager.getInstance().getActivePlanets().get(0).wireframe;
        }
        if(!e.isAutoRepeat()){
            setKey(e.getKeyCode(),true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(!e.isAutoRepeat()){
            setKey(e.getKeyCode(),false);
        }
    }

    private void setKey(short code,boolean state){
        keyStates.set(code,state);

    }

    public boolean getKey(short code){
        return keyStates.get(code);
    }

}
