package com.wilson.gdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.wilson.gdx.util.Constants;
import com.wilson.gdx.util.GamePreferences;


public class WorldRenderer implements Disposable
{
	/**
	 * This class will handle all creation of rendered game objects. When we are
	 * done, it will also dispose of them for us.
	 * 
	 * We immediately create the WorldController, Camera, and Spritebatch for
	 * this game on WorldRenderers instantiation.
	 */
	private static final String TAG = WorldRenderer.class.getName();

	private static final boolean DEBUG_DRAW_BOX2D_WORLD = true;

	private OrthographicCamera camera;
	private OrthographicCamera cameraGUI;
	private SpriteBatch batch;
	private WorldController worldController;
	private Box2DDebugRenderer b2debugRenderer;

	/**
	 * Creates a shader that turns the game grayscale.
	 */
	private ShaderProgram shaderMonochrome;

	public WorldRenderer(WorldController worldController)
	{
		this.worldController = worldController;
		init();
	}
	/**
	 * Creates our SpriteBatch for the game. This does not actually draw them to
	 * the screen yet. We also initialize the OrthographicCamera and set its
	 * position, which is then updated.
	 * 
	 * OrthographicCamera is used primarily for 2D rendering. This is because it
	 * has no vanishing point, thus not able to show any real depth.
	 */
	private void init()
	{
		batch = new SpriteBatch();
		camera = new OrthographicCamera(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
		camera.position.set(0, 0, 0);
		camera.update();
		cameraGUI = new OrthographicCamera(Constants.VIEWPORT_GUI_WIDTH, Constants.VIEWPORT_GUI_HEIGHT);
		cameraGUI.position.set(0, 0, 0);
		cameraGUI.setToOrtho(true); // flip y-axis
		cameraGUI.update();
		b2debugRenderer = new Box2DDebugRenderer();
		shaderMonochrome = new ShaderProgram(Gdx.files.internal(Constants.shaderMonochromeVertex),
		        Gdx.files.internal(Constants.shaderMonochromeFragment));
		if (!shaderMonochrome.isCompiled())
		{
			String msg = "Could not compile shader program: " + shaderMonochrome.getLog();
			throw new GdxRuntimeException(msg);
		}
	}

	public void render()
	{
		renderWorld(batch);
		renderGui(batch);
	}
	/**
	 * Initiates the world rendering and calls to the other classes to perform
	 * their duties.
	 * 
	 * @param batch
	 */
	private void renderWorld(SpriteBatch batch)
	{
		worldController.cameraHelper.applyTo(camera);
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		if (GamePreferences.instance.useMonochromeShader)
		{
			batch.setShader(shaderMonochrome);
			shaderMonochrome.setUniformf("u_amount", 1.0f);
		}
		worldController.level.render(batch);
		batch.setShader(null);
		batch.end();
		if (DEBUG_DRAW_BOX2D_WORLD)
		{
			b2debugRenderer.render(worldController.b2world, camera.combined);
		}
	}
	/**
	 * This creates the GUI. To do this is makes a camera specifically for GUI
	 * elements that will not move position on our screen when the character
	 * moves.
	 * 
	 * @param batch
	 */
	private void renderGui(SpriteBatch batch)
	{
		batch.setProjectionMatrix(cameraGUI.combined);
		batch.begin();

		// draw collected gold coins icon + text (anchored to top left edge)
		renderGuiScore(batch);
		// draw collected feather icon (anchored to top left edge)
		renderGuiFeatherPowerup(batch);
		// draw extra lives icon + text (anchored to top right edge)
		renderGuiExtraLive(batch);
		// draw FPS text (anchored to bottom right edge)
		if (GamePreferences.instance.showFpsCounter)
			renderGuiFpsCounter(batch);
		// draw game over text
		renderGuiGameOverMessage(batch);

		batch.end();
	}
	/**
	 * Adds the score to our game
	 * 
	 * @param batch
	 */
	private void renderGuiScore(SpriteBatch batch)
	{
		float x = -15;
		float y = -15;
		float offsetX = 50;
		float offsetY = 50;
		if (worldController.scoreVisual < worldController.score)
		{
			long shakeAlpha = System.currentTimeMillis() % 360;
			float shakeDist = 1.5f;
			offsetX += MathUtils.sinDeg(shakeAlpha * 2.2f) * shakeDist;
			offsetY += MathUtils.sinDeg(shakeAlpha * 2.9f) * shakeDist;
		}
		batch.draw(Assets.instance.goldCoin.goldCoin, x, y, offsetX, offsetY, 100, 100, 0.35f, -0.35f, 0);
		Assets.instance.fonts.defaultBig.draw(batch, "" + (int) worldController.scoreVisual, x + 75, y + 37);
	}

	private void renderGuiFeatherPowerup(SpriteBatch batch)
	{
		float x = -15;
		float y = 30;
		float timeLeftFeatherPowerup = worldController.level.bunnyHead.timeLeftFeatherPowerup;
		if (timeLeftFeatherPowerup > 0)
		{
			// Start icon fade in/out if the left power-up time
			// is less than 4 seconds. The fade interval is set
			// to 5 changes per second.
			if (timeLeftFeatherPowerup < 4)
			{
				if (((int) (timeLeftFeatherPowerup * 5) % 2) != 0)
				{
					batch.setColor(1, 1, 1, 0.5f);
				}
			}
			batch.draw(Assets.instance.feather.feather, x, y, 50, 50, 100, 100, 0.35f, -0.35f, 0);
			batch.setColor(1, 1, 1, 1);
			Assets.instance.fonts.defaultSmall.draw(batch, "" + (int) timeLeftFeatherPowerup, x + 60, y + 57);
		}
	}
	/**
	 * Shows bunny heads depicting our total lives left
	 * 
	 * Whenever a life is lost, the icons alpha value is slowly increased to
	 * create a fading effect
	 * 
	 * @param batch
	 */
	private void renderGuiExtraLive(SpriteBatch batch)
	{
		float x = cameraGUI.viewportWidth - 50 - Constants.LIVES_START * 50;
		float y = -15;
		for (int i = 0; i < Constants.LIVES_START; i++)
		{
			if (worldController.lives <= i)
				batch.setColor(0.5f, 0.5f, 0.5f, 0.5f);
			batch.draw(Assets.instance.bunny.head, x + i * 50, y, 50, 50, 120, 100, 0.35f, -0.35f, 0);
			batch.setColor(1, 1, 1, 1);
		}
		if (worldController.lives >= 0 && worldController.livesVisual > worldController.lives)
		{
			int i = worldController.lives;
			float alphaColor = Math.max(0, worldController.livesVisual - worldController.lives - 0.5f);
			float alphaScale = 0.35f * (2 + worldController.lives - worldController.livesVisual) * 2;
			float alphaRotate = -45 * alphaColor;
			batch.setColor(1.0f, 0.7f, 0.7f, alphaColor);
			batch.draw(Assets.instance.bunny.head, x + i * 50, y, 50, 50, 120, 100, alphaScale, -alphaScale,
			        alphaRotate);
			batch.setColor(1, 1, 1, 1);
		}
	}
	/**
	 * Keeps track of our FPS
	 * 
	 * @param batch
	 */
	private void renderGuiFpsCounter(SpriteBatch batch)
	{
		float x = cameraGUI.viewportWidth - 55;
		float y = cameraGUI.viewportHeight - 15;
		int fps = Gdx.graphics.getFramesPerSecond();
		BitmapFont fpsFont = Assets.instance.fonts.defaultNormal;
		if (fps >= 45)
		{
			// 45 or more FPS show up in green
			fpsFont.setColor(0, 1, 0, 1);
		} else if (fps >= 30)
		{
			// 30 or more FPS show up in yellow
			fpsFont.setColor(1, 1, 0, 1);
		} else
		{
			// less than 30 FPS show up in red
			fpsFont.setColor(1, 0, 0, 1);
		}

		fpsFont.draw(batch, "FPS: " + fps, x, y);
		fpsFont.setColor(1, 1, 1, 1); // white
	}

	private void renderGuiGameOverMessage(SpriteBatch batch)
	{
		float x = cameraGUI.viewportWidth / 2;
		float y = cameraGUI.viewportHeight / 2;
		if (worldController.isGameOver())
		{
			BitmapFont fontGameOver = Assets.instance.fonts.defaultBig;
			fontGameOver.setColor(1, 0.75f, 0.25f, 1);
			fontGameOver.draw(batch, "GAME OVER", x, y, 0, Align.center, false);
			fontGameOver.setColor(1, 1, 1, 1);
		}
	}
	/**
	 * Lets us resize the camera viewport, effectively zooming out.
	 * 
	 * @param width
	 * @param height
	 */
	public void resize(int width, int height)
	{
		camera.viewportWidth = (Constants.VIEWPORT_HEIGHT / (float) height) * (float) width;
		camera.update();
		cameraGUI.viewportHeight = Constants.VIEWPORT_GUI_HEIGHT;
		cameraGUI.viewportWidth = (Constants.VIEWPORT_GUI_HEIGHT / (float) height) * (float) width;
		cameraGUI.position.set(cameraGUI.viewportWidth / 2, cameraGUI.viewportHeight / 2, 0);
		cameraGUI.update();
	}
	/**
	 * Destroys sprites when game is closed. Also destroys monochrome shader.
	 */
	@Override
	public void dispose()
	{
		batch.dispose();
		shaderMonochrome.dispose();
	}

}