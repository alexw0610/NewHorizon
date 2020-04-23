#version 430


out vec4 fragColor;

in vec4 gl_FragCoord;
in vec3 normals;
in vec4 positionWorldSpace;

vec3 lightPos = vec3(-1,-1,-1);

float lambertian(){

    return max(dot(normals,normalize(lightPos)),0.5);

}

void main(){
        vec3 center = vec3(8192.0,8192.0,8192.0);
        float dist = distance(center,positionWorldSpace.xyz);


        vec3 purple = vec3(153.0/255.0,0.0/255.0,255.0/255.0);
        vec3 orange = vec3(255.0/255.0,102.0/255.0,0.0/255.0);
        vec3 green = vec3(68.0/255.0,255.0/255.0,0.0/255.0);
        float relDist = dist-7632; //0-560
        vec3 colorA = mix(mix(purple,orange,relDist/186),mix(orange,green,relDist/280),relDist/560);

        float light = lambertian();
        fragColor = vec4(colorA*light,1);

}

