package org.microbasic.sm.part3;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;

public abstract class Light
{
	public static ShaderProgram	shaderProgram	= null;
	public static ModelBatch	modelBatch		= null;

	/**
	 * The light camera
	 */
	public Camera				camera;
	/**
	 * Associated screen
	 */
	public MainScreen			mainScreen;
	/**
	 * Position of the light
	 */
	public Vector3				position		= new Vector3();

	public Light(final MainScreen mainScreen)
	{
		this.mainScreen = mainScreen;
	}

	/**
	 * Add the uniforms to the scene shader
	 * 
	 * @param sceneShaderProgram
	 */
	public abstract void applyToShader(ShaderProgram sceneShaderProgram);

	/**
	 * Called on creation, initialize the object
	 */
	public void init()
	{
		if (shaderProgram == null)
		{
			shaderProgram = mainScreen.setupShader("depthmap");
			modelBatch = new ModelBatch(new DefaultShaderProvider()
			{
				@Override
				protected Shader createShader(final Renderable renderable)
				{
					return new DepthMapShader(renderable, shaderProgram);
				}
			});
		}
	}

	/**
	 * Create the depth map for this light
	 * 
	 * @param modelInstance
	 */
	public abstract void render(ModelInstance modelInstance);

}
