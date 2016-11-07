package com.wilson.gdx.game.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.wilson.gdx.game.Assets;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * The Rock Object is divided up in to three parts using two different images in
 * our atlas. We have a middle and an edge piece.
 * 
 * The middle piece is what is tiled throughout the game, and we have an edge
 * that is placed to cap off the sides so as not to make the game look like
 * blocks.
 * 
 * @author Chris
 *
 */
public class Rock extends AbstractGameObject
{

	/**
	 * These variables store the two parts of each rendered rock.
	 */
	private TextureRegion regEdge;
	private TextureRegion regMiddle;

	// Start length of this rock
	private int length;

	/**
	 * Makes floating mechanism of rocks correctly initialized. Cycle time is
	 * randomly picked between 0 and half a maximum float cycle time. This
	 * allows rocks to be in different positions when they initialize.
	 */
	private final float FLOAT_CYCLE_TIME = 2.0f;
	private final float FLOAT_AMPLITUDE = 0.25f;
	private float floatCycleTimeLeft;
	private boolean floatingDownwards;
	private Vector2 floatTargetPosition;

	public Rock()
	{
		init();
	}

	private void init()
	{
		dimension.set(1, 1.5f);

		regEdge = Assets.instance.rock.edge;
		regMiddle = Assets.instance.rock.middle;

		// Start length of this rock
		setLength(1);

		floatingDownwards = false;
		floatCycleTimeLeft = MathUtils.random(0, FLOAT_CYCLE_TIME / 2);
		floatTargetPosition = null;
	}

	/**
	 * Sets the starting length of the rock.
	 * 
	 * @param length
	 */
	public void setLength(int length)
	{
		this.length = length;
		// Update bounding box for collision detection
		bounds.set(0, 0, dimension.x * length, dimension.y);
	}

	/**
	 * Lets us increase the length of a rock by a given amount. This is used by
	 * our level loader.
	 * 
	 * @param amount
	 */
	public void increaseLength(int amount)
	{
		setLength(length + amount);
	}

	/**
	 * We inherit from AbstractGame Object so we need to implement a render
	 * method and override it.
	 */
	@Override
	public void render(SpriteBatch batch)
	{
		TextureRegion reg = null;

		float relX = 0;
		float relY = 0;

		/**
		 * This method cuts out a rectangle from our atlas and draws it to the
		 * position given with originX and originY. The width and height define
		 * the dimension of the object itself.
		 * 
		 * The final two booleans are flip booleans. These will flip the image
		 * of the rocks edge so that we can use one image to accomplish two
		 * different requirements.
		 * 
		 * The first boolean would flip vertically, so we keep that false. We
		 * turn on flip Y to flip it horizontally.
		 */
		// Draw left edge
		reg = regEdge;
		relX -= dimension.x / 4;
		batch.draw(reg.getTexture(), position.x + relX, position.y + relY, origin.x, origin.y, dimension.x / 4,
		        dimension.y, scale.x, scale.y, rotation, reg.getRegionX(), reg.getRegionY(), reg.getRegionWidth(),
		        reg.getRegionHeight(), false, false);

		// Draw middle
		relX = 0;
		reg = regMiddle;
		for (int i = 0; i < length; i++)
		{
			batch.draw(reg.getTexture(), position.x + relX, position.y + relY, origin.x, origin.y, dimension.x,
			        dimension.y, scale.x, scale.y, rotation, reg.getRegionX(), reg.getRegionY(), reg.getRegionWidth(),
			        reg.getRegionHeight(), false, false);
			relX += dimension.x;
		}

		// Draw right edge
		reg = regEdge;
		batch.draw(reg.getTexture(), position.x + relX, position.y + relY, origin.x + dimension.x / 8, origin.y,
		        dimension.x / 4, dimension.y, scale.x, scale.y, rotation, reg.getRegionX(), reg.getRegionY(),
		        reg.getRegionWidth(), reg.getRegionHeight(), true, false);
	}

	@Override
	public void update(float deltaTime)
	{
		super.update(deltaTime);

		floatCycleTimeLeft -= deltaTime;
		if (floatCycleTimeLeft <= 0)
		{
			floatCycleTimeLeft = FLOAT_CYCLE_TIME;
			floatingDownwards = !floatingDownwards;
			body.setLinearVelocity(0, FLOAT_AMPLITUDE * (floatingDownwards ? -1 : 1));
		} else
		{
			body.setLinearVelocity(body.getLinearVelocity().scl(0.98f));

		}
	}

}