package com.wilson.gdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import com.wilson.gdx.WilsonGdxGame;

public class DesktopLauncher
{
	private static boolean rebuildAtlas = false;
	private static boolean drawDebugOutline = false;

	/**
	 * Class is mostly not touched. We only need it to run the Desktop version
	 * of the game.
	 * 
	 * We also use it to specify the TexturePacker. This creates the atlas that
	 * we use the render images to the screen. Using the atlas, we load only one
	 * larger image rather than multiple smaller images.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		if (rebuildAtlas)
		{
			Settings settings = new Settings();
			settings.maxWidth = 1024;
			settings.maxHeight = 1024;
			settings.duplicatePadding = false;
			settings.debug = drawDebugOutline;
			/**
			 * process() takes three inputs: Folder containing the images,
			 * destination folder, and description file they must be written in
			 * that order.
			 */
			TexturePacker.process(settings, "assets-raw/images", "../core/assets/images", "theLibrary.pack");
			TexturePacker.process(settings, "assets-raw/images-ui", "../core/assets/images", "canyonbunny-ui.pack");
		}

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "The Library";
		config.width = 800;
		config.height = 480;

		new LwjglApplication(new WilsonGdxGame(), config);
	}
}