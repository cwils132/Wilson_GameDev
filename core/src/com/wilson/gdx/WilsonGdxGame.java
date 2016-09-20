package com.wilson.gdx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.wilson.gdx.game.Assets;
import com.wilson.gdx.game.WorldController;
import com.wilson.gdx.game.WorldRenderer;

public class WilsonGdxGame implements ApplicationListener
{
	/**
	 * Tags are used for error reporting. This helps quickly
	 * track down where our program is failing.
	 */
	private static final String TAG = WilsonGdxGame.class.getName();

	private WorldController worldController;
	private WorldRenderer worldRenderer;

	private boolean paused;

	@Override
	public void create()
	{
		// Set Libgdx log level to DEBUG
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		// Load assets
		Assets.instance.init(new AssetManager());
		// Initialize controller and renderer
		worldController = new WorldController();
		worldRenderer = new WorldRenderer(worldController);
	}

	@Override
	public void render()
	{
		// Do not update game world when paused.
		if (!paused)
		{
			// Update game world by the time that has passed
			// since last rendered frame.
			worldController.update(Gdx.graphics.getDeltaTime());
		}
		// Sets the clear screen color to: Cornflower Blue
		Gdx.gl.glClearColor(0x64 / 255.0f, 0x95 / 255.0f, 0xed / 255.0f, 0xff / 255.0f);
		// Clears the screen
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		// Render game world to screen
		worldRenderer.render();
	}

	@Override
	public void resize(int width, int height)
	{
		worldRenderer.resize(width, height);
	}

	/**
	 * pause() and resume() are good for memory management
	 * in Android applications. For desktop it serves
	 * only to pause the game.
	 */
	@Override
	public void pause()
	{
		paused = true;
	}

	@Override
	public void resume()
	{
		Assets.instance.init(new AssetManager());
		paused = false;
	}

	/**
	 * Because LibGDX is made with C as the backbone, we need
	 * to call dispose on our own. Javas garbage collector
	 * will not work.
	 */
	@Override
	public void dispose()
	{
		worldRenderer.dispose();
		Assets.instance.dispose();
	}

}