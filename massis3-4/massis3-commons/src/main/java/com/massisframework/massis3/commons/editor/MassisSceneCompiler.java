package com.massisframework.massis3.commons.editor;

import static com.massisframework.massis3.commons.loader.sh3d.xml.XMLConversionUtil.createZipURI;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.massisframework.massis3.commons.loader.sh3d.xml.HomeJmeBuilding;
import com.massisframework.massis3.commons.loader.sh3d.xml.HomeXML2MassisBuildingExporter;

public class MassisSceneCompiler {

	static Logger logger = LoggerFactory.getLogger(MassisSceneCompiler.class);

	public static Path compileHome(final Path homePath)
			throws IOException, RecorderException
	{
		final String homeFileName = com.google.common.io.Files
				.getNameWithoutExtension(homePath.getFileName().toString());
		final Path sceneFilePath = homePath.getParent()
				.resolve(homeFileName + "."+HomeJmeBuilding.MASSIS_SCENE_EXTENSION);
		return compileHome(homePath, sceneFilePath);
	}

	public static synchronized Path compileHome(final Path homePath,
			final Path sceneFilePath)
			throws IOException, RecorderException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("Compiling scene " + homePath);
		}

		boolean recompileNeeded = true;
		if (Files.exists(sceneFilePath))
		{
			// check sha1
			if (logger.isInfoEnabled())
			{
				logger.info("A compiled scene already exists. Checking SHA1");
			}
			try (FileSystem fs = FileSystems.newFileSystem(
					createZipURI(sceneFilePath),
					Collections.emptyMap()))
			{

				try (Reader r = new InputStreamReader(
						Files.newInputStream(fs.getPath("/scene.massis"))))
				{
					final HomeJmeBuilding b = new Gson().fromJson(r,
							HomeJmeBuilding.class);
					final String buildingSha1 = b.getSha1();
					final String sh3dsha1 = com.google.common.io.Files
							.hash(homePath.toFile(), Hashing.sha1()).toString();
					if (buildingSha1.equals(sh3dsha1))
					{
						if (logger.isInfoEnabled())
						{
							logger.info(
									"SHA1 matches. There is no need for recompile");
						}
						recompileNeeded = false;

					} else
					{
						if (logger.isInfoEnabled())
						{
							logger.info("SHA1 differs. Recompiling!");
						}
						recompileNeeded = true;
					}

				}
			}
		}
		if (recompileNeeded)
		{
			if (logger.isInfoEnabled())
			{
				logger.info("Exporting XML Home");
			}
			final File out = OperatingSystem.createTemporaryFile(
					"home_zip_export",
					".zip");
			final HomeXMLFileRecorder xmlFileRecorder = new HomeXMLFileRecorder(
					9,
					HomeXMLFileRecorder.CONVERT_MODELS_TO_OBJ_FORMAT |
							HomeXMLFileRecorder.INCLUDE_HOME_STRUCTURE |
							HomeXMLFileRecorder.REDUCE_IMAGES);
			xmlFileRecorder.exportHome(homePath.toFile(), out, null);
			if (logger.isInfoEnabled())
			{
				logger.info("Exporting massis Scene");
			}
			exportMassisBuilding(out, sceneFilePath.toFile());
			if (logger.isInfoEnabled())
			{
				logger.info("Deleting home xml tmp file");
			}
			Files.delete(out.toPath());
		}
		if (logger.isInfoEnabled())
		{
			logger.info("Done compiling " + homePath
					+ ". Compiled scene located at: " + sceneFilePath);
		}
		return sceneFilePath;
	}

	public static synchronized void exportMassisBuilding(final File homeXMLZipFile,
			final File outFile)
			throws IOException
	{
		HomeXML2MassisBuildingExporter.exportMassisBuilding(homeXMLZipFile,
				outFile);
	}

}
