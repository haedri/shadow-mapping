package org.microbasic.sm.part3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.UBJsonReader;
import org.microbasic.sm.tools.IntFirstPersonCameraController;
import org.microbasic.sm.tools.ScreenshotFactory;
import org.microbasic.sm.tools.SimpleTextureShader;

/**
 * Main screen
 *
 * @author jb
 */
public class MainScreen implements Screen
{
	
	private ShaderProgram                  shaderProgram;
	private ModelBatch                     modelBatch;
	private ModelInstance                  modelInstance;
	private PerspectiveCamera              camera;
	private IntFirstPersonCameraController firstPersonCameraController;
	
	private long    lastFpsDisplayed = System.currentTimeMillis();
	private long    lastScreenShot   = 0;
	public  boolean takeScreenshots  = false;
	
	public static final int DEPTHMAPSIZE = 1024;
	public Light currentLight;
	
	public MainScreen()
	{
		init();
	}
	
	@Override
	public void show()
	{
	}
	
	/**
	 * Initialize a shader, vertex shader must be named prefix_v.glsl, fragment shader must be named
	 * prefix_f.glsl
	 *
	 * @param prefix
	 * @return
	 */
	public ShaderProgram setupShader(final String prefix)
	{
		ShaderProgram.pedantic = false;
		final String packageName = getClass().getPackage().getName().substring(1 + getClass().getPackage().getName().lastIndexOf("."));
		
		final ShaderProgram shaderProgram = new ShaderProgram(Gdx.files.internal("shaders/" + packageName + "/" + prefix + "_v.glsl"), Gdx.files.internal("shaders/" + packageName + "/" + prefix
						+ "_f.glsl"));
		if (!shaderProgram.isCompiled())
		{
			System.err.println("Error with shader " + prefix + ": " + shaderProgram.getLog());
			System.exit(1);
		}
		else
		{
			Gdx.app.log("init", "Shader " + prefix + " compilled " + shaderProgram.getLog());
		}
		return shaderProgram;
	}
	
	/**
	 * Called on start
	 */
	public void init()
	{
		initCameras();
		initShaders();
		
		// Load the scene, it is just one big model
		final G3dModelLoader loader = new G3dModelLoader(new UBJsonReader());
		final Model model = loader.loadModel(Gdx.files.internal("models/scene_f0.g3db"));
		modelInstance = new ModelInstance(model);
		modelInstance.transform.setToScaling(4f, 4f, 4f);
		
		//currentLight = new DirectionalLight(this, new Vector3(33, 10, 3), new Vector3(-10, 0, 0));
		currentLight = new PointLight(this, new Vector3(-25.5f, 12.0f, -26f));
	}
	
	/**
	 * Load camera(s)
	 */
	public void initCameras()
	{
		camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.near = 1f;
		camera.far = 200;
		camera.position.set(-31, 11, 27);
		camera.lookAt(0, 11, 0);
		camera.update();
		
		firstPersonCameraController = new IntFirstPersonCameraController(camera);
		firstPersonCameraController.setVelocity(30);
		Gdx.input.setInputProcessor(firstPersonCameraController);
	}
	
	/**
	 * Load shader(s)
	 */
	public void initShaders()
	{
		shaderProgram = setupShader("scene");
		modelBatch = new ModelBatch(new DefaultShaderProvider()
		{
			@Override
			protected Shader createShader(final Renderable renderable)
			{
				return new SimpleTextureShader(renderable, shaderProgram);
			}
		});
	}
	
	/**
	 * Render the main scene, final render
	 */
	public void renderScene()
	{
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		currentLight.applyToShader(shaderProgram);
		modelBatch.begin(camera);
		modelBatch.render(modelInstance);
		modelBatch.end();
		if (takeScreenshots)
		{
			ScreenshotFactory.saveScreenshot(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), "scene");
		}
		
	}
	
	/**
	 * Render a frame
	 */
	@Override
	public void render(final float delta)
	{
		act(delta);
		currentLight.render(modelInstance);
		renderScene();
	}
	
	/**
	 * Everything that is not directly drawing but needs to be computed each frame
	 *
	 * @param delta
	 */
	public void act(final float delta)
	{
		if (System.currentTimeMillis() - lastScreenShot > 1000 * 1 && Gdx.input.isKeyJustPressed(Keys.F2))
		{
			takeScreenshots = true;
			lastScreenShot = System.currentTimeMillis();
		}
		else
		{
			takeScreenshots = false;
		}
		firstPersonCameraController.update(delta);
		if (System.currentTimeMillis() - lastFpsDisplayed > 1000 * 2)
		{
			lastFpsDisplayed = System.currentTimeMillis();
			Gdx.app.log("act", "FPS: " + Gdx.graphics.getFramesPerSecond());
		}
	}
	
	/**
	 * Window resized
	 */
	@Override
	public void resize(final int width, final int height)
	{
		camera.viewportHeight = height;
		camera.viewportWidth = width;
		camera.update();
	}
	
	@Override
	public void pause()
	{
	
	}
	
	@Override
	public void resume()
	{
	
	}
	
	@Override
	public void hide()
	{
	
	}
	
	@Override
	public void dispose()
	{
	
	}
	
}
