package com.microbasic.sm;

import com.badlogic.gdx.Game;

public class ShadowMapping extends Game
{
	@Override
	public void create()
	{
		setScreen(new com.microbasic.sm.part1.MainScreen());
		//setScreen(new com.microbasic.sm.part2.MainScreen());
		//setScreen(new com.microbasic.sm.part3.MainScreen());
		//setScreen(new com.microbasic.sm.part4.MainScreen());
	}

}
