package com.wilson.gdx.game.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public abstract class AbstractGameObject
{
	/**
	 * We are using this abstract class to store basic information about our game
	 * objects without any direct alteration to their functions.
	 * 
	 * Here we store info about position, dimension, origin, scale factor, and angle
	 * of rotation.
	 * 
	 * @author Chris
	 *
	 */
	public Vector2 position;
	public Vector2 dimension;
	public Vector2 origin;
	public Vector2 scale;
	public float rotation;
	public Vector2 velocity;
	public Vector2 terminalVelocity;
	public Vector2 friction;
	public Vector2 acceleration;
	public Rectangle bounds;
	
	/**
	 * Body allows objects to use Box2D physics
	 */
	public Body body;
	
	public float stateTime;
	public Animation animation;
	/**
	 * Sets variables to be used with movement and physics.
	 */
	public AbstractGameObject()
	{
		position = new Vector2();
		dimension = new Vector2(1, 1);
		origin = new Vector2();
		scale = new Vector2(1, 1);
		rotation = 0;
		velocity = new Vector2();
		terminalVelocity = new Vector2(1, 1);
		friction = new Vector2();
		acceleration = new Vector2();
		bounds = new Rectangle();
	}
	/**
	 * These classes exist so that World Controller can call on it (as it really
	 * wants to do) but will not actually alter our objects. Because each object
	 * will react to situations differently, we will define these behaviors in
	 * the objects themselves.
	 * 
	 * @param deltaTime
	 */
	public void update(float deltaTime)
	{
		stateTime += deltaTime;
		if (body == null)
		{
			updateMotionX(deltaTime);
			updateMotionY(deltaTime);

			// Move to new position
			position.x += velocity.x * deltaTime;
			position.y += velocity.y * deltaTime;
		} else
		{
			position.set(body.getPosition());
			rotation = body.getAngle() * MathUtils.radiansToDegrees;
		}
	}

	protected void updateMotionX(float deltaTime)
	{
		if (velocity.x != 0)
		{
			// Apply friction
			if (velocity.x > 0)
			{
				velocity.x = Math.max(velocity.x - friction.x * deltaTime, 0);
			} else
			{
				velocity.x = Math.min(velocity.x + friction.x * deltaTime, 0);
			}
		}
		// Apply acceleration
		velocity.x += acceleration.x * deltaTime;
		// Make sure the object's velocity does not exceed the
		// positive or negative terminal velocity
		velocity.x = MathUtils.clamp(velocity.x, -terminalVelocity.x, terminalVelocity.x);
	}

	protected void updateMotionY(float deltaTime)
	{
		if (velocity.y != 0)
		{
			// Apply friction
			if (velocity.y > 0)
			{
				velocity.y = Math.max(velocity.y - friction.y * deltaTime, 0);
			} else
			{
				velocity.y = Math.min(velocity.y + friction.y * deltaTime, 0);
			}
		}
		// Apply acceleration
		velocity.y += acceleration.y * deltaTime;
		// Make sure the object's velocity does not exceed the
		// positive or negative terminal velocity
		velocity.y = MathUtils.clamp(velocity.y, -terminalVelocity.y, terminalVelocity.y);
	}

	public void setAnimation(Animation animation)
	{
		this.animation = animation;
		stateTime = 0;
	}

	public abstract void render(SpriteBatch batch);

}