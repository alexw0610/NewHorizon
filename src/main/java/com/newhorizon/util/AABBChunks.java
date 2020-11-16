package com.newhorizon.util;

import org.joml.Vector3i;

import java.util.LinkedList;

public class AABBChunks {

    public static LinkedList<Vector3i> getCubes(Vector3i pos, Vector3i target){

        LinkedList<Vector3i> list = new LinkedList<>();

        int xSign = pos.x > target.x ? -1: 1;
        int ySign = pos.y > target.y ? -1: 1;
        int zSign = pos.z > target.z ? -1: 1;

        int tempx = Math.abs(pos.x()-target.x);
        int tempy = Math.abs(pos.y()-target.y);
        int tempz = Math.abs(pos.z()-target.z);


        for(int x = 0; x < tempx+1;x++){
            for(int y = 0; y < tempy+1;y++){
                for(int z = 0; z < tempz+1;z++){
                    list.add(new Vector3i(xSign == 1 ? pos.x()+x:pos.x()-x,ySign == 1 ? pos.y()+x:pos.y()-y,zSign == 1 ? pos.z()+z:pos.z()-z));
                }
            }
        }
        return list;
    }
}
