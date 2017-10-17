package com.massisframework.massis3.commons.loader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;

public class JsonLoader implements AssetLoader {

	@Override
	public Object load(final AssetInfo assetInfo) throws IOException
	{
		try (Reader reader = new InputStreamReader(assetInfo.openStream()))
		{
			return new Gson().fromJson(reader, JsonObject.class);
		}
	}

}
