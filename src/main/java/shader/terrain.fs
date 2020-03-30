#version 430


out vec4 fragColor;

in vec4 gl_FragCoord;
in vec3 normals;
in vec4 positionWorldSpace;

vec3 lightPos = vec3(-1,-1,-1);

float lambertian(){

    return max(dot(normals,normalize(lightPos)),0.2);

}

void main(){
        vec3 center = vec3(0,0,0);
        //float dist = max(distance(center,positionWorldSpace.xyz),0);
        //vec3 c = mix(vec3(1,0,0),vec3(0,0,1),max(mod(dist,20),0)/20);
        //vec3 c2 = mix(c,vec3(0,0,1),max(dist-512,0)/5);

        vec3 color = vec3(0.0/255.0,255.0/255.0,140.0/255.0);
        float light = lambertian();
        fragColor = vec4(color*light,1);

}

