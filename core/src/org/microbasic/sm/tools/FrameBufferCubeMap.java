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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;

/**
 * <p>
 * Encapsulates OpenGL ES 2.0 frame buffer objects. This is a simple helper class which should cover
 * most FBO uses. It will automatically create a texture for the color attachment and a renderbuffer
 * for the depth buffer. You can get a hold of the texture by
 * {@link FrameBuffer#getColorBufferTexture()}. This class will only work with OpenGL ES 2.0.
 * </p>
 * 
 * <p>
 * FrameBuffers are managed. In case of an OpenGL context loss, which only happens on Android when a
 * user switches to another application or receives an incoming call, the framebuffer will be
 * automatically recreated.
 * </p>
 * 
 * <p>
 * A FrameBuffer must be disposed if it is no longer needed
 * </p>
 * 
 * @author mzechner
 */
public class FrameBufferCubeMap implements Disposable
{
	/** the frame buffers **/
	private final static Map<Application, Array<FrameBufferCubeMap>>	buffers								= new HashMap<Application, Array<FrameBufferCubeMap>>();

	/** the color buffer texture **/
	protected Cubemap													colorTexture;

	/** the default framebuffer handle, a.k.a screen. */
	private static int													defaultFramebufferHandle;
	/** true if we have polled for the default handle already. */
	private static boolean												defaultFramebufferHandleInitialized	= false;

	/** the framebuffer handle **/
	private int															framebufferHandle;

	/** the depthbuffer render object handle **/
	private int															depthbufferHandle;

	/** the stencilbuffer render object handle **/
	private int															stencilbufferHandle;

	/** width **/
	protected final int													width;

	/** height **/
	protected final int													height;

	/** depth **/
	protected final boolean												hasDepth;

	/** stencil **/
	protected final boolean												hasStencil;

	/** format **/
	protected final Pixmap.Format										format;

	/**
	 * Creates a new FrameBuffer having the given dimensions and potentially a depth buffer
	 * attached.
	 * 
	 * @param format
	 * @param width
	 * @param height
	 * @param hasDepth
	 */
	public FrameBufferCubeMap(final Pixmap.Format format, final int width, final boolean hasDepth)
	{
		this(format, width, width, hasDepth, false);
	}

	/**
	 * Creates a new FrameBuffer having the given dimensions and potentially a depth and a stencil
	 * buffer attached.
	 * 
	 * @param format
	 *            the format of the color buffer; according to the OpenGL ES 2.0 spec, only RGB565,
	 *            RGBA4444 and RGB5_A1 are
	 *            color-renderable
	 * @param width
	 *            the width of the framebuffer in pixels
	 * @param height
	 *            the height of the framebuffer in pixels
	 * @param hasDepth
	 *            whether to attach a depth buffer
	 * @throws com.badlogic.gdx.utils.GdxRuntimeException
	 *             in case the FrameBuffer could not be created
	 */
	public FrameBufferCubeMap(final Pixmap.Format format, final int width, final int height, final boolean hasDepth, final boolean hasStencil)
	{
		this.width = width;
		this.height = height;
		this.format = format;
		this.hasDepth = hasDepth;
		this.hasStencil = hasStencil;
		build();

		addManagedFrameBuffer(Gdx.app, this);
	}

	/** Override this method in a derived class to set up the backing texture as you like. */
	protected void setupTexture()
	{
		//Texture olorTexture = new Texture(width, height, format);
		colorTexture = new Cubemap(width, height, width, format);
		colorTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		colorTexture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
	}

	private void build()
	{
		final GL20 gl = Gdx.gl20;

		// iOS uses a different framebuffer handle! (not necessarily 0)
		if (!defaultFramebufferHandleInitialized)
		{
			defaultFramebufferHandleInitialized = true;
			if (Gdx.app.getType() == ApplicationType.iOS)
			{
				final IntBuffer intbuf = ByteBuffer.allocateDirect(16 * Integer.SIZE / 8).order(ByteOrder.nativeOrder()).asIntBuffer();
				gl.glGetIntegerv(GL20.GL_FRAMEBUFFER_BINDING, intbuf);
				defaultFramebufferHandle = intbuf.get(0);
			}
			else
			{
				defaultFramebufferHandle = 0;
			}
		}

		setupTexture();

		final IntBuffer handle = BufferUtils.newIntBuffer(1);
		gl.glGenFramebuffers(1, handle);
		framebufferHandle = handle.get(0);

		if (hasDepth)
		{
			handle.clear();
			gl.glGenRenderbuffers(1, handle);
			depthbufferHandle = handle.get(0);
		}

		if (hasStencil)
		{
			handle.clear();
			gl.glGenRenderbuffers(1, handle);
			stencilbufferHandle = handle.get(0);
		}

		gl.glBindTexture(GL20.GL_TEXTURE_CUBE_MAP, colorTexture.getTextureObjectHandle());

		if (hasDepth)
		{
			gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, depthbufferHandle);
			gl.glRenderbufferStorage(GL20.GL_RENDERBUFFER, GL20.GL_DEPTH_COMPONENT16, colorTexture.getWidth(), colorTexture.getHeight());
		}

		if (hasStencil)
		{
			gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, stencilbufferHandle);
			gl.glRenderbufferStorage(GL20.GL_RENDERBUFFER, GL20.GL_STENCIL_INDEX8, colorTexture.getWidth(), colorTexture.getHeight());
		}

		gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebufferHandle);
		gl.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0, GL20.GL_TEXTURE_CUBE_MAP, colorTexture.getTextureObjectHandle(), 0);
		if (hasDepth)
		{
			gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_DEPTH_ATTACHMENT, GL20.GL_RENDERBUFFER, depthbufferHandle);
		}

		if (hasStencil)
		{
			gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_STENCIL_ATTACHMENT, GL20.GL_RENDERBUFFER, stencilbufferHandle);
		}

		final int result = gl.glCheckFramebufferStatus(GL20.GL_FRAMEBUFFER);

		gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, 0);
		gl.glBindTexture(GL20.GL_TEXTURE_CUBE_MAP, 0);
		gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, defaultFramebufferHandle);

		/*for (int i = 0; i < 6; i++)
		{
			gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL20.GL_ALPHA, width, height, 0, GL20.GL_ALPHA, GL20.GL_UNSIGNED_BYTE, null);
		}*/

		if (result != GL20.GL_FRAMEBUFFER_COMPLETE)
		{
			colorTexture.dispose();
			if (hasDepth)
			{
				handle.clear();
				handle.put(depthbufferHandle);
				handle.flip();
				gl.glDeleteRenderbuffers(1, handle);
			}

			if (hasStencil)
			{
				handle.clear();
				handle.put(stencilbufferHandle);
				handle.flip();
				gl.glDeleteRenderbuffers(1, handle);
			}

			handle.clear();
			handle.put(framebufferHandle);
			handle.flip();
			gl.glDeleteFramebuffers(1, handle);

			if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT)
			{
				throw new IllegalStateException("frame buffer couldn't be constructed: incomplete attachment");
			}
			if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS)
			{
				throw new IllegalStateException("frame buffer couldn't be constructed: incomplete dimensions");
			}
			if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT)
			{
				throw new IllegalStateException("frame buffer couldn't be constructed: missing attachment");
			}
			if (result == GL20.GL_FRAMEBUFFER_UNSUPPORTED)
			{
				throw new IllegalStateException("frame buffer couldn't be constructed: unsupported combination of formats");
			}
			throw new IllegalStateException("frame buffer couldn't be constructed: unknown error " + result);
		}
	}

	/** Releases all resources associated with the FrameBuffer. */
	@Override
	public void dispose()
	{
		final GL20 gl = Gdx.gl20;

		final IntBuffer handle = BufferUtils.newIntBuffer(1);

		colorTexture.dispose();
		if (hasDepth)
		{
			handle.put(depthbufferHandle);
			handle.flip();
			gl.glDeleteRenderbuffers(1, handle);
		}

		if (hasStencil)
		{
			handle.put(stencilbufferHandle);
			handle.flip();
			gl.glDeleteRenderbuffers(1, handle);
		}

		handle.clear();
		handle.put(framebufferHandle);
		handle.flip();
		gl.glDeleteFramebuffers(1, handle);

		if (buffers.get(Gdx.app) != null)
		{
			buffers.get(Gdx.app).removeValue(this, true);
		}
	}

	/** Makes the frame buffer current so everything gets drawn to it. */
	public void bind(final int side)
	{
		Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebufferHandle);
		Gdx.gl20.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0, side, colorTexture.getTextureObjectHandle(), 0);
	}

	/**
	 * Unbinds the framebuffer, all drawing will be performed to the normal framebuffer from here
	 * on.
	 */
	public static void unbind()
	{
		Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, defaultFramebufferHandle);
	}

	/**
	 * Binds the frame buffer and sets the viewport accordingly, so everything gets drawn to it.
	 * 
	 * @param side
	 *            the side of the cube we are currently drawing on
	 * @param camera
	 *            adjust camera properties for this side
	 */
	public void begin(final Cubemap.CubemapSide side, final Camera camera)
	{
		begin(side.glEnum);
		if (camera != null)
		{
			switch (side)
			{
			case NegativeX:
				camera.up.set(0, -1, 0);
				camera.direction.set(-1, 0, 0);
				break;
			case NegativeY:
				camera.up.set(0, 0, -1);
				camera.direction.set(0, -1, 0);
				break;
			case NegativeZ:
				camera.up.set(0, -1, 0);
				camera.direction.set(0, 0, -1);
				break;
			case PositiveX:
				camera.up.set(0, -1, 0);
				camera.direction.set(1, 0, 0);
				break;
			case PositiveY:
				camera.up.set(0, 0, 1);
				camera.direction.set(0, 1, 0);
				break;
			case PositiveZ:
				camera.up.set(0, -1, 0);
				camera.direction.set(0, 0, 1);
				break;
			default:
				break;
			}
			camera.update();
		}
	}

	/** Binds the frame buffer and sets the viewport accordingly, so everything gets drawn to it. */
	private void begin(final int side)
	{
		bind(side);
		setFrameBufferViewport();
	}

	/** Sets viewport to the dimensions of framebuffer. Called by {@link #begin()}. */
	protected void setFrameBufferViewport()
	{
		Gdx.gl20.glViewport(0, 0, colorTexture.getWidth(), colorTexture.getHeight());
	}

	/**
	 * Unbinds the framebuffer, all drawing will be performed to the normal framebuffer from here
	 * on.
	 */
	public void end()
	{
		unbind();
		setDefaultFrameBufferViewport();
	}

	/** Sets viewport to the dimensions of default framebuffer (window). Called by {@link #end()}. */
	protected void setDefaultFrameBufferViewport()
	{
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	/**
	 * Unbinds the framebuffer and sets viewport sizes, all drawing will be performed to the normal
	 * framebuffer from here on.
	 * 
	 * @param x
	 *            the x-axis position of the viewport in pixels
	 * @param y
	 *            the y-asis position of the viewport in pixels
	 * @param width
	 *            the width of the viewport in pixels
	 * @param height
	 *            the height of the viewport in pixels
	 */
	public void end(final int x, final int y, final int width, final int height)
	{
		unbind();
		Gdx.gl20.glViewport(x, y, width, height);
	}

	/** @return the color buffer texture */
	public Cubemap getColorBufferTexture()
	{
		return colorTexture;
	}

	/** @return the height of the framebuffer in pixels */
	public int getHeight()
	{
		return colorTexture.getHeight();
	}

	/** @return the width of the framebuffer in pixels */
	public int getWidth()
	{
		return colorTexture.getWidth();
	}

	private static void addManagedFrameBuffer(final Application app, final FrameBufferCubeMap frameBuffer)
	{
		Array<FrameBufferCubeMap> managedResources = buffers.get(app);
		if (managedResources == null)
		{
			managedResources = new Array<FrameBufferCubeMap>();
		}
		managedResources.add(frameBuffer);
		buffers.put(app, managedResources);
	}

	/**
	 * Invalidates all frame buffers. This can be used when the OpenGL context is lost to rebuild
	 * all managed frame buffers. This
	 * assumes that the texture attached to this buffer has already been rebuild! Use with care.
	 */
	public static void invalidateAllFrameBuffers(final Application app)
	{
		if (Gdx.gl20 == null)
		{
			return;
		}

		final Array<FrameBufferCubeMap> bufferArray = buffers.get(app);
		if (bufferArray == null)
		{
			return;
		}
		for (int i = 0; i < bufferArray.size; i++)
		{
			bufferArray.get(i).build();
		}
	}

	public static void clearAllFrameBuffers(final Application app)
	{
		buffers.remove(app);
	}

	public static StringBuilder getManagedStatus(final StringBuilder builder)
	{
		builder.append("Managed buffers/app: { ");
		for (final Application app : buffers.keySet())
		{
			builder.append(buffers.get(app).size);
			builder.append(" ");
		}
		builder.append("}");
		return builder;
	}

	public static String getManagedStatus()
	{
		return getManagedStatus(new StringBuilder()).toString();
	}
}
