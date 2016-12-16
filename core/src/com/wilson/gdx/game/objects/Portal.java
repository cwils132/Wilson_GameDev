package com.wilson.gdx.game.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.wilson.gdx.game.Assets;

public class Portal extends AbstractGameObject {

	private TextureRegion regGoal;

	public Portal () {
		init();
	}

	private void init()
	{
		dimension.set(3.0f, 3.0f);
		setAnimation(Assets.instance.levelDecoration.animOrangePortal);
		stateTime = MathUtils.random(0.0f, 1.0f);

		// Set bounding box for collision detection
		bounds.set(1, Float.MIN_VALUE, 10, Float.MAX_VALUE);
		origin.set(dimension.x / 2.0f, 0.0f);
	}

	public void render(SpriteBatch batch)
	{
		TextureRegion reg = null;

		reg = animation.getKeyFrame(stateTime, true);
		batch.draw(reg.getTexture(), position.x - origin.x, position.y - origin.y, origin.x, origin.y, dimension.x,
		        dimension.y, scale.x, scale.y, rotation, reg.getRegionX(), reg.getRegionY(), reg.getRegionWidth(),
		        reg.getRegionHeight(), false, false);
	}

}