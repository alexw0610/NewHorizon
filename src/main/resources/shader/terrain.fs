#version 430

out vec4 fragColor;
in vec4 gl_FragCoord;
in vec3 normals;
in vec4 positionWorldSpace;
in float vertexColor;

uniform vec3 cameraPos;
uniform float atmoDensity;
uniform vec3 atmoColor;

float multiplier = 2.0f;
vec3 lightPos = vec3(-1,-1,-1);

float lambertian(){

    return max(dot(normals,normalize(lightPos)),0.5);

}
vec4 atmosphere(){

    return (vec4(atmoColor,0)*distance(positionWorldSpace.xyz,cameraPos)*atmoDensity);
}

void main(){
        vec3 center = vec3(512.0,512.0,512.0)*multiplier;
        float dist = distance(center,positionWorldSpace.xyz);
        vec3 brown = vec3(173.0/255.0,94.0/255.0,55.0/255.0);
        vec3 green = vec3(134.0/255.0,250.0/255.0,92.0/255.0);
        vec3 gray = vec3(155.0/255.0,166.0/255.0,250.0/255.0);
        vec3 white = vec3(240.0/255.0,240.0/255.0,255.0/255.0);
        float relDist = dist; //0-560
        //vec3 colorA = vec3(positionWorldSpace.x/1024,positionWorldSpace.y/1024,positionWorldSpace.z/1024);
        vec3 colorA = vec3(0,0,0);
        if(relDist<330*multiplier){
            colorA = brown;
        }else if(relDist<338*multiplier){
            colorA = green;
        }else if(relDist<344*multiplier){
            colorA = gray;
        }else if(relDist<400*multiplier){
            colorA = white;
        }
        float light = lambertian();
        fragColor = vec4(colorA*light,1);
        fragColor = fragColor+atmosphere();





}

