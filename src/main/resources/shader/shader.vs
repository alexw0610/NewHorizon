#version 430

layout (location=0) in vec3 position;
layout (location=1) in vec3 normals;
layout (location=2) in int color;

out vec4 positionWorldSpace;
out flat int vertexColor;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform vec3 camera;

void main()
{
    vec3 positionCameraSpace = vec3(position.x+floor(camera.x/8)*8,position.y+floor(camera.y/8)*8,position.z+floor(camera.z/8)*8);
    vertexColor = color;
    positionWorldSpace = viewMatrix * vec4(positionCameraSpace-vec3(16*8,16*8,16*8),1);
    gl_Position = projectionMatrix * viewMatrix * vec4(positionCameraSpace-vec3(16*8,16*8,16*8),1.0);

}