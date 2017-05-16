package org.microbasic.sm.part2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import org.microbasic.sm.tools.ScreenshotFactory;

public class DirectionalLight extends Light
{

	public Vector3		direction;
	public FrameBuffer	frameBuffer;
	public Texture		depthMap;

	public DirectionalLight(final MainScreen mainScreen, final Vector3 position, final Vector3 direction)
	{
		super(mainScreen);
		this.position = position;
		this.direction = direction;
		init();
	}

	@Override
	public void applyToShader(final ShaderProgram sceneShaderProgram)
	{
		sceneShaderProgram.begin();
		final int textureNum = 2;
		depthMap.bind(textureNum);
		sceneShaderProgram.setUniformi("u_depthMap", textureNum);
		sceneShaderProgram.setUniformMatrix("u_lightTrans", camera.combined);
		sceneShaderProgram.setUniformf("u_cameraFar", camera.far);
		sceneShaderProgram.setUniformf("u_lightPosition", camera.position);
		sceneShaderProgram.end();
	}

	@Override
	public void init()
	{
		super.init();

		camera = new PerspectiveCamera(120f, MainScreen.DEPTHMAPSIZE, MainScreen.DEPTHMAPSIZE);
		camera.near = 1f;
		camera.far = 70;
		camera.position.set(position);
		camera.lookAt(direction);
		camera.update();
	}

	@Override
	public void render(final ModelInstance modelInstance)
	{

		if (frameBuffer == null)
		{
			frameBuffer = new FrameBuffer(Format.RGBA8888, MainScreen.DEPTHMAPSIZE, MainScreen.DEPTHMAPSIZE, true);
		}
		frameBuffer.begin();
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		shaderProgram.begin();
		shaderProgram.setUniformf("u_cameraFar", camera.far);
		shaderProgram.setUniformf("u_lightPosition", camera.position);
		shaderProgram.end();

		modelBatch.begin(camera);
		modelBatch.render(modelInstance);
		modelBatch.end();

		if (mainScreen.takeScreenshots)
		{
			ScreenshotFactory.saveScreenshot(frameBuffer.getWidth(), frameBuffer.getHeight(), "depthmap");
		}
		frameBuffer.end();
		depthMap = frameBuffer.getColorBufferTexture();
	}

}
