package com.wilson.gdx.game;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.wilson.gdx.util.CameraHelper;
import com.wilson.gdx.game.objects.Rock;
import com.wilson.gdx.util.Constants;
import com.badlogic.gdx.Game;
import com.wilson.gdx.screens.MenuScreen;
import com.wilson.gdx.screens.DirectedGame;
import com.badlogic.gdx.math.Interpolation;
import com.wilson.gdx.screens.transitions.ScreenTransition;
import com.wilson.gdx.screens.transitions.ScreenTransitionSlide;
import com.wilson.gdx.util.AudioManager;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.wilson.gdx.game.objects.Carrot;
import com.badlogic.gdx.utils.Disposable;

import com.badlogic.gdx.math.Rectangle;
import com.wilson.gdx.game.objects.BunnyHead;
import com.wilson.gdx.game.objects.BunnyHead.JUMP_STATE;
import com.wilson.gdx.game.objects.Feather;
import com.wilson.gdx.game.objects.GoldCoin;
import com.wilson.gdx.game.objects.Rock;

public class WorldController extends InputAdapter implements Disposable
{

	private static final String TAG = WorldController.class.getName();

	private DirectedGame game;
	public Level level;
	public int lives;
	public float livesVisual;
	public int score;
	public float scoreVisual;
	private boolean goalReached;

	public CameraHelper cameraHelper;

	// Rectangles for collision detection
	private Rectangle r1 = new Rectangle();
	private Rectangle r2 = new Rectangle();

	private float timeLeftGameOverDelay;

	/**
	 * Creates a Box2D world for the physics engine
	 */
	public World b2world;

	public WorldController(DirectedGame game)
	{
		this.game = game;
		init();
	}

	private void init()
	{
		cameraHelper = new CameraHelper();
		lives = Constants.LIVES_START;
		livesVisual = lives;
		timeLeftGameOverDelay = 0;
		initLevel();
	}

	private void initLevel()
	{
		score = 0;
		scoreVisual = score;
		goalReached = false;
		level = new Level(Constants.LEVEL_01);
		cameraHelper.setTarget(level.bunnyHead);
		initPhysics();
	}

	/**
	 * Creates the physics for Box2D in the game. Gives rocks a body polygon
	 * shape for use in collision detection with BunnyHead.
	 */
	private void initPhysics()
	{
		if (b2world != null)
			b2world.dispose();
		b2world = new World(new Vector2(0, -9.81f), true);
		// Rocks
		Vector2 origin = new Vector2();
		for (Rock rock : level.rocks)
		{
			BodyDef bodyDef = new BodyDef();
			bodyDef.type = BodyType.KinematicBody;
			bodyDef.position.set(rock.position);
			Body body = b2world.createBody(bodyDef);
			rock.body = body;
			PolygonShape polygonShape = new PolygonShape();
			origin.x = rock.bounds.width / 2.0f;
			origin.y = rock.bounds.height / 2.0f;
			polygonShape.setAsBox(rock.bounds.width / 2.0f, rock.bounds.height / 2.0f, origin, 0);
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = polygonShape;
			body.createFixture(fixtureDef);
			polygonShape.dispose();
		}
	}

	/**
	 * Updates the game for use in tracking the camera, playing sounds, checking
	 * if the player fell in water, updating the level, and other various tasks.
	 * Also updates if lives are lost and displays if the game is over.
	 * 
	 * @param deltaTime
	 */
	public void update(float deltaTime)
	{
		handleDebugInput(deltaTime);
		if (isGameOver() || goalReached)
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
		b2world.step(deltaTime, 8, 3);
		cameraHelper.update(deltaTime);
		if (!isGameOver() && isPlayerInWater())
		{
			AudioManager.instance.play(Assets.instance.sounds.liveLost);
			lives--;
			if (isGameOver())
				timeLeftGameOverDelay = Constants.TIME_DELAY_GAME_OVER;
			else
				initLevel();
		}
		level.mountains.updateScrollPosition(cameraHelper.getPosition());
		if (livesVisual > lives)
			livesVisual = Math.max(lives, livesVisual - 1 * deltaTime);
		if (scoreVisual < score)
			scoreVisual = Math.min(score, scoreVisual + 250 * deltaTime);
	}

	/**
	 * Check to see if the game is over.
	 * 
	 * @return
	 */
	public boolean isGameOver()
	{
		return lives < 0;
	}

	/**
	 * If the player falls, return the player to the beginning position of the
	 * level.
	 * 
	 * @return
	 */
	public boolean isPlayerInWater()
	{
		return level.bunnyHead.position.y < -5;
	}

	/**
	 * Monitors for various collision situations and calls to the appropriate
	 * methods to execute actions.
	 */
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

		// Test collision: Bunny Head <-> Goal
		if (!goalReached)
		{
			r2.set(level.goal.bounds);
			r2.x += level.goal.position.x;
			r2.y += level.goal.position.y;
			if (r1.overlaps(r2))
				onCollisionBunnyWithGoal();
		}
	}

	/**
	 * Determines collision with rocks so the BunnyHead does not pass through
	 * the rocks and fall when it is not supposed to.
	 * 
	 * @param rock
	 */
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

	/**
	 * If the player collides with a coin the score is increased, a sound is
	 * played, and the coin object is destroyed.
	 * 
	 * @param goldcoin
	 */
	private void onCollisionBunnyWithGoldCoin(GoldCoin goldcoin)
	{
		goldcoin.collected = true;
		AudioManager.instance.play(Assets.instance.sounds.pickupCoin);
		score += goldcoin.getScore();
		Gdx.app.log(TAG, "Gold coin collected");
	}

	/**
	 * If the character collides with a feather the feather is destroyed, the
	 * player gains a powerup, a sound is played, and score is increased.
	 * 
	 * @param feather
	 */
	private void onCollisionBunnyWithFeather(Feather feather)
	{
		feather.collected = true;
		AudioManager.instance.play(Assets.instance.sounds.pickupFeather);
		score += feather.getScore();
		level.bunnyHead.setFeatherPowerup(true);
		Gdx.app.log(TAG, "Feather collected");
	}

	/**
	 * If the player comes in contact with the Goal object carrots are spawned
	 * and rained down based on the players location.
	 */
	private void onCollisionBunnyWithGoal()
	{
		goalReached = true;
		timeLeftGameOverDelay = Constants.TIME_DELAY_GAME_FINISHED;
		Vector2 centerPosBunnyHead = new Vector2(level.bunnyHead.position);
		centerPosBunnyHead.x += level.bunnyHead.bounds.width;
		spawnCarrots(centerPosBunnyHead, Constants.CARROTS_SPAWN_MAX, Constants.CARROTS_SPAWN_RADIUS);
	}

	/**
	 * Handles debug movement with the camera to view level.
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
	 * Moves character if left arrow button is pressed. If right arrow is
	 * pressed, move right. Space jumps.
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
				level.bunnyHead.velocity.x = level.bunnyHead.terminalVelocity.x;
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

	private void backToMenu()
	{
		// switch to menu screen
		ScreenTransition transition = ScreenTransitionSlide.init(0.75f, ScreenTransitionSlide.DOWN, false,
		        Interpolation.bounceOut);
		game.setScreen(new MenuScreen(game), transition);
	}

	@Override
	public void dispose()
	{
		if (b2world != null)
			b2world.dispose();
	}

	/**
	 * Spawns carrots when the player reaches the goal. The amount of carrots
	 * generated is determined by a cariable in the Constants class.
	 * 
	 * When carrots are made they are put in to an array of carrots. Each carrot
	 * has a body so that when it collides with the ground it bounces off.
	 * 
	 * @param pos
	 * @param numCarrots
	 * @param radius
	 */
	private void spawnCarrots(Vector2 pos, int numCarrots, float radius)
	{
		float carrotShapeScale = 0.5f;
		// create carrots with box2d body and fixture
		for (int i = 0; i < numCarrots; i++)
		{
			Carrot carrot = new Carrot();
			// calculate random spawn position, rotation, and scale
			float x = MathUtils.random(-radius, radius);
			float y = MathUtils.random(5.0f, 15.0f);
			float rotation = MathUtils.random(0.0f, 360.0f) * MathUtils.degreesToRadians;
			float carrotScale = MathUtils.random(0.5f, 1.5f);
			carrot.scale.set(carrotScale, carrotScale);
			// create box2d body for carrot with start position
			// and angle of rotation
			BodyDef bodyDef = new BodyDef();
			bodyDef.position.set(pos);
			bodyDef.position.add(x, y);
			bodyDef.angle = rotation;
			Body body = b2world.createBody(bodyDef);
			body.setType(BodyType.DynamicBody);
			carrot.body = body;
			// create rectangular shape for carrot to allow
			// interactions (collisions) with other objects
			PolygonShape polygonShape = new PolygonShape();
			float halfWidth = carrot.bounds.width / 2.0f * carrotScale;
			float halfHeight = carrot.bounds.height / 2.0f * carrotScale;
			polygonShape.setAsBox(halfWidth * carrotShapeScale, halfHeight * carrotShapeScale);
			// set physics attributes
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = polygonShape;
			fixtureDef.density = 50;
			fixtureDef.restitution = 0.5f;
			fixtureDef.friction = 0.5f;
			body.createFixture(fixtureDef);
			polygonShape.dispose();
			// finally, add new carrot to list for updating/rendering
			level.carrots.add(carrot);
		}
	}

}