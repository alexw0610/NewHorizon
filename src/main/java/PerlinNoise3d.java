package main.java;



import org.joml.Vector3f;


public class PerlinNoise3d {

    private int gridSize;

    private Vector3f[] gridVectors;

    public PerlinNoise3d(int gridSize){
        this.gridSize = gridSize;
        gridVectors = new Vector3f[(int)Math.pow(gridSize,3)];
        populateGridVectors();

        //TODO: Remove gridVectors and make indefinatly accessable using seed and function instead of Random
    }

    private void populateGridVectors(){

        for(int i = 0; i < Math.pow(gridSize,3);i++){

            gridVectors[i] = new Vector3f((float)Math.random(),(float)Math.random(),(float)Math.random());

        }

    }

    public float getValue(Vector3f positionOrg){

        Vector3f position = new Vector3f(positionOrg.x%gridSize, positionOrg.y%gridSize, positionOrg.z%gridSize);

        int p1x = (int)Math.floor(position.x);
        int p1y = (int)Math.floor(position.y);
        int p1z = (int)Math.floor(position.z);

        if(p1x == gridSize-1){
            p1x -= 1;
        }
        if(p1y == gridSize-1){
            p1y -= 1;
        }
        if(p1z == gridSize-1){
            p1z -= 1;
        }

        int index = p1x+(p1z*gridSize)+(p1y*(int)Math.pow(gridSize,2));

        Vector3f v1 = gridVectors[index];
        Vector3f v2 = gridVectors[index+1];
        Vector3f v3 = gridVectors[index+1+gridSize];
        Vector3f v4 = gridVectors[index+gridSize];

        Vector3f v5 = gridVectors[(int)Math.pow(gridSize,2)+index];
        Vector3f v6 = gridVectors[(int)Math.pow(gridSize,2)+index+1];
        Vector3f v7 = gridVectors[(int)Math.pow(gridSize,2)+index+1+gridSize];
        Vector3f v8 = gridVectors[(int)Math.pow(gridSize,2)+index+gridSize];

        Vector3f positionUnit = new Vector3f(positionOrg.x%1,positionOrg.y%1,positionOrg.z%1);

        float u = positionUnit.x;
        float v = positionUnit.y;
        float w = positionUnit.z;

        Vector3f pv1 = new Vector3f(positionUnit.x,positionUnit.y,positionUnit.z).sub(new Vector3f(0,0,0));
        Vector3f pv2 = new Vector3f(positionUnit.x,positionUnit.y,positionUnit.z).sub(new Vector3f(1,0,0));

        Vector3f pv3 = new Vector3f(positionUnit.x,positionUnit.y,positionUnit.z).sub(new Vector3f(1,0,1));
        Vector3f pv4 = new Vector3f(positionUnit.x,positionUnit.y,positionUnit.z).sub(new Vector3f(0,0,1));

        Vector3f pv5 = new Vector3f(positionUnit.x,positionUnit.y,positionUnit.z).sub(new Vector3f(0,1,0));
        Vector3f pv6 = new Vector3f(positionUnit.x,positionUnit.y,positionUnit.z).sub(new Vector3f(1,1,0));

        Vector3f pv7 = new Vector3f(positionUnit.x,positionUnit.y,positionUnit.z).sub(new Vector3f(1,1,1));
        Vector3f pv8 = new Vector3f(positionUnit.x,positionUnit.y,positionUnit.z).sub(new Vector3f(0,1,1));


        float pv1pv2 = lerp(v1.dot(pv1),v2.dot(pv2),u);
        float pv3pv4 = lerp(v3.dot(pv3),v4.dot(pv4),u);
        float pv5pv6 = lerp(v5.dot(pv5),v6.dot(pv6),u);
        float pv7pv8 = lerp(v7.dot(pv7),v8.dot(pv8),u);

        float pv1pv2pv3pv4 = lerp(pv1pv2,pv3pv4,w);
        float pv5pv6pv7pv8 = lerp(pv5pv6,pv7pv8,w);
        return lerp(pv1pv2pv3pv4,pv5pv6pv7pv8,v);


    }

    private float lerp(float point1, float point2, float alpha)
    {
        return point1 + alpha * (Math.abs(point2) - Math.abs(point1));
    }





}
