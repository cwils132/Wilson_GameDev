package com.wilson.gdx.game;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.wilson.gdx.util.CameraHelper;
import com.wilson.gdx.game.objects.Rock;
import com.wilson.gdx.util.Constants;
import com.badlogic.gdx.Game;
import com.wilson.gdx.screens.MenuScreen;

import com.badlogic.gdx.math.Rectangle;
import com.wilson.gdx.game.objects.BunnyHead;
import com.wilson.gdx.game.objects.BunnyHead.JUMP_STATE;
import com.wilson.gdx.game.objects.Feather;
import com.wilson.gdx.game.objects.GoldCoin;
import com.wilson.gdx.game.objects.Rock;

public class WorldController extends InputAdapter
{

	private static final String TAG = WorldController.class.getName();

	public Level level;
	public int lives;
	public int score;

	public CameraHelper cameraHelper;

	// Rectangles for collision detection
	private Rectangle r1 = new Rectangle();
	private Rectangle r2 = new Rectangle();

	private float timeLeftGameOverDelay;

	private Game game;

	public WorldController(Game game)
	{
		this.game = game;
		init();
	}

	private void init()
	{
		Gdx.input.setInputProcessor(this);
		cameraHelper = new CameraHelper();
		lives = Constants.LIVES_START;
		timeLeftGameOverDelay = 0;
		initLevel();
	}

	private void initLevel()
	{
		score = 0;
		level = new Level(Constants.LEVEL_01);
		cameraHelper.setTarget(level.bunnyHead);
	}

	/**
	 * Calls to CameraHelper to update the game as things change with the
	 * images. Also tells the program to cause the image rotations, and debugs
	 * as necessary.
	 * 
	 * In this case, the debug checks to make sure the program is running
	 * through the desktop.
	 * 
	 * @param deltaTime
	 */
	public void update(float deltaTime)
	{
		handleDebugInput(deltaTime);
		if (isGameOver())
		{
			timeLeftGameOverDelay -= deltaTime;
			if (timeLeftGameOverDelay < 0)
				backToMenu();
		} else
		{
			handleInputGame(deltaTime);
		}
		level.update(deltaTime);
		testCollisions();
		cameraHelper.update(deltaTime);
		if (!isGameOver() && isPlayerInWater())
		{
			lives--;
			if (isGameOver())
				timeLeftGameOverDelay = Constants.TIME_DELAY_GAME_OVER;
			else
				initLevel();
		}
	}

	public boolean isGameOver()
	{
		return lives < 0;
	}

	public boolean isPlayerInWater()
	{
		return level.bunnyHead.position.y < -5;
	}

	private void testCollisions()
	{
		r1.set(level.bunnyHead.position.x, level.bunnyHead.position.y, level.bunnyHead.bounds.width,
		        level.bunnyHead.bounds.height);

		// Test collision: Bunny Head <-> Rocks
		for (Rock rock : level.rocks)
		{
			r2.set(rock.position.x, rock.position.y, rock.bounds.width, rock.bounds.height);
			if (!r1.overlaps(r2))
				continue;
			onCollisionBunnyHeadWithRock(rock);
			// IMPORTANT: must do all collisions for valid edge testing on
			// rocks.
		}

		// Test collision: Bunny Head <-> Gold Coins
		for (GoldCoin goldcoin : level.goldcoins)
		{
			if (goldcoin.collected)
				continue;
			r2.set(goldcoin.position.x, goldcoin.position.y, goldcoin.bounds.width, goldcoin.bounds.height);
			if (!r1.overlaps(r2))
				continue;
			onCollisionBunnyWithGoldCoin(goldcoin);
			break;
		}

		// Test collision: Bunny Head <-> Feathers
		for (Feather feather : level.feathers)
		{
			if (feather.collected)
				continue;
			r2.set(feather.position.x, feather.position.y, feather.bounds.width, feather.bounds.height);
			if (!r1.overlaps(r2))
				continue;
			onCollisionBunnyWithFeather(feather);
			break;
		}
	}

	private void onCollisionBunnyHeadWithRock(Rock rock)
	{
		BunnyHead bunnyHead = level.bunnyHead;
		float heightDifference = Math.abs(bunnyHead.position.y - (rock.position.y + rock.bounds.height));
		if (heightDifference > 0.25f)
		{
			boolean hitRightEdge = bunnyHead.position.x > (rock.position.x + rock.bounds.width / 2.0f);
			if (hitRightEdge)
			{
				bunnyHead.position.x = rock.position.x + rock.bounds.width;
			} else
			{
				bunnyHead.position.x = rock.position.x - bunnyHead.bounds.width;
			}
			return;
		}

		switch (bunnyHead.jumpState)
		{
		case GROUNDED:
			break;
		case FALLING:
		case JUMP_FALLING:
			bunnyHead.position.y = rock.position.y + bunnyHead.bounds.height + bunnyHead.origin.y;
			bunnyHead.jumpState = JUMP_STATE.GROUNDED;
			break;
		case JUMP_RISING:
			bunnyHead.position.y = rock.position.y + bunnyHead.bounds.height + bunnyHead.origin.y;
			break;
		}
	}

	private void onCollisionBunnyWithGoldCoin(GoldCoin goldcoin)
	{
		goldcoin.collected = true;
		score += goldcoin.getScore();
		Gdx.app.log(TAG, "Gold coin collected");
	}

	private void onCollisionBunnyWithFeather(Feather feather)
	{
		feather.collected = true;
		score += feather.getScore();
		level.bunnyHead.setFeatherPowerup(true);
		Gdx.app.log(TAG, "Feather collected");
	}

	/**
	 * If the program is running in Desktop mode, keyboard use is enabled. The
	 * keyboard is used by called InputAdapter. By using the Keys() method
	 * LibGDX is able to tell what keyboard keys are used at each press.
	 * 
	 * When a key is pressed, the if statement checks it and performs the
	 * corresponding action. This will currently move objects around the screen
	 * or move the camera itself.
	 * 
	 * @param deltaTime
	 */
	private void handleDebugInput(float deltaTime)
	{
		if (Gdx.app.getType() != ApplicationType.Desktop)
			return;

		if (!cameraHelper.hasTarget(level.bunnyHead))
		{
			// Camera Controls (move)
			float camMoveSpeed = 5 * deltaTime;
			float camMoveSpeedAccelerationFactor = 5;
			if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT))
				camMoveSpeed *= camMoveSpeedAccelerationFactor;
			if (Gdx.input.isKeyPressed(Keys.LEFT))
				moveCamera(-camMoveSpeed, 0);
			if (Gdx.input.isKeyPressed(Keys.RIGHT))
				moveCamera(camMoveSpeed, 0);
			if (Gdx.input.isKeyPressed(Keys.UP))
				moveCamera(0, camMoveSpeed);
			if (Gdx.input.isKeyPressed(Keys.DOWN))
				moveCamera(0, -camMoveSpeed);
			if (Gdx.input.isKeyPressed(Keys.BACKSPACE))
				cameraHelper.setPosition(0, 0);
		}

		// Camera Controls (zoom)
		float camZoomSpeed = 1 * deltaTime;
		float camZoomSpeedAccelerationFactor = 5;
		if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT))
			camZoomSpeed *= camZoomSpeedAccelerationFactor;
		if (Gdx.input.isKeyPressed(Keys.COMMA))
			cameraHelper.addZoom(camZoomSpeed);
		if (Gdx.input.isKeyPressed(Keys.PERIOD))
			cameraHelper.addZoom(-camZoomSpeed);
		if (Gdx.input.isKeyPressed(Keys.SLASH))
			cameraHelper.setZoom(1);
	}

	/**
	 * This is set up to handle player inputs on the bunnyHead. When left or
	 * right is pressed it gets the terminalVelocity value of the BunnyHead
	 * object and executes movement.
	 * 
	 * (Terminal Velocity values actually stored in AbstractGameObject)
	 * 
	 * Jump is different however. The calculations are stored in BunnyHead since
	 * we don't need anything else to be able to jump when we press space. It
	 * calls to a function setJumping() and it executes depending on the
	 * situation.
	 * 
	 * @param deltaTime
	 */
	private void handleInputGame(float deltaTime)
	{
		if (cameraHelper.hasTarget(level.bunnyHead))
		{
			// Player Movement
			if (Gdx.input.isKeyPressed(Keys.LEFT))
			{
				level.bunnyHead.velocity.x = -level.bunnyHead.terminalVelocity.x;
			} else if (Gdx.input.isKeyPressed(Keys.RIGHT))
			{
				level.bunnyHead.velocity.x = level.bunnyHead.terminalVelocity.x;
			} else
			{
				// Execute auto-forward movement on non-desktop platform
				if (Gdx.app.getType() != ApplicationType.Desktop)
				{
					level.bunnyHead.velocity.x = level.bunnyHead.terminalVelocity.x;
				}
			}

			// Bunny Jump
			if (Gdx.input.isTouched() || Gdx.input.isKeyPressed(Keys.SPACE))
				level.bunnyHead.setJumping(true);
			else
				level.bunnyHead.setJumping(false);
		}
	}

	private void moveCamera(float x, float y)
	{
		x += cameraHelper.getPosition().x;
		y += cameraHelper.getPosition().y;
		cameraHelper.setPosition(x, y);
	}

	/**
	 * Further key input commands. Reset, Select next sprite, Toggle Camera
	 * follow
	 */
	@Override
	public boolean keyUp(int keycode)
	{
		// Reset game world
		if (keycode == Keys.R)
		{
			init();
			Gdx.app.debug(TAG, "Game world resetted");
		}
		// Toggle camera follow
		else if (keycode == Keys.ENTER)
		{
			cameraHelper.setTarget(cameraHelper.hasTarget() ? null : level.bunnyHead);
			Gdx.app.debug(TAG, "Camera follow enabled: " + cameraHelper.hasTarget());
		}
		// Back to Menu
		else if (keycode == Keys.ESCAPE || keycode == Keys.BACK)
		{
			backToMenu();
		}
		return false;
	}

	/**
	 * Allows us to switch back to menu whenever the player loses
	 * or hits escape.
	 */
	private void backToMenu()
	{
		// switch to menu screen
		game.setScreen(new MenuScreen(game));
	}
}