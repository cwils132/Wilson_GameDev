package com.wilson.gdx.game.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.wilson.gdx.game.Assets;

public class WaterOverlay extends AbstractGameObject
{

	/**
	 * Water sits in front of all other rendered images and does
	 * not move with the camera. Tiles across infinitely
	 * throughout the level.
	 */
	private TextureRegion regWaterOverlay;
	private float length;

	public WaterOverlay(float length)
	{
		this.length = length;
		init();
	}

	private void init()
	{
		dimension.set(length * 10, 3);

		regWaterOverlay = Assets.instance.levelDecoration.dustOverlay;

		origin.x = -dimension.x / 2;
	}

	@Override
	public void render(SpriteBatch batch)
	{
		TextureRegion reg = null;
		reg = regWaterOverlay;
		batch.draw(reg.getTexture(), position.x + origin.x, position.y + origin.y, origin.x, origin.y, dimension.x,
		        dimension.y, scale.x, scale.y, rotation, reg.getRegionX(), reg.getRegionY(), reg.getRegionWidth(),
		        reg.getRegionHeight(), false, false);
	}

}