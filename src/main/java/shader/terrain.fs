#version 430

out vec4 fragColor;
in vec4 gl_FragCoord;
in vec3 normals;
in vec4 positionWorldSpace;
in float vertexColor;

uniform vec3 cameraPos;
uniform float atmoDensity;
uniform vec3 atmoColor;

vec3 lightPos = vec3(-1,-1,-1);

float lambertian(){

    return max(dot(normals,normalize(lightPos)),0.5);

}
vec4 atmosphere(){

    return (vec4(atmoColor,0)*distance(positionWorldSpace.xyz,cameraPos)*atmoDensity);
}

void main(){
        vec3 center = vec3(8192.0,8192.0,8192.0);
        float dist = distance(center,positionWorldSpace.xyz);

        vec3 purple = vec3(153.0/255.0,0.0/255.0,255.0/255.0);
        vec3 orange = vec3(255.0/255.0,102.0/255.0,0.0/255.0);
        vec3 green = vec3(68.0/255.0,255.0/255.0,0.0/255.0);
        float relDist = dist-7632; //0-560
        vec3 colorA = mix(mix(purple,orange,relDist/186),mix(orange,green,relDist/280),relDist/560);
        //float dista = max(log(max(distance(positionWorldSpace.xyz,vec3(16.0*64.0,16.0*32.0,16.0*32.0))-128.0,1)),1.0);
        float light = lambertian();
        //if(vertexColor == 0){
            //fragColor = vec4(vec3(170.0/255.0,146.0/255.0,121.0/255.0)*light,1);
        //}else if(vertexColor == 1){
            //fragColor = vec4(vec3(154.0/255.0,198.0/255.0,135.0/255.0)*light,1);
        //}else if(vertexColor == 2){
            //fragColor = vec4(vec3(152.0/255.0,161.0/255.0,183.0/255.0)*light,1);
        //}
        fragColor = vec4(mix(purple,orange,vertexColor)*light,1);
        fragColor = fragColor+atmosphere();





}

