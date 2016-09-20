package com.wilson.gdx.util;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class CameraHelper
{

	private static final String TAG = CameraHelper.class.getName();

	private final float MAX_ZOOM_IN = 0.25f;
	private final float MAX_ZOOM_OUT = 10.0f;

	private Vector2 position;
	private float zoom;
	private Sprite target;

	public CameraHelper()
	{
		position = new Vector2();
		zoom = 1.0f;
	}

	/**
	 * The game has to redraw several hundred times a second. We use update to
	 * make sure it keeps track of the time properly and updates where things
	 * are on the screen accordingly.
	 * 
	 * @param deltaTime
	 */
	public void update(float deltaTime)
	{
		if (!hasTarget())
			return;

		position.x = target.getX() + target.getOriginX();
		position.y = target.getY() + target.getOriginY();
	}

	public void setPosition(float x, float y)
	{
		this.position.set(x, y);
	}

	public Vector2 getPosition()
	{
		return position;
	}

	public void addZoom(float amount)
	{
		setZoom(zoom + amount);
	}

	public void setZoom(float zoom)
	{
		this.zoom = MathUtils.clamp(zoom, MAX_ZOOM_IN, MAX_ZOOM_OUT);
	}

	public float getZoom()
	{
		return zoom;
	}

	public void setTarget(Sprite target)
	{
		this.target = target;
	}

	public Sprite getTarget()
	{
		return target;
	}

	public boolean hasTarget()
	{
		return target != null;
	}

	/**
	 * If we have a game object targeted, we know to follow it
	 * with the camera.
	 * @param target
	 * @return
	 */
	public boolean hasTarget(Sprite target)
	{
		return hasTarget() && this.target.equals(target);
	}

	/**
	 * Aids in camera movement to follow the target.
	 * @param camera
	 */
	public void applyTo(OrthographicCamera camera)
	{
		camera.position.x = position.x;
		camera.position.y = position.y;
		camera.zoom = zoom;
		camera.update();
	}

}