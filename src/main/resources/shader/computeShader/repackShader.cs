#version 460

layout (local_size_x = 1, local_size_y = 1, local_size_z = 1) in;

//input
layout(std430, binding = 5) buffer verticesIn
{
    float verticesInput[];
};
layout(std430, binding = 6) buffer indicesIn
{
    int indicesInput[];
};
layout(std430, binding = 7) buffer normalsIn
{
    float normalsInput[];
};
layout(std430, binding = 8) buffer countIn
{
    int countInput[];
};

//output
layout(std430, binding = 9) buffer verticesOut
{
    float verticesOutput[];
};
layout(std430, binding = 10) buffer indicesOut
{
    int indicesOutput[];
};
layout(std430, binding = 11) buffer normalsOut
{
    float normalsOutput[];
};



void main(){
    int counter = 0;
    int indOffset = 0;
    int vertOffset = 0;


    for(int i = 0; i < countInput.length();i++){
        int amount = countInput[i];
        if(amount > 0){
            int indOffsetTemp = 0;
            int vertOffsetTemp = 0;
            for(int items = 0; items < amount; items++){
                indicesOutput[indOffset+items] = indicesInput[items+i*15]+counter*12;
                indOffsetTemp++;
            }
            for(int verts = 0; verts < 36; verts++){
                verticesOutput[vertOffset+verts] = verticesInput[verts+i*36];
                normalsOutput[vertOffset+verts] = normalsInput[verts+i*36];
                vertOffsetTemp++;
            }
            indOffset += indOffsetTemp;
            vertOffset += vertOffsetTemp;
            counter++;
        }
    }

}
