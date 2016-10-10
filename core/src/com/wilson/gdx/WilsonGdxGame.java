package com.wilson.gdx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.wilson.gdx.game.Assets;
import com.wilson.gdx.screens.MenuScreen;

public class WilsonGdxGame extends Game
{
	/**
	 * Immediately creates an instance of menu screen and creates
	 * an asset manager.
	 */
	@Override
	public void create()
	{
		// Set Libgdx log level
		Gdx.app.setLogLevel(Application.LOG_DEBUG);

		// Load assets
		Assets.instance.init(new AssetManager());

		// Start game at menu screen
		setScreen(new MenuScreen(this));
	}

}