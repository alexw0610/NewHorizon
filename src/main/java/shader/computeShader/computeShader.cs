#version 460

layout (local_size_x = 1, local_size_y = 1, local_size_z = 1) in;

//input
layout(std430, binding = 1) buffer indicesIn
{
    int indicesLookup[];
};
layout(std430, binding = 2) buffer verticesIn
{
    float verticesLookup[];
};
layout(std430, binding = 4) buffer dataIn
{
    float voxelData[];
};
layout(std140, binding = 3) buffer spanIn
{
    int span[];
};

//output
layout(std430
, binding = 5) buffer verticesOut
{
    float vertices[];
};
layout(std430, binding = 6) buffer indicesOut
{
    int indices[];
};
layout(std430, binding = 7) buffer normalOut
{
    float normals[];
};
layout(std430, binding = 8) buffer countOut
{
    int count[];
};

float getInterpCoord(float a, float b, float iso){

        float ad = iso-a;
        float bd = iso-b;
        float adAbs = abs(ad);
        float range = adAbs+abs(bd);
        return (adAbs*(1.0/range))-0.5;

    }

void writeVertices(int offset16, int offset, int dim, float isolevel){
    //copy the vertices to the output. With chunk internal offsets
    int resolution = span[0];
    int square = dim*dim;
    for(int i = 0;i<12;i++){
        if(i == 0){
            vertices[i*3+offset16*36] =  getInterpCoord(voxelData[offset+dim],voxelData[offset+1+dim],isolevel)*resolution+((gl_WorkGroupID.x+0.5)*resolution);
            vertices[i*3+1+offset16*36] =   (verticesLookup[i*3+1])*resolution+((gl_WorkGroupID.z+0.5)*resolution);        //y
            vertices[i*3+2+offset16*36] =   (verticesLookup[i*3+2])*resolution+((gl_WorkGroupID.y+0.5)*resolution);        //z

        }else if(i == 1){
            vertices[i*3+offset16*36] =     (verticesLookup[i*3])*resolution+((gl_WorkGroupID.x+0.5)*resolution);          //x
            vertices[i*3+1+offset16*36] =   (verticesLookup[i*3+1])*resolution+((gl_WorkGroupID.z+0.5)*resolution);        //y
            vertices[i*3+2+offset16*36] =  getInterpCoord(voxelData[offset+1],voxelData[offset+1+dim],isolevel)*resolution+((gl_WorkGroupID.y+0.5)*resolution);

        }else if(i == 2){
            vertices[i*3+offset16*36] =  getInterpCoord(voxelData[offset],voxelData[offset+1],isolevel)*resolution+((gl_WorkGroupID.x+0.5)*resolution);
            vertices[i*3+1+offset16*36] =   (verticesLookup[i*3+1])*resolution+((gl_WorkGroupID.z+0.5)*resolution);        //y
            vertices[i*3+2+offset16*36] =   (verticesLookup[i*3+2])*resolution+((gl_WorkGroupID.y+0.5)*resolution);        //z

        }else if(i == 3){
            vertices[i*3+offset16*36] =     (verticesLookup[i*3])*resolution+((gl_WorkGroupID.x+0.5)*resolution);          //x
            vertices[i*3+1+offset16*36] =   (verticesLookup[i*3+1])*resolution+((gl_WorkGroupID.z+0.5)*resolution);        //y
            vertices[i*3+2+offset16*36] =  getInterpCoord(voxelData[offset],voxelData[offset+dim],isolevel)*resolution+((gl_WorkGroupID.y+0.5)*resolution);

        }else if(i == 4){
            vertices[i*3+offset16*36] =  getInterpCoord(voxelData[offset+square+dim],voxelData[offset+square+1+dim],isolevel)*resolution+((gl_WorkGroupID.x+0.5)*resolution);
            vertices[i*3+1+offset16*36] =   (verticesLookup[i*3+1])*resolution+((gl_WorkGroupID.z+0.5)*resolution);        //y
            vertices[i*3+2+offset16*36] =   (verticesLookup[i*3+2])*resolution+((gl_WorkGroupID.y+0.5)*resolution);        //z

        }else if(i == 5){
            vertices[i*3+offset16*36] =     (verticesLookup[i*3])*resolution+((gl_WorkGroupID.x+0.5)*resolution);          //x
            vertices[i*3+1+offset16*36] =   (verticesLookup[i*3+1])*resolution+((gl_WorkGroupID.z+0.5)*resolution);        //y
            vertices[i*3+2+offset16*36] =  getInterpCoord(voxelData[offset+square+1],voxelData[offset+square+1+dim],isolevel)*resolution+((gl_WorkGroupID.y+0.5)*resolution);

        }else if(i == 6){
            vertices[i*3+offset16*36] =  getInterpCoord(voxelData[offset+square],voxelData[offset+square+1],isolevel)*resolution+((gl_WorkGroupID.x+0.5)*resolution);
            vertices[i*3+1+offset16*36] =   (verticesLookup[i*3+1])*resolution+((gl_WorkGroupID.z+0.5)*resolution);        //y
            vertices[i*3+2+offset16*36] =   (verticesLookup[i*3+2])*resolution+((gl_WorkGroupID.y+0.5)*resolution);        //z

        }else if(i == 7){
            vertices[i*3+offset16*36] =     (verticesLookup[i*3])*resolution+((gl_WorkGroupID.x+0.5)*resolution);          //x
            vertices[i*3+1+offset16*36] =   (verticesLookup[i*3+1])*resolution+((gl_WorkGroupID.z+0.5)*resolution);        //y
            vertices[i*3+2+offset16*36] = getInterpCoord(voxelData[offset+square],voxelData[offset+square+dim],isolevel)*resolution+((gl_WorkGroupID.y+0.5)*resolution);

        }else if(i == 8){
            vertices[i*3+offset16*36] =     (verticesLookup[i*3])*resolution+((gl_WorkGroupID.x+0.5)*resolution);
            vertices[i*3+1+offset16*36] =  getInterpCoord(voxelData[offset+dim],voxelData[offset+square+dim],isolevel)*resolution+((gl_WorkGroupID.z+0.5)*resolution);
            vertices[i*3+2+offset16*36] =   (verticesLookup[i*3+2])*resolution+((gl_WorkGroupID.y+0.5)*resolution);        //z

        }else if(i == 9){
            vertices[i*3+offset16*36] =     (verticesLookup[i*3])*resolution+((gl_WorkGroupID.x+0.5)*resolution);          //x
            vertices[i*3+1+offset16*36] =  getInterpCoord(voxelData[offset+1+dim],voxelData[offset+square+1+dim],isolevel)*resolution+((gl_WorkGroupID.z+0.5)*resolution);
            vertices[i*3+2+offset16*36] =   (verticesLookup[i*3+2])*resolution+((gl_WorkGroupID.y+0.5)*resolution);        //z

        }else if(i == 10){
            vertices[i*3+offset16*36] =     (verticesLookup[i*3])*resolution+((gl_WorkGroupID.x+0.5)*resolution);          //x
            vertices[i*3+1+offset16*36] =  getInterpCoord(voxelData[offset+1],voxelData[offset+square+1],isolevel)*resolution+((gl_WorkGroupID.z+0.5)*resolution);
            vertices[i*3+2+offset16*36] =   (verticesLookup[i*3+2])*resolution+((gl_WorkGroupID.y+0.5)*resolution);        //z

        }else if(i == 11){
            vertices[i*3+offset16*36] =     (verticesLookup[i*3])*resolution+((gl_WorkGroupID.x+0.5)*resolution);          //x
            vertices[i*3+1+offset16*36] =  getInterpCoord(voxelData[offset],voxelData[offset+square],isolevel)*resolution+((gl_WorkGroupID.z+0.5)*resolution);
            vertices[i*3+2+offset16*36] =   (verticesLookup[i*3+2])*resolution+((gl_WorkGroupID.y+0.5)*resolution);        //z

        }

    }


}
void writeNormals(int offset,int count){

    for(int i = 0; i < count/3;i++){
        vec3 pointA = vec3(vertices[offset*36+(indices[offset*15+i*3]*3)],vertices[offset*36+(indices[offset*15+i*3]*3)+1],vertices[offset*36+(indices[offset*15+i*3]*3)+2]);
        vec3 pointB = vec3(vertices[offset*36+(indices[offset*15+i*3+1]*3)],vertices[offset*36+(indices[offset*15+i*3+1]*3)+1],vertices[offset*36+(indices[offset*15+i*3+1]*3)+2]);
        vec3 pointC = vec3(vertices[offset*36+(indices[offset*15+i*3+2]*3)],vertices[offset*36+(indices[offset*15+i*3+2]*3)+1],vertices[offset*36+(indices[offset*15+i*3+2]*3)+2]);

        vec3 e1 = pointA - pointB;
        vec3 e2 = pointA - pointC;
        vec3 normal = cross(normalize(e1),normalize(e2));
        //normal = normalize(normal);


        normals[offset*36+(indices[offset*15+i*3]*3)] = (normal.x);
        normals[offset*36+(indices[offset*15+i*3]*3)+1] = (normal.y);
        normals[offset*36+(indices[offset*15+i*3]*3)+2] = (normal.z);

        normals[offset*36+(indices[offset*15+i*3+1]*3)] = (normal.x);
        normals[offset*36+(indices[offset*15+i*3+1]*3)+1] = (normal.y);
        normals[offset*36+(indices[offset*15+i*3+1]*3)+2] = (normal.z);

        normals[offset*36+(indices[offset*15+i*3+2]*3)] = (normal.x);
        normals[offset*36+(indices[offset*15+i*3+2]*3)+1] = (normal.y);
        normals[offset*36+(indices[offset*15+i*3+2]*3)+2] = (normal.z);

    }

}
int writeIndices(int offset, int index){
    //copy the indices to the output. With chunk internal offsets
    int counter = 0;
    for(int i = 0;i<15;i++){
        int ind = indicesLookup[16*index+i];
        if(ind != -1){
            indices[offset*15+i] = ind;
            counter++;
        }
    }
    count[offset] = counter;
    return counter;
}


int getCubeIndex(int offset, int dim,float isolevel){
    int square = dim*dim;

    int index = 0;

    if (voxelData[offset+dim] < isolevel) index |= 1;               //0
    if (voxelData[offset+1+dim] < isolevel) index |= 2;             //1
    if (voxelData[offset+1] < isolevel) index |= 4;                 //2
    if (voxelData[offset] < isolevel) index |=8;                    //3
    if (voxelData[offset+square+dim] < isolevel) index |= 16;       //4
    if (voxelData[offset+square+1+dim] < isolevel) index |= 32;     //5
    if (voxelData[offset+square+1] < isolevel) index |= 64;         //6
    if (voxelData[offset+square] < isolevel) index |= 128;          //7


    return index;

}

void main(){

    int indexX = int(gl_WorkGroupID.x);
    int indexY = int(gl_WorkGroupID.y);
    int indexZ = int(gl_WorkGroupID.z);
    int offset16 = indexX+(indexY*(16))+(indexZ*(16)*(16));
    int offset = indexX+(indexY*(17))+(indexZ*(17)*(17));

    int index = getCubeIndex(offset,17,0.5);
    writeVertices(offset16,offset,17,0.5);
    int count = writeIndices(offset16,index);
    writeNormals(offset16,count);


}
