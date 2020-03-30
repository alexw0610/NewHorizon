#version 430

layout (location=0) in vec3 position;

out vec4 positionWorldSpace;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;


void main()
{

    positionWorldSpace = viewMatrix * vec4(position,1);
    gl_Position = projectionMatrix * viewMatrix * vec4(position,1.0);

}