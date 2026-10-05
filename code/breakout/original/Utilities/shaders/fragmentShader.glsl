#version 440 core

struct Material 
{
    vec3 ambientColor;
    float ambientStrength;
    vec3 diffuseColor;
    vec3 specularColor;
    float shininess;
}; 

struct LightSource 
{
    vec3 position;	
    vec3 ambientColor;
    vec3 diffuseColor;
    vec3 specularColor;
    float focalStrength;
    float specularIntensity;
};

#define TOTAL_LIGHTS 4

in vec3 fragmentPosition;
in vec3 fragmentVertexNormal;
in vec2 fragmentTextureCoordinate;

out vec4 outFragmentColor;

uniform bool bUseTexture = false;
uniform bool bUseLighting = false;
uniform vec4 objectColor = vec4(1.0f);
uniform sampler2D objectTexture;
uniform vec3 viewPosition;
uniform vec2 UVscale = vec2(1.0f, 1.0f);
uniform LightSource lightSources[TOTAL_LIGHTS];
uniform Material material;
uniform sampler2D objectTexture2;
uniform float blendFactor = 0.0f;
uniform bool bUseTexture2 = false;

// function prototypes
vec3 CalcLightSource(LightSource light, vec3 lightNormal, vec3 vertexPosition, vec3 viewDirection);

void main()
{
    if (bUseLighting == true)
    {
        vec3 lightNormal = normalize(fragmentVertexNormal);
        vec3 viewDirection = normalize(viewPosition - fragmentPosition);
        vec3 phongResult = vec3(0.0f);

        for (int i = 0; i < TOTAL_LIGHTS; i++)
        {
            phongResult += CalcLightSource(lightSources[i], lightNormal, fragmentPosition, viewDirection);
        }

        if (bUseTexture == true)
        {
            vec4 textureColor = texture(objectTexture, fragmentTextureCoordinate * UVscale);

            if (bUseTexture2 == true)
            {
                vec4 textureColor2 = texture(objectTexture2, fragmentTextureCoordinate * UVscale);
                textureColor = mix(textureColor, textureColor2, blendFactor);
            }

            outFragmentColor = vec4(phongResult * textureColor.xyz, 1.0);
        }
        else
        {
            outFragmentColor = vec4(phongResult * objectColor.xyz, objectColor.w);
        }
    }
    else
    {
        if (bUseTexture == true)
        {
            vec4 textureColor = texture(objectTexture, fragmentTextureCoordinate * UVscale);

            if (bUseTexture2 == true)
            {
                vec4 textureColor2 = texture(objectTexture2, fragmentTextureCoordinate * UVscale);
                textureColor = mix(textureColor, textureColor2, blendFactor);
            }

            outFragmentColor = textureColor;
        }
        else
        {
            outFragmentColor = objectColor;  // also restored this missing fallback
        }
    }  // ? was missing
}      // ? was missing

// calculates the color when using a directional light.
vec3 CalcLightSource(LightSource light, vec3 lightNormal, vec3 vertexPosition, vec3 viewDirection)
{
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    // Ambient
    ambient = light.ambientColor + (material.ambientColor * material.ambientStrength);

    // Diffuse
    vec3 lightDirection = normalize(light.position - vertexPosition);
    float impact = max(dot(lightNormal, lightDirection), 0.0);
    diffuse = impact * material.diffuseColor;

    // Specular
    vec3 reflectDir = reflect(-lightDirection, lightNormal);
    float specularComponent = pow(max(dot(viewDirection, reflectDir), 0.0), light.focalStrength);
    specular = (light.specularIntensity * material.shininess) * specularComponent * material.specularColor;

    return (ambient + diffuse + specular);
}