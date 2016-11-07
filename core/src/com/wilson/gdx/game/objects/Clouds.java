package com.wilson.gdx.game.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.wilson.gdx.game.Assets;

public class Clouds extends AbstractGameObject
{

	/**
	 * Defines clouds for us.
	 */
	private float length;

	private Array<TextureRegion> regClouds;
	private Array<Cloud> clouds;

	private class Cloud extends AbstractGameObject
	{
		private TextureRegion regCloud;

		public Cloud()
		{
		}

		public void setRegion(TextureRegion region)
		{
			regCloud = region;
		}

		/**
		 * Like the other objects, batch.draw defines the placement of the cloud
		 * in the game.
		 */
		@Override
		public void render(SpriteBatch batch)
		{
			TextureRegion reg = regCloud;
			batch.draw(reg.getTexture(), position.x + origin.x, position.y + origin.y, origin.x, origin.y, dimension.x,
			        dimension.y, scale.x, scale.y, rotation, reg.getRegionX(), reg.getRegionY(), reg.getRegionWidth(),
			        reg.getRegionHeight(), false, false);
		}
	}

	public Clouds(float length)
	{
		this.length = length;
		init();
	}

	/**
	 * This method is to ensure there is a cloud every 5 meters of the game. To
	 * actually spawn the cloud however, it calls to the spawnCloud() method.
	 */
	private void init()
	{
		dimension.set(3.0f, 1.5f);

		regClouds = new Array<TextureRegion>();
		regClouds.add(Assets.instance.levelDecoration.cloud01);
		regClouds.add(Assets.instance.levelDecoration.cloud02);
		regClouds.add(Assets.instance.levelDecoration.cloud03);

		int distFac = 5;
		int numClouds = (int) (length / distFac);
		clouds = new Array<Cloud>(2 * numClouds);
		for (int i = 0; i < numClouds; i++)
		{
			Cloud cloud = spawnCloud();
			cloud.position.x = i * distFac;
			clouds.add(cloud);
		}
	}

	/**
	 * This adds the cloud to the scene and randomly shifts it up or down. The
	 * new cloud is then saved to the list and returned to the calling method.
	 * 
	 * @return
	 */
	private Cloud spawnCloud()
	{
		Cloud cloud = new Cloud();
		cloud.dimension.set(dimension);
		// select random cloud image
		cloud.setRegion(regClouds.random());
		// position
		Vector2 pos = new Vector2();
		pos.x = length + 10; // position after end of level
		pos.y += 1.75; // base position
		// random additional position
		pos.y += MathUtils.random(0.0f, 0.2f) * (MathUtils.randomBoolean() ? 1 : -1);
		cloud.position.set(pos);
		/**
		 * This section creates different clouds that utilize different
		 * movement. Iterates through all clouds and if the cloud is moved off
		 * screen, it is removed and a new one is added to the right of the
		 * level.
		 */
		// speed
		Vector2 speed = new Vector2();
		speed.x += 0.5f; // base speed
		// random additional speed
		speed.x += MathUtils.random(0.0f, 0.75f);
		cloud.terminalVelocity.set(speed);
		speed.x *= -1; // move left
		cloud.velocity.set(speed);
		return cloud;
	}

	@Override
	public void update(float deltaTime)
	{
		for (int i = clouds.size - 1; i >= 0; i--)
		{
			Cloud cloud = clouds.get(i);
			cloud.update(deltaTime);
			if (cloud.position.x < -10)
			{
				// cloud moved outside of world.
				// destroy and spawn new cloud at end of level.
				clouds.removeIndex(i);
				clouds.add(spawnCloud());
			}
		}
	}

	@Override
	public void render(SpriteBatch batch)
	{
		for (Cloud cloud : clouds)
			cloud.render(batch);
	}

}