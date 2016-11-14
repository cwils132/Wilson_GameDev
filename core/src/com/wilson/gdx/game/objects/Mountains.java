package com.wilson.gdx.game.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.wilson.gdx.game.Assets;
import com.badlogic.gdx.math.Vector2;

public class Mountains extends AbstractGameObject
{

	/**
	 * This class works similarly to rock. Mountains have two parts with
	 * seamless blending to allow for an infinite loop in our background.
	 */
	private TextureRegion regMountainLeft;
	private TextureRegion regMountainRight;

	private int length;

	public Mountains(int length)
	{
		this.length = length;
		init();
	}
	
	public void updateScrollPosition (Vector2 camPosition) {
		position.set(camPosition.x, position.y);
	}

	private void init()
	{
		dimension.set(10, 2);

		regMountainLeft = Assets.instance.levelDecoration.mountainLeft;
		regMountainRight = Assets.instance.levelDecoration.mountainRight;

		// shift mountain and extend length
		origin.x = -dimension.x * 2;
		length += dimension.x * 2;
	}
	
	/**
	 * This creates the two sides of the mountain background graphics
	 * and draws them across the entire level.
	 * 
	 * Also includes the parallax effect. the float parallazSpeedx variable
	 * ranges between 0 and 1 and describes the distance and its scrolling
	 * speed. The scrolling depends on camera position, which is then
	 * multiplied with the distance factor. New method to accomodate
	 * this is added called updateScrollPosition()
	 * @param batch
	 * @param offsetX
	 * @param offsetY
	 * @param tintColor
	 * @param parallaxSpeedX
	 */

	private void drawMountain (SpriteBatch batch, float offsetX, float offsetY, float tintColor, float parallaxSpeedX) {
		TextureRegion reg = null;
		batch.setColor(tintColor, tintColor, tintColor, 1);
		float xRel = dimension.x * offsetX;
		float yRel = dimension.y * offsetY;

		// mountains span the whole level
		int mountainLength = 0;
		mountainLength += MathUtils.ceil(length / (2 * dimension.x) * (1 - parallaxSpeedX));
		mountainLength += MathUtils.ceil(0.5f + offsetX);
		for (int i = 0; i < mountainLength; i++) {
			// mountain left
			reg = regMountainLeft;
			batch.draw(reg.getTexture(), origin.x + xRel + position.x * parallaxSpeedX, origin.y + yRel + position.y, origin.x,
				origin.y, dimension.x, dimension.y, scale.x, scale.y, rotation, reg.getRegionX(), reg.getRegionY(),
				reg.getRegionWidth(), reg.getRegionHeight(), false, false);
			xRel += dimension.x;
			// mountain right
			reg = regMountainRight;
			batch.draw(reg.getTexture(), origin.x + xRel + position.x * parallaxSpeedX, origin.y + yRel + position.y, origin.x,
				origin.y, dimension.x, dimension.y, scale.x, scale.y, rotation, reg.getRegionX(), reg.getRegionY(),
				reg.getRegionWidth(), reg.getRegionHeight(), false, false);
			xRel += dimension.x;
		}
		// reset color to white
		batch.setColor(1, 1, 1, 1);
	}

	/**
	 * All three rendered mountains now move at different speeds.
	 */
	@Override
	public void render(SpriteBatch batch)
	{
		// distant mountains (dark gray)
		drawMountain(batch, 0.5f, 0.5f, 0.5f, 0.8f);
		// distant mountains (gray)
		drawMountain(batch, 0.25f, 0.25f, 0.7f, 0.5f);
		// distant mountains (light gray)
		drawMountain(batch, 0.0f, 0.0f, 0.9f, 0.3f);
	}

}