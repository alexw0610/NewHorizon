#version 430

out vec4 fragColor;

in vec4 gl_FragCoord;
in vec4 positionWorldSpace;
in float vertexColor;


void main(){
        float dist = distance(positionWorldSpace.xyz,vec3(0,0,0))/30;
        fragColor = vec4(1,1,1,1/((dist*dist)));
}

