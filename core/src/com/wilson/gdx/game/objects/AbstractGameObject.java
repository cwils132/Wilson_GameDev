package com.wilson.gdx.game.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * We are using this abstract class to store basic information
 * about our game objects without any direct alteration
 * to their functions.
 * 
 * Here we store info about position, dimension, origin, scale factor,
 * and angle of rotation.
 * @author Chris
 *
 */
public abstract class AbstractGameObject
{
	public Vector2 position;
	public Vector2 dimension;
	public Vector2 origin;
	public Vector2 scale;
	public float rotation;

	public AbstractGameObject()
	{
		position = new Vector2();
		dimension = new Vector2(1, 1);
		origin = new Vector2();
		scale = new Vector2(1, 1);
		rotation = 0;
	}

	/**
	 * These classes exist so that World Controller can call on it
	 * (as it really wants to do) but will not actually alter
	 * our objects. Because each object will react
	 * to situations differently, we will define these
	 * behaviors in the objects themselves.
	 * @param deltaTime
	 */
	public void update(float deltaTime)
	{
	}

	public abstract void render(SpriteBatch batch);

}