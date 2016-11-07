package com.wilson.gdx.game.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.wilson.gdx.game.Assets;

public class GoldCoin extends AbstractGameObject
{

	private TextureRegion regGoldCoin;

	/**
	 * Determines if the coin is picked up or not.
	 */
	public boolean collected;

	public GoldCoin()
	{
		init();
	}

	private void init()
	{
		dimension.set(0.5f, 0.5f);

		regGoldCoin = Assets.instance.goldCoin.goldCoin;

		// Set bounding box for collision detection
		bounds.set(0, 0, dimension.x, dimension.y);

		collected = false;
	}

	/**
	 * Creates a coin image using TextureAtlas dimensions.
	 */
	public void render(SpriteBatch batch)
	{
		if (collected)
			return;

		TextureRegion reg = null;

		reg = regGoldCoin;
		batch.draw(reg.getTexture(), position.x, position.y, origin.x, origin.y, dimension.x, dimension.y, scale.x,
		        scale.y, rotation, reg.getRegionX(), reg.getRegionY(), reg.getRegionWidth(), reg.getRegionHeight(),
		        false, false);
	}

	/**
	 * Increases the score for each coin collected.
	 * 
	 * @return
	 */
	public int getScore()
	{
		return 100;
	}

}