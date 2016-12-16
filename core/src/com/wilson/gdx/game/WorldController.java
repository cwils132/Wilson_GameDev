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
import com.wilson.gdx.game.objects.RoughRock;
import com.wilson.gdx.util.Constants;
import com.wilson.gdx.util.GamePreferences;
import com.badlogic.gdx.Game;
import com.wilson.gdx.screens.GameScreen;
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
import com.wilson.gdx.game.objects.BunnyHead.VIEW_DIRECTION;
import com.wilson.gdx.game.objects.Feather;
import com.wilson.gdx.game.objects.GoldCoin;
import com.wilson.gdx.game.objects.Portal;
import com.wilson.gdx.game.objects.Rock;

public class WorldController extends InputAdapter
{

	private static final String TAG = WorldController.class.getName();

	public Level level;
	public int lives;
	public int score;
	public float livesVisual; // decreases when lives decrease
	public float scoreVisual;

	/**
	 * Helps keep track of portal collision.
	 */
	private Rectangle r1 = new Rectangle();
	private Rectangle r2 = new Rectangle();

	float timeHeld;
	private boolean portalReached;
	private String currentLevel;
	GamePreferences preferences = GamePreferences.instance;

	public Array<AbstractGameObject> objectsToRemove = new Array<AbstractGameObject>();

	// Box2D Collisions
	public World myWorld;

	public CameraHelper cameraHelper;

	private float timeLeftGameOverDelay;

	private Game game;

	public WorldController(Game game)
	{
		this.game = game;
		init();
	}

	/**
	 * Generates initial level map.
	 */
	private void init()
	{
		objectsToRemove = new Array<AbstractGameObject>();
		Gdx.input.setInputProcessor(this);
		cameraHelper = new CameraHelper();
		currentLevel = Constants.LEVEL_01;
		lives = Constants.LIVES_START;
		livesVisual = lives;
		timeLeftGameOverDelay = 0;
		initLevel();
	}

	/**
	 * Sets the stage for the second level. A boolean in update
	 * checks if the portal has been reached on the first or second
	 * level. If the first is reached, these two methods are then called
	 * and the new map generated. Scores and lives are preserved.
	 * @param tranLives
	 * @param tranScore
	 */
	private void initSecond(int tranLives, int tranScore)
	{
		objectsToRemove = new Array<AbstractGameObject>();
		Gdx.input.setInputProcessor(this);
		cameraHelper = new CameraHelper();
		currentLevel = Constants.LEVEL_02;
		lives = tranLives;
		livesVisual = lives;
		timeLeftGameOverDelay = 0;
		initLevelTwo(tranScore);
	}

	private void initLevelTwo(int tranScore)
	{
		score = 0;
		scoreVisual = tranScore;
		portalReached = false;
		level = new Level(Constants.LEVEL_02);
		cameraHelper.setTarget(level.bunnyHead);
		initPhysics();
	}

	/**
	 * Adds physics in to the game to be initialized at the start through
	 * initLevel();. Defines a KinematicBody for rocks to be used to if
	 * collision occurs the rocks do not let the player fall through.
	 *
	 * Defines a polygon shape for all items in the game world. Then sets
	 * physics definitions such as density and friction. Collectible items
	 * are set as a sensor so that they do not interfere with movement
	 * on collision.
	 */
	private void initPhysics()
	{
		if (myWorld != null)
		{
			myWorld.dispose();
		}
		myWorld = new World(new Vector2(0, -29.81f), true);
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

			fixtureDef.friction = 0.5f;
			polygonShape.dispose();
		}

		for (RoughRock pieceOfLand : level.roughRocks)
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
			fixtureDef.friction = 0.0f;
			polygonShape.dispose();
		}

		for (GoldCoin collectableBook : level.goldcoins)
		{
			BodyDef bodyDef = new BodyDef();
			bodyDef.position.set(collectableBook.position);
			bodyDef.type = BodyType.KinematicBody;
			Body body = myWorld.createBody(bodyDef);
			body.setUserData(collectableBook);
			collectableBook.body = body;
			PolygonShape polygonShape = new PolygonShape();
			origin.x = collectableBook.bounds.width / 2.0f;
			origin.y = collectableBook.bounds.height / 2.0f;
			polygonShape.setAsBox(collectableBook.bounds.width / 2.0f, (collectableBook.bounds.height - 0.04f) / 2.0f,
			        origin, 0);
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = polygonShape;
			fixtureDef.isSensor = true;
			body.createFixture(fixtureDef);
			polygonShape.dispose();
		}

		for (Feather collectableFeather : level.feathers)
		{
			BodyDef bodyDef = new BodyDef();
			bodyDef.position.set(collectableFeather.position);
			bodyDef.type = BodyType.KinematicBody;
			Body body = myWorld.createBody(bodyDef);
			body.setUserData(collectableFeather);
			collectableFeather.body = body;
			PolygonShape polygonShape = new PolygonShape();
			origin.x = collectableFeather.bounds.width / 2.0f;
			origin.y = collectableFeather.bounds.height / 2.0f;
			polygonShape.setAsBox(collectableFeather.bounds.width / 2.0f,
			        (collectableFeather.bounds.height - 0.04f) / 2.0f, origin, 0);
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = polygonShape;
			fixtureDef.isSensor = true;
			body.createFixture(fixtureDef);
			polygonShape.dispose();
		}

		for (Emerald collectableFeather : level.emeralds)
		{
			BodyDef bodyDef = new BodyDef();
			bodyDef.position.set(collectableFeather.position);
			bodyDef.type = BodyType.KinematicBody;
			Body body = myWorld.createBody(bodyDef);
			body.setUserData(collectableFeather);
			collectableFeather.body = body;
			PolygonShape polygonShape = new PolygonShape();
			origin.x = collectableFeather.bounds.width / 2.0f;
			origin.y = collectableFeather.bounds.height / 2.0f;
			polygonShape.setAsBox(collectableFeather.bounds.width / 2.0f,
			        (collectableFeather.bounds.height - 0.04f) / 2.0f, origin, 0);
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = polygonShape;
			fixtureDef.isSensor = true;
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
		polygonShape.setAsBox((player.bounds.width - 0.2f) / 2.0f, (player.bounds.height - 0.15f) / 2.0f, origin, 0);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = polygonShape;
		fixtureDef.density = 3.0f;
		fixtureDef.friction = player.friction.x;
		body.createFixture(fixtureDef);
		polygonShape.dispose();
	}

	private void testCollisions()
	{
		r1.set(level.bunnyHead.position.x, level.bunnyHead.position.y, level.bunnyHead.bounds.width,
		        level.bunnyHead.bounds.height);
		if (!portalReached)
		{
			r2.set(level.orangePortal.bounds);
			r2.x += level.orangePortal.position.x;
			r2.y += level.orangePortal.position.y;
			if (r1.overlaps(r2))
				onCollisionWithPortal();
		}
	}

	private void onCollisionWithPortal()
	{
		portalReached = true;
		Vector2 centerPosBunnyHead = new Vector2(level.bunnyHead.position);
		centerPosBunnyHead.x += level.bunnyHead.bounds.width;
	}

	/**
	 * Initializes all level methods and physics engine for Box2D.
	 */
	private void initLevel()
	{
		score = 0;
		scoreVisual = score;
		portalReached = false;
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
		testCollisions();
		level.update(deltaTime);
		cameraHelper.update(deltaTime);
		if (!isGameOver() && isPlayerInWater())
		{
			AudioManager.instance.play(Assets.instance.sounds.liveLost);
			lives--;
			if (currentLevel.equalsIgnoreCase(Constants.LEVEL_02))
			{
				initLevelTwo(score);
			}
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
		if (portalReached && currentLevel.equals(Constants.LEVEL_01))
		{
			setLevel(Constants.LEVEL_02);
		}
		/**
		 * Changes behavior depending if portal on first or second level is
		 * reached. If second portal is reached, the game stores your current
		 * score to a list in Gameprefs and is then printed out in console.
		 */
		if (portalReached && currentLevel.equals(Constants.LEVEL_02))
		{
			for (int i = 0; i < preferences.scores.length; i++)
			{
				if (score > preferences.scores[i])
				{
					preferences.prefs.putInteger("score ", preferences.scores[i]);
					i = preferences.scores.length;
				}
			}
			int x = 1;
			for (int i = preferences.scores.length; i > preferences.scores.length; i--)
			{
				System.out.println(x + " " + preferences.scores[i]);
				x++;
			}
			init();
		}
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
				if (level.bunnyHead.hasBookmark)
				{
					level.bunnyHead.terminalVelocity.set(10.0f, 4.0f);
					level.bunnyHead.velocity.x = -level.bunnyHead.terminalVelocity.x;
				} else
				{
					level.bunnyHead.terminalVelocity.set(5.0f, 4.0f);
					level.bunnyHead.velocity.x = -level.bunnyHead.terminalVelocity.x;
				}

			} else if (Gdx.input.isKeyPressed(Keys.RIGHT))
			{
				if (level.bunnyHead.hasBookmark)
				{
					level.bunnyHead.terminalVelocity.set(10.0f, 4.0f);
					level.bunnyHead.velocity.x = level.bunnyHead.terminalVelocity.x;
				} else
				{
					level.bunnyHead.terminalVelocity.set(5.0f, 4.0f);
					level.bunnyHead.velocity.x = level.bunnyHead.terminalVelocity.x;
				}
			} else
			{
				// Execute auto-forward movement on non-desktop platform
				if (Gdx.app.getType() != ApplicationType.Desktop)
				{
					level.bunnyHead.body.getLinearVelocity().x = level.bunnyHead.terminalVelocity.x;
				}
			}

			// Bunny Jump
			if ((Gdx.input.isTouched() || Gdx.input.isKeyPressed(Keys.SPACE)) && level.bunnyHead.jumping == false)
			{
				level.bunnyHead.dustParticles.allowCompletion();
				level.bunnyHead.sparkParticles.allowCompletion();
				/**
				 * Determines how long the jump button can be held before the
				 * character falls.
				 */
				if (timeHeld < 0.35)
				{
					if (level.bunnyHead.grounded)
					{
						AudioManager.instance.play(Assets.instance.sounds.jump);
					}
					/**
					 * Sets boolean grounded to false since he is now jumping
					 */
					level.bunnyHead.grounded = false;
					/**
					 * Checks to see if the player has an emerald. If true, the
					 * force upwards is greater. If not, no change.
					 */
					if (level.bunnyHead.hasEmerald)
					{
						level.bunnyHead.body.applyLinearImpulse(0.0f, 30.0f, level.bunnyHead.body.getPosition().x,
						        level.bunnyHead.body.getPosition().y, true);
					} else
					{
						level.bunnyHead.body.applyLinearImpulse(0.0f, 20.0f, level.bunnyHead.body.getPosition().x,
						        level.bunnyHead.body.getPosition().y, true);
					}
					level.bunnyHead.position.set(level.bunnyHead.body.getPosition());
					timeHeld += deltaTime;
				}
			}
		}
	}

	public void resetJump()
	{
		timeHeld = 0.0f;
		Gdx.app.error(TAG, "Reset Time: " + timeHeld);
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
		} else if (keycode == Keys.SPACE)
		{
			level.bunnyHead.jumping = true;
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

	private void setLevel(String level)
	{
		// Advance to next level
		initSecond(lives, score);
	}

	public void flagForRemoval(AbstractGameObject obj)
	{
		objectsToRemove.add(obj);
	}
}