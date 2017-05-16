package org.microbasic.sm;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ShadowMapping extends Game
{
	@Override
	public void create()
	{
		//setScreen(new org.microbasic.sm.part1.MainScreen());
		setScreen(new org.microbasic.sm.part2.MainScreen());
		//setScreen(new org.microbasic.sm.part3.MainScreen());
		//setScreen(new org.microbasic.sm.part4.MainScreen());
	}
	
}
