#version 430


out vec4 fragColor;

in vec4 gl_FragCoord;
in vec3 normals;
in vec4 positionWordSpace;

vec3 lightPos = vec3(1,1,1);

float lambertian(){

    return max(dot(normals,normalize(lightPos)),0.5);

}

void main(){

        vec3 color = vec3(positionWordSpace.x/16,positionWordSpace.y/16,positionWordSpace.z/16);
        float light = lambertian();
        fragColor = vec4(color*light,1);

}

