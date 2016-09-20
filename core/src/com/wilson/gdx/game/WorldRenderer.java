package com.wilson.gdx.game;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.wilson.gdx.util.Constants;

public class WorldRenderer implements Disposable
{

	/**
	 * This class will handle all creation of rendered game
	 * objects. When we are done, it will also dispose of them
	 * for us.
	 * 
	 * We immediately create the WorldController, Camera, and Spritebatch
	 * for this game on WorldRenderers instantiation.
	 */
	private static final String TAG = WorldRenderer.class.getName();

	private OrthographicCamera camera;
	private SpriteBatch batch;
	private WorldController worldController;

	public WorldRenderer(WorldController worldController)
	{
		this.worldController = worldController;
		init();
	}

	/**
	 * Creates our SpriteBatch for the game.
	 * This does not actually draw them to the screen yet.
	 * We also initialize the OrthographicCamera and set
	 * its position, which is then updated.
	 * 
	 * OrthographicCamera is used primarily for 2D rendering.
	 * This is because it has no vanishing point, thus not
	 * able to show any real depth.
	 */
	private void init()
	{
		batch = new SpriteBatch();
		camera = new OrthographicCamera(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
		camera.position.set(0, 0, 0);
		camera.update();
	}

	public void render()
	{
		renderTestObjects();
	}

	/**
	 * SpriteBatch is actually drawn here.
	 * It must be between a Begin and End.
	 */
	private void renderTestObjects()
	{
		worldController.cameraHelper.applyTo(camera);
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		for (Sprite sprite : worldController.testSprites)
		{
			sprite.draw(batch);
		}
		batch.end();
	}

	/**
	 * Lets us resize the camera viewport, effectively zooming out.
	 * @param width
	 * @param height
	 */
	public void resize(int width, int height)
	{
		camera.viewportWidth = (Constants.VIEWPORT_HEIGHT / height) * width;
		camera.update();
	}

	/**
	 * Destroys sprites when game is closed.
	 */
	@Override
	public void dispose()
	{
		batch.dispose();
	}

}