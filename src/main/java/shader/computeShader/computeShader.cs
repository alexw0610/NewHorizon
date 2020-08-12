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
layout(std430, binding = 5) buffer verticesOut
{
    float vertices[];
};
layout(std430, binding = 6) buffer indicesOut
{
    int indices[];
};

void writeVertices(int offset){
    //copy the vertices to the output. With chunk internal offsets
    int resolution = span[0];
    for(int i = 0;i<12;i++){
        vertices[i*3+offset*36] =     (verticesLookup[i*3]*resolution)+((gl_WorkGroupID.x+0.5)*resolution);          //x
        vertices[i*3+1+offset*36] =   (verticesLookup[i*3+1]*resolution)+((gl_WorkGroupID.z+0.5)*resolution);        //y
        vertices[i*3+2+offset*36] =   (verticesLookup[i*3+2]*resolution)+((gl_WorkGroupID.y+0.5)*resolution);        //z
    }
}

void writeIndices(int offset, int index){
    //copy the indices to the output. With chunk internal offsets
    for(int i = 0;i<15;i++){

        indices[offset*15+i] = indicesLookup[16*index+i] == -1 ? 0 : indicesLookup[16*index+i]+offset*12;

    }
}


int getCubeIndex(int offset, int dim,float isolevel){
    int square = dim*dim;

    int index = 0;

    if (voxelData[offset+dim] < isolevel) index |= 1;
    if (voxelData[offset+1+dim] < isolevel) index |= 2;
    if (voxelData[offset+1] < isolevel) index |= 4;
    if (voxelData[offset] < isolevel) index |=8;
    if (voxelData[offset+square+dim] < isolevel) index |= 16;
    if (voxelData[offset+square+1+dim] < isolevel) index |= 32;
    if (voxelData[offset+square+1] < isolevel) index |= 64;
    if (voxelData[offset+square] < isolevel) index |= 128;


    return index;

}

void main(){

    int indexX = int(gl_WorkGroupID.x);
    int indexY = int(gl_WorkGroupID.y);
    int indexZ = int(gl_WorkGroupID.z);
    int offset16 = indexX+(indexY*(16))+(indexZ*(16)*(16));
    int offset = indexX+(indexY*(17))+(indexZ*(17)*(17));

    int index = getCubeIndex(offset,17,0.5);
    writeVertices(offset16);
    writeIndices(offset16,index);


}
