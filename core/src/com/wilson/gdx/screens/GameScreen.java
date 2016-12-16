package com.wilson.gdx.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.wilson.gdx.game.WorldController;
import com.wilson.gdx.game.WorldRenderer;
import com.wilson.gdx.util.GamePreferences;

public class GameScreen extends AbstractGameScreen
{

	/**
	 * Since we decoupled the main class from the rest of the program,
	 * this class now controls the Game screen.
	 */
	private static final String TAG = GameScreen.class.getName();

	private WorldController worldController;
	private WorldRenderer worldRenderer;

	private boolean paused;
	
	String level;

	public GameScreen(Game game)
	{
		super(game);
	}

	@Override
	public void render(float deltaTime)
	{
		// Do not update game world when paused.
		if (!paused)
		{
			// Update game world by the time that has passed
			// since last rendered frame.
			worldController.update(deltaTime);
		}
		// Sets the clear screen color to: Cornflower Blue
		Gdx.gl.glClearColor(0.5f, 0.5f, 0.0f, 0.0f);
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
	 * This class makes sure the game screen always uses the most recent
	 * settings chosen.
	 */
	@Override
	public void show()
	{
		GamePreferences.instance.load();
		worldController = new WorldController(game);
		worldRenderer = new WorldRenderer(worldController);
		Gdx.input.setCatchBackKey(true);
	}

	@Override
	public void hide()
	{
		worldRenderer.dispose();
		Gdx.input.setCatchBackKey(false);
	}

	@Override
	public void pause()
	{
		paused = true;
	}

	@Override
	public void resume()
	{
		super.resume();
		// Only called on Android!
		paused = false;
	}

}