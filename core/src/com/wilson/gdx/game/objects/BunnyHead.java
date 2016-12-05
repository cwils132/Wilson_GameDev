package com.wilson.gdx.game.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.wilson.gdx.game.Assets;
import com.wilson.gdx.util.Constants;
import com.wilson.gdx.util.GamePreferences;
import com.wilson.gdx.util.CharacterSkin;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.wilson.gdx.util.AudioManager;

public class BunnyHead extends AbstractGameObject
{

	public static final String TAG = BunnyHead.class.getName();
	
	public boolean grounded;
	public boolean jumping;

	public ParticleEffect dustParticles = new ParticleEffect();

	public enum VIEW_DIRECTION
	{
		LEFT, RIGHT
	}

//	public enum JUMP_STATE
//	{
//		GROUNDED, FALLING, JUMP_RISING, JUMP_FALLING
//	}

	/**
	 * Variables used to define the bunny head during runtime.
	 */
	private TextureRegion regHead;

	public VIEW_DIRECTION viewDirection;

	public float timeJumping;
//	public JUMP_STATE jumpState;

	public boolean hasFeatherPowerup;
	public float timeLeftFeatherPowerup;

	public BunnyHead()
	{
		init();
	}

	/**
	 * Initializes the BunnyHead object and sets the origin. Also sets
	 * terminalVelocity of the object, friction, and acceleration. All of which
	 * will change when we switch to Box2D.
	 * 
	 * Immediately sets jump state to falling, that the bunny will be looking to
	 * the right, and that we have no feather powerup.
	 * 
	 * Uses dust particles file to generate dust as the bunny moves around
	 * the screen on the ground.
	 */
	public void init()
	{
		dimension.set(1, 1);

		regHead = Assets.instance.character.head;

		// Center image on game object
		origin.set(dimension.x / 2, dimension.y / 2);

		// Bounding box for collision detection
		bounds.set(0, 0, dimension.x, dimension.y);

		// Set physics values
		terminalVelocity.set(5.0f, 4.0f);
		friction.set(12.0f, 0.0f);
		acceleration.set(0.0f, -25.0f);

		// View direction
		viewDirection = VIEW_DIRECTION.RIGHT;

		// Jump state
//		jumpState = JUMP_STATE.FALLING;
		grounded = true;
		timeJumping = 0;

		// Power-ups
		hasFeatherPowerup = false;
		timeLeftFeatherPowerup = 0;

		// Particles
		dustParticles.load(Gdx.files.internal("../core/assets/particles/dust.pfx"), Gdx.files.internal("../core/assets/particles"));
	}

	/**
	 * Updates the screen as you move, the feather timer counts down, etc.
	 */
	@Override
	public void update (float deltaTime) {
        super.update(deltaTime);
        updateMotionX(deltaTime);
        updateMotionY(deltaTime);
        if (body != null)
        {
            body.setLinearVelocity(velocity);
            position.set(body.getPosition());
        }
        if (velocity.x != 0)
        {
            viewDirection = velocity.x < 0 ? VIEW_DIRECTION.LEFT : VIEW_DIRECTION.RIGHT;
        }
		if (timeLeftFeatherPowerup > 0) {
			timeLeftFeatherPowerup -= deltaTime;
			if (timeLeftFeatherPowerup < 0) {
				// disable power-up
				timeLeftFeatherPowerup = 0;
				setFeatherPowerup(false);
			}
		}
		dustParticles.update(deltaTime);
	}
	

	/**
	 * Changes color of the bunny to yellow when the feather is collected.
	 * Otherwise just draws the head from the region of the atlas.
	 */
	@Override
	public void render (SpriteBatch batch) {
		TextureRegion reg = null;

		// Draw Particles
		dustParticles.draw(batch);

		// Apply Skin Color
		batch.setColor(CharacterSkin.values()[GamePreferences.instance.charSkin].getColor());

		// Set special color when game object has a feather power-up
		if (hasFeatherPowerup) {
			batch.setColor(1.0f, 0.8f, 0.0f, 1.0f);
		}

		// Draw image
		reg = regHead;
		batch.draw(reg.getTexture(), position.x, position.y, origin.x, origin.y, dimension.x, dimension.y, scale.x, scale.y,
			rotation, reg.getRegionX(), reg.getRegionY(), reg.getRegionWidth(), reg.getRegionHeight(),
			viewDirection == VIEW_DIRECTION.LEFT, false);

		// Reset color to white
		batch.setColor(1, 1, 1, 1);
	}

	/**
	 * Sets the duration of the feather powerup.
	 * 
	 * @param pickedUp
	 */
	public void setFeatherPowerup(boolean pickedUp)
	{
		hasFeatherPowerup = pickedUp;
		if (pickedUp)
		{
			timeLeftFeatherPowerup = Constants.ITEM_FEATHER_POWERUP_DURATION;
		}
	}

	/**
	 * Returns true if the feather is picked up. Otherwise hasFeatherPowerUp is
	 * false and also returns to false after the time runs out on the power up.
	 * 
	 * @return
	 */
	public boolean hasFeatherPowerup()
	{
		return hasFeatherPowerup && timeLeftFeatherPowerup > 0;
	}

	/**
	 * Controls the jumping command. jumpKeyPressed means if the spacebar is
	 * pressed, react accordingly.
	 * 
	 * On ground, jump. If in the air and space is let go, fall.
	 * 
	 * If falling and has feather, fall slower. Otherwise just fall.
	 * 
	 * @param jumpKeyPressed
	 */

}