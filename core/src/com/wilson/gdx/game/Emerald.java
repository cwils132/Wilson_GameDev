package com.wilson.gdx.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.wilson.gdx.game.objects.AbstractGameObject;

public class Emerald extends AbstractGameObject
{

	private TextureRegion regEmerald;

	public boolean collected;

	public Emerald()
	{
		init();
	}

	private void init()
	{
		dimension.set(0.5f, 0.5f);

		regEmerald = Assets.instance.emerald.emerald;

		// Set bounding box for collision detection
		bounds.set(0, 0, dimension.x, dimension.y);

		collected = false;
	}

	public void render(SpriteBatch batch)
	{
		if (collected)
			return;

		TextureRegion reg = null;

		reg = regEmerald;
		batch.draw(reg.getTexture(), position.x, position.y, origin.x, origin.y, dimension.x, dimension.y, scale.x,
		        scale.y, rotation, reg.getRegionX(), reg.getRegionY(), reg.getRegionWidth(), reg.getRegionHeight(),
		        false, false);
	}

	public int getScore()
	{
		return 250;
	}

}