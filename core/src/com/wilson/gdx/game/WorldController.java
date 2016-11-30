package com.wilson.gdx.game;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.wilson.gdx.util.CameraHelper;
import com.wilson.gdx.game.objects.Rock;
import com.wilson.gdx.util.Constants;
import com.badlogic.gdx.Game;
import com.wilson.gdx.screens.MenuScreen;
import com.wilson.gdx.util.AudioManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.wilson.gdx.util.CollisionHandler;

import com.badlogic.gdx.math.Rectangle;
import com.wilson.gdx.game.objects.AbstractGameObject;
import com.wilson.gdx.game.objects.BunnyHead;
//import com.wilson.gdx.game.objects.BunnyHead.JUMP_STATE;
import com.wilson.gdx.game.objects.BunnyHead.VIEW_DIRECTION;
import com.wilson.gdx.game.objects.Feather;
import com.wilson.gdx.game.objects.GoldCoin;
import com.wilson.gdx.game.objects.Rock;

public class WorldController extends InputAdapter
{

	private static final String TAG = WorldController.class.getName();

	public Level level;
	public int lives;
	public int score;
	public float livesVisual; // decreases when lives decrease
	public float scoreVisual;

	float timeHeld;

	public Array<AbstractGameObject> objectsToRemove = new Array<AbstractGameObject>();

	// Box2D Collisions
	public World myWorld;

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
		objectsToRemove = new Array<AbstractGameObject>();
		Gdx.input.setInputProcessor(this);
		cameraHelper = new CameraHelper();
		lives = Constants.LIVES_START;
		livesVisual = lives;
		timeLeftGameOverDelay = 0;
		initLevel();
	}

	/**
	 * Adds physics in to the game to be initialized at the start through
	 * initLevel();. Defines a KinematicBody for rocks to be used to if
	 * collision occurs the rocks do not let the player fall through.
	 *
	 * Defines a polygon shape for the rocks and a listener for world to monitor
	 * if collisions occur. If a collision does occur then CollisionHandler is
	 * called.
	 */
	private void initPhysics()
	{
		if (myWorld != null)
			myWorld.dispose();
		myWorld = new World(new Vector2(0, -9.81f), true);
		myWorld.setContactListener(new CollisionHandler(this));
		Vector2 origin = new Vector2();
		for (Rock pieceOfLand : level.rocks)
		{
			BodyDef bodyDef = new BodyDef();
			bodyDef.position.set(pieceOfLand.position);
			bodyDef.type = BodyType.KinematicBody;
			Body body = myWorld.createBody(bodyDef);
			body.setUserData(pieceOfLand);
			pieceOfLand.body = body;
			PolygonShape polygonShape = new PolygonShape();
			origin.x = pieceOfLand.bounds.width / 2.0f;
			origin.y = pieceOfLand.bounds.height / 2.0f;
			polygonShape.setAsBox(pieceOfLand.bounds.width / 2.0f, (pieceOfLand.bounds.height - 0.04f) / 2.0f, origin,
			        0);
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = polygonShape;
			body.createFixture(fixtureDef);
			polygonShape.dispose();
		}

		/**
		 * Defines the body for BunnyHead using a DynamicBody so that the player
		 * can be affected by gravity. The box shape used to define the body is
		 * a polygon That is should be in the shape of a square to stretch the
		 * bunny head graphic in to proper proportions.
		 */
		// For PLayer
		BunnyHead player = level.bunnyHead;
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(player.position);
		bodyDef.fixedRotation = true;

		Body body = myWorld.createBody(bodyDef);
		body.setType(BodyType.DynamicBody);
		body.setGravityScale(9.8f);
		body.setUserData(player);
		player.body = body;

		PolygonShape polygonShape = new PolygonShape();
		origin.x = (player.bounds.width) / 2.0f;
		origin.y = (player.bounds.height) / 2.0f;
		polygonShape.setAsBox((player.bounds.width - 0.7f) / 2.0f, (player.bounds.height - 0.15f) / 2.0f, origin, 0);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = polygonShape;
		body.createFixture(fixtureDef);
		polygonShape.dispose();
	}

	/**
	 * Initializes all level methods and physics engine for Box2D.
	 */
	private void initLevel()
	{
		score = 0;
		scoreVisual = score;
		level = new Level(Constants.LEVEL_01);
		cameraHelper.setTarget(level.bunnyHead);
		initPhysics();
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
				init();
		} else
		{
			handleInputGame(deltaTime);
		}

		if (MathUtils.random(0.0f, 2.0f) < deltaTime)
		{
			Vector2 centerPos = new Vector2(level.bunnyHead.position);
			centerPos.x += level.bunnyHead.bounds.width;
		}

		myWorld.step(deltaTime, 8, 3); // tells box2d world to update

		level.update(deltaTime);
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

	public boolean isGameOver()
	{
		return lives < 0;
	}

	public boolean isPlayerInWater()
	{
		return level.bunnyHead.body.getPosition().y < -5;
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
					level.bunnyHead.body.getLinearVelocity().x = level.bunnyHead.terminalVelocity.x;
				}
			}

			// Bunny Jump
			if (Gdx.input.isTouched() || Gdx.input.isKeyPressed(Keys.SPACE))
			{
				Vector2 vec = level.bunnyHead.body.getLinearVelocity();
				Gdx.app.error(TAG, "Process Space: "+timeHeld);
				if (timeHeld < 0.5)
				{
					Gdx.app.error(TAG, "Process Jump: "+timeHeld);
					level.bunnyHead.grounded = false;
					level.bunnyHead.jumping = true;
//					level.bunnyHead.body.applyLinearImpulse(0.0f, 10.0f, level.bunnyHead.body.getPosition().x, level.bunnyHead.body.getPosition().y, true);
					level.bunnyHead.body.setLinearVelocity(vec.x, level.bunnyHead.terminalVelocity.y);
					level.bunnyHead.position.set(level.bunnyHead.body.getPosition());
					timeHeld += deltaTime;
				}
			}
		}
	}

	public void resetJump()
	{
		timeHeld = 0.0f;
		Gdx.app.error(TAG, "Reset Time: "+timeHeld);
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
	 * Allows us to switch back to menu whenever the player loses or hits
	 * escape.
	 */
	private void backToMenu()
	{
		// switch to menu screen
		game.setScreen(new MenuScreen(game));
	}

	public void flagForRemoval(AbstractGameObject obj)
	{
		objectsToRemove.add(obj);
	}
}