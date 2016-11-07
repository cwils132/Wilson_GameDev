package com.wilson.gdx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.wilson.gdx.game.Assets;
import com.wilson.gdx.screens.MenuScreen;
import com.wilson.gdx.screens.DirectedGame;
import com.badlogic.gdx.math.Interpolation;
import com.wilson.gdx.screens.transitions.ScreenTransition;
import com.wilson.gdx.screens.transitions.ScreenTransitionSlice;
import com.wilson.gdx.util.AudioManager;
import com.wilson.gdx.util.GamePreferences;

public class WilsonGdxGame extends DirectedGame
{
	/**
	 * Immediately creates an instance of menu screen and creates an asset
	 * manager.
	 * 
	 * Also loads the background music through AudioManager and plays from this
	 * class so that is runs regardless of game state.
	 */
	@Override
	public void create()
	{
		// Set Libgdx log level
		Gdx.app.setLogLevel(Application.LOG_DEBUG);

		// Load assets
		Assets.instance.init(new AssetManager());

		// Load preferences for audio settings and start playing music
		GamePreferences.instance.load();
		AudioManager.instance.play(Assets.instance.music.song01);

		// Start game at menu screen
		ScreenTransition transition = ScreenTransitionSlice.init(2, ScreenTransitionSlice.UP_DOWN, 10,
		        Interpolation.pow5Out);
		setScreen(new MenuScreen(this), transition);
	}

}