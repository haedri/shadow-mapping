#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

uniform sampler2D u_diffuseTexture;
varying vec2 v_texCoords0;
varying float v_intensity;

void main()
{
	vec4 finalColor  = texture2D(u_diffuseTexture, v_texCoords0);	
	finalColor.rgb   = finalColor.rgb*v_intensity;
	gl_FragColor     = finalColor;
	
}

