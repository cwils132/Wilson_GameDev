package com.wilson.gdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Disposable;
import com.wilson.gdx.util.Constants;

public class Assets implements Disposable, AssetErrorListener
{

	public static final String TAG = Assets.class.getName();

	public static final Assets instance = new Assets();

	private AssetManager assetManager;

	public AssetSword sword;
	public AssetCloud1 cloud1;
	
	public AssetLevelDecoration levelDecoration;

	// singleton: prevent instantiation from other classes
	private Assets()
	{
	}

	/**
	 * Specifies regions of the atlas being created. This way
	 * the program knows where to look to pull the information
	 * and render the proper image to the screen.
	 * @author Chris
	 *
	 */
	public class AssetSword
	{
		public final AtlasRegion sword;

		public AssetSword(TextureAtlas atlas)
		{
			sword = atlas.findRegion("sword");
		}
	}


	public class AssetCloud1
	{
		public final AtlasRegion cloud1;

		public AssetCloud1(TextureAtlas atlas)
		{
			cloud1 = atlas.findRegion("cloud1");
		}
	}

	/**
	 * Used for building the level. Pulls images from
	 * atlas and applies them.
	 * @author Chris
	 *
	 */
	public class AssetLevelDecoration
	{
		public final AtlasRegion sword;
		public final AtlasRegion cloud1;
		
		public AssetLevelDecoration(TextureAtlas atlas)
		{
			sword = atlas.findRegion("sword");
			cloud1 = atlas.findRegion("cloud1");
		}
	}

	public void init(AssetManager assetManager)
	{
		this.assetManager = assetManager;
		// set asset manager error handler
		assetManager.setErrorListener(this);
		// load texture atlas
		assetManager.load(Constants.TEXTURE_ATLAS_OBJECTS, TextureAtlas.class);
		// start loading assets and wait until finished
		assetManager.finishLoading();

		Gdx.app.debug(TAG, "# of assets loaded: " + assetManager.getAssetNames().size);
		for (String a : assetManager.getAssetNames())
		{
			Gdx.app.debug(TAG, "asset: " + a);
		}

		TextureAtlas atlas = assetManager.get(Constants.TEXTURE_ATLAS_OBJECTS);

		// enable texture filtering for pixel smoothing
		for (Texture t : atlas.getTextures())
		{
			t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		}

		// create game resource objects
		sword = new AssetSword(atlas);
		cloud1 = new AssetCloud1(atlas);
		levelDecoration = new AssetLevelDecoration(atlas);
	}

	@Override
	public void dispose()
	{
		assetManager.dispose();
	}

	@Override
	public void error(AssetDescriptor asset, Throwable throwable)
	{
		Gdx.app.error(TAG, "Couldn't load asset '" + asset.fileName + "'", (Exception) throwable);

	}

}