package com.wilson.gdx.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.wilson.gdx.game.Assets;
import com.badlogic.gdx.InputProcessor;

public abstract class AbstractGameScreen implements Screen
{

	/**
	 * This abstract class provides methods for GameScreen and MenuScreen. Keeps
	 * WorldController and WorldRenderer from being directly used by the main
	 * class.
	 */
	protected DirectedGame game;

	public AbstractGameScreen(DirectedGame game)
	{
		this.game = game;
	}

	public abstract void render(float deltaTime);

	public abstract void resize(int width, int height);

	public abstract void show();

	public abstract void hide();

	public abstract void pause();

	public abstract InputProcessor getInputProcessor();

	public void resume()
	{
		Assets.instance.init(new AssetManager());
	}

	public void dispose()
	{
		Assets.instance.dispose();
	}

}