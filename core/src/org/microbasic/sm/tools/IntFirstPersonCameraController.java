/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.microbasic.sm.tools;

import java.awt.im.InputContext;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;

/**
 * Takes a {@link Camera} instance and controls it via w,a,s,d and mouse panning.
 * Slightly modified from the original one to understand Azerty keyboards (if locale is fr_FR or
 * fr_BE) and handle shift/ctrl to "run"
 * 
 * @author badlogic
 */
public class IntFirstPersonCameraController extends InputAdapter
{
	private final Camera	camera;
	private final IntIntMap	keys			= new IntIntMap();
	private final int		STRAFE_LEFT		= isAzertyKeyboard() ? Keys.Q : Keys.A;
	private final int		STRAFE_RIGHT	= Keys.D;
	private final int		FORWARD			= isAzertyKeyboard() ? Keys.Z : Keys.W;
	private final int		BACKWARD		= Keys.S;
	private final int		UP				= isAzertyKeyboard() ? Keys.A : Keys.Q;
	private final int		DOWN			= Keys.E;
	private float			velocity		= 5;
	private float			degreesPerPixel	= 0.5f;
	private final Vector3	tmp				= new Vector3();

	/**
	 * Try to guess if Azerty keyboard by using locale, defaults to qwerty in case of error.
	 * 
	 * @return true is keyboard should be azerty
	 */
	public static boolean isAzertyKeyboard()
	{
		if (Gdx.app.getType() != ApplicationType.Desktop)
		{
			return false;
		}
		try
		{
			final InputContext context = InputContext.getInstance();

			final String inputLocale = context.getLocale().toString();
			if (inputLocale != null && (inputLocale.equalsIgnoreCase("fr_FR") || inputLocale.equalsIgnoreCase("fr_BE")))
			{
				return true;
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return false;

	}

	public IntFirstPersonCameraController(final Camera camera)
	{
		this.camera = camera;
	}

	@Override
	public boolean keyDown(final int keycode)
	{
		keys.put(keycode, keycode);
		return true;
	}

	@Override
	public boolean keyUp(final int keycode)
	{
		keys.remove(keycode, 0);
		return true;
	}

	/**
	 * Sets the velocity in units per second for moving forward, backward and strafing left/right.
	 * 
	 * @param velocity
	 *            the velocity in units per second
	 */
	public void setVelocity(final float velocity)
	{
		this.velocity = velocity;
	}

	/**
	 * Sets how many degrees to rotate per pixel the mouse moved.
	 * 
	 * @param degreesPerPixel
	 */
	public void setDegreesPerPixel(final float degreesPerPixel)
	{
		this.degreesPerPixel = degreesPerPixel;
	}

	@Override
	public boolean touchDragged(final int screenX, final int screenY, final int pointer)
	{
		final float deltaX = -Gdx.input.getDeltaX() * degreesPerPixel;
		final float deltaY = -Gdx.input.getDeltaY() * degreesPerPixel;
		camera.direction.rotate(camera.up, deltaX);
		tmp.set(camera.direction).crs(camera.up).nor();
		camera.direction.rotate(tmp, deltaY);
		// camera.up.rotate(tmp, deltaY);
		return true;
	}

	public void update()
	{
		update(Gdx.graphics.getDeltaTime());
	}

	public void update(final float deltaTime)
	{
		final boolean run = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_LEFT);
		if (keys.containsKey(FORWARD))
		{
			tmp.set(camera.direction).nor().scl(deltaTime * velocity * (run ? 2 : 1));
			camera.position.add(tmp);
		}
		if (keys.containsKey(BACKWARD))
		{
			tmp.set(camera.direction).nor().scl(-deltaTime * velocity * (run ? 2 : 1));
			camera.position.add(tmp);
		}
		if (keys.containsKey(STRAFE_LEFT))
		{
			tmp.set(camera.direction).crs(camera.up).nor().scl(-deltaTime * velocity * (run ? 2 : 1));
			camera.position.add(tmp);
		}
		if (keys.containsKey(STRAFE_RIGHT))
		{
			tmp.set(camera.direction).crs(camera.up).nor().scl(deltaTime * velocity * (run ? 2 : 1));
			camera.position.add(tmp);
		}
		if (keys.containsKey(UP))
		{
			tmp.set(camera.up).nor().scl(deltaTime * velocity * (run ? 2 : 1));
			camera.position.add(tmp);
		}
		if (keys.containsKey(DOWN))
		{
			tmp.set(camera.up).nor().scl(-deltaTime * velocity * (run ? 2 : 1));
			camera.position.add(tmp);
		}
		camera.update(true);
	}

}
