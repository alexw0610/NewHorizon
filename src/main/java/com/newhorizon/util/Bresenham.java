package com.newhorizon.util;

import org.joml.Vector3i;

import java.util.LinkedList;

public class Bresenham {

    public static LinkedList<Vector3i> getPoints(Vector3i start, Vector3i end, int thickness){
        LinkedList<Vector3i> list = new LinkedList<>();
        list.add(start);
        int x1 = start.x;
        int x2 = end.x;

        int y1 = start.y;
        int y2 = end.y;

        int z1 = start.z;
        int z2 = end.z;

        int xs;
        int ys;
        int zs;
        int dx = Math.abs(end.x - start.x);
        int dy = Math.abs(end.y - start.y);
        int dz = Math.abs(end.z - start.z);

        if (x2 > x1){
            xs = 1;
        }else{
            xs = -1;
        }
        if (y2 > y1){
            ys = 1;
        }else{
            ys = -1;
        }
        if (z2 > z1){
            zs = 1;
        }else{
            zs = -1;
        }



        int p1;
        int p2;

        //Driving axis is X-axis

        if (dx >= dy && dx >= dz){
            p1 = 2 * dy - dx;
            p2 = 2 * dz - dx;
            while (x1 != x2){
                x1 += xs;
                if (p1 >= 0){
                    y1 += ys;
                    p1 -= 2 * dx;
                }
                if (p2 >= 0){
                    z1 += zs;
                    p2 -= 2 * dx;
                }
                p1 += 2 * dy;
                p2 += 2 * dz;

                list.add(new Vector3i(x1, y1, z1));
                if(thickness > 1){
                    for(int x = -1*thickness ; x <= 1*thickness;x++){
                        for(int y = -1*thickness ; x <= 1*thickness;x++){
                            for(int z = -1*thickness ; x <= 1*thickness;x++){
                                Vector3i temp = new Vector3i(x1+x,y2+y,z1+z);
                                if(!list.contains(temp)){
                                    list.add(temp);
                                }
                            }
                        }
                    }
                }
            }

        }else if(dy >= dx && dy >= dz){
            p1 = 2 * dx - dy;
            p2 = 2 * dz - dy;
            while (y1 != y2){
                y1 += ys;
                if (p1 >= 0){
                    x1 += xs;
                    p1 -= 2 * dy;
                }

                if (p2 >= 0){
                    z1 += zs;
                    p2 -= 2 * dy;
                }

                p1 += 2 * dx;
                p2 += 2 * dz;
                list.add(new Vector3i(x1, y1, z1));
                if(thickness > 1){
                    for(int x = -1*thickness ; x <= 1*thickness;x++){
                        for(int y = -1*thickness ; x <= 1*thickness;x++){
                            for(int z = -1*thickness ; x <= 1*thickness;x++){
                                Vector3i temp = new Vector3i(x1+x,y2+y,z1+z);
                                if(!list.contains(temp)){
                                    list.add(temp);
                                }
                            }
                        }
                    }
                }
            }

        }else{
            p1 = 2 * dy - dz;
            p2 = 2 * dx - dz;
            while (z1 != z2){
                z1 += zs;
                if (p1 >= 0){
                    y1 += ys;
                    p1 -= 2 * dz;
                }

                if (p2 >= 0){
                    x1 += xs;
                    p2 -= 2 * dz;
                }
                p1 += 2 * dy;
                p2 += 2 * dx;
                list.add(new Vector3i(x1, y1, z1));
                if(thickness > 1){
                    for(int x = -1*thickness ; x <= 1*thickness;x++){
                        for(int y = -1*thickness ; x <= 1*thickness;x++){
                            for(int z = -1*thickness ; x <= 1*thickness;x++){
                                Vector3i temp = new Vector3i(x1+x,y2+y,z1+z);
                                if(!list.contains(temp)){
                                    list.add(temp);
                                }
                            }
                        }
                    }
                }
            }

        }

        return list;

    }
}
