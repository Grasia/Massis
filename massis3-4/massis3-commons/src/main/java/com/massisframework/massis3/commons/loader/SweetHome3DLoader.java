package com.massisframework.massis3.commons.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import com.eteks.sweethome3d.model.RecorderException;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import com.massisframework.massis3.commons.editor.MassisSceneCompiler;
import com.massisframework.massis3.commons.loader.sh3d.xml.MassisBuildingLoader;
import com.massisframework.massis3.commons.spatials.Spatials;

public class SweetHome3DLoader implements AssetLoader {

	@Override
	public synchronized Object load(final AssetInfo assetInfo)
			throws IOException
	{
		final Path targetHomePath = Spatials.MASSIS_ASSET_PATH
				.resolve(assetInfo.getKey().getName());
		Files.createDirectories(targetHomePath.getParent());
		try (InputStream is = assetInfo.openStream())
		{
			FileUtils.copyInputStreamToFile(is, targetHomePath.toFile());
		}
		try
		{
			final Path scenePath = MassisSceneCompiler
					.compileHome(targetHomePath);
			return MassisBuildingLoader.loadSpatials(scenePath.toFile());
		} catch (final RecorderException e)
		{
			throw new IOException(e);
		}
	}

}
