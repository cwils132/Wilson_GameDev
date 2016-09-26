package com.wilson.gdx.game.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.wilson.gdx.game.Assets;

/**
 * The Rock Object is divided up in to three parts using two different images
 * in our atlas. We have a middle and an edge piece.
 * 
 * The middle piece is what is tiled throughout the game, and we have an
 * edge that is placed to cap off the sides so as not to make the game
 * look like blocks.
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

	private int length;

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
	}

	/**
	 * Sets the starting length of the rock.
	 * @param length
	 */
	public void setLength(int length)
	{
		this.length = length;
	}

	/**
	 * Lets us increase the length of a rock by a given amount.
	 * This is used by our level loader.
	 * @param amount
	 */
	public void increaseLength(int amount)
	{
		setLength(length + amount);
	}

	/**
	 * We inherit from AbstractGame Object so we need to implement
	 * a render method and override it.
	 */
	@Override
	public void render(SpriteBatch batch)
	{
		TextureRegion reg = null;

		float relX = 0;
		float relY = 0;

		/**
		 * This method cuts out a rectangle from our atlas and draws it
		 * to the position given with originX and originY. The width and height
		 * define the dimension of the object itself.
		 * 
		 * The final two booleans are flip booleans. These will flip the image
		 * of the rocks edge so that we can use one image to accomplish
		 * two different requirements.
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

}