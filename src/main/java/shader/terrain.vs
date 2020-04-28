#version 430

layout (location=0) in vec3 position;
layout (location=1) in vec3 normal;
layout (location=2) in int color;

out vec3 normals;
out vec4 positionWorldSpace;
out flat int vertexColor;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 modelMatrix;

void main()
{
    normals = normal;
    vertexColor = color;
    positionWorldSpace = modelMatrix * vec4(position,1);
    gl_Position = projectionMatrix * (viewMatrix * modelMatrix) * vec4(position,1.0);

}