package com.massisframework.massis3.commons.loader.sh3d.xml;

import static com.massisframework.massis3.commons.loader.sh3d.xml.XMLConversionUtil.createZipURI;
import static com.massisframework.massis3.commons.loader.sh3d.xml.XMLConversionUtil.forEachFurnitureItem;
import static com.massisframework.massis3.commons.loader.sh3d.xml.XMLConversionUtil.getMassisGID;
import static com.massisframework.massis3.commons.loader.sh3d.xml.XMLConversionUtil.getProperty;
import static com.massisframework.massis3.commons.spatials.Spatials.rawScale;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jme3.ai.navmesh.NavMesh;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.material.Material;
import com.jme3.material.plugin.export.material.J3MExporter;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.OBJLoader;
import com.jme3.system.JmeSystem;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.Texture2D;
import com.jme3.util.TangentBinormalGenerator;
import com.massisframework.massis3.commons.loader.GroupedObjLoader;
import com.massisframework.massis3.commons.loader.sh3d.xml.model.DoorOrWindowXML;
import com.massisframework.massis3.commons.loader.sh3d.xml.model.FurnitureXML;
import com.massisframework.massis3.commons.loader.sh3d.xml.model.HomeXML;
import com.massisframework.massis3.commons.loader.sh3d.xml.model.LevelXML;
import com.massisframework.massis3.commons.loader.sh3d.xml.model.MaterialXML;
import com.massisframework.massis3.commons.loader.sh3d.xml.model.Point;
import com.massisframework.massis3.commons.loader.sh3d.xml.model.Room;
import com.massisframework.massis3.commons.loader.sh3d.xml.model.TextureXML;
import com.massisframework.massis3.commons.loader.sh3d.xml.model.WallXML;
import com.massisframework.massis3.commons.pathfinding.UniformGridGraph;
import com.massisframework.massis3.commons.pathfinding.navmesh.impl.DefaultNavMeshFactory;
import com.massisframework.massis3.commons.spatials.FastLodGenerator;
import com.massisframework.massis3.commons.spatials.Spatials;

import jme3tools.converters.ImageToAwt;
import jme3tools.optimize.GeometryBatchFactory;

public class HomeXML2MassisBuildingExporter {

	private static Logger logger = LoggerFactory
			.getLogger(HomeXML2MassisBuildingExporter.class);
	private final AssetManager assetManager;
	private HomeXML home;
	private final Map<String/* massisGID */, String/* spatialId */> spatialIds = new HashMap<>();
	private final Map<String/* spatialId */, Spatial/* jme3 model */> modelMap = new HashMap<>();
	private final List<Material> materialList = new ArrayList<>();
	private HomeJmeBuilding homeStructure;
	private final File homeXMLZipFile;
	private final File outFile;

	public static void exportMassisBuilding(final File homeXMLZipFile,
			final File outFile)
			throws IOException
	{
		new HomeXML2MassisBuildingExporter(homeXMLZipFile, outFile).export();
	}

	private HomeXML2MassisBuildingExporter(final File homeXMLZipFile,
			final File outFile)
	{
		this.homeXMLZipFile = homeXMLZipFile;
		this.outFile = outFile;
		this.assetManager = JmeSystem
				.newAssetManager(JmeSystem.getPlatformAssetConfigURL());
		assetManager.registerLocator(homeXMLZipFile.getAbsolutePath(),
				ZipLocator.class);
		assetManager.unregisterLoader(OBJLoader.class);
		assetManager.registerLoader(GroupedObjLoader.class, "obj");
	}

	private static String wrapUUID(String prefix,String postfix)
	{
		return prefix+"_"+java.util.UUID.randomUUID().toString()+"_"+postfix;
	}
	private static String wrapUUID(String prefix)
	{
		return prefix+"_"+java.util.UUID.randomUUID().toString();
	}

	private void export() throws IOException
	{
		if (logger.isInfoEnabled())
		{
			logger.info("Started Scene export");
		}
		try (FileSystem fs = FileSystems.newFileSystem(
				createZipURI(this.homeXMLZipFile),
				Collections.emptyMap()))
		{
			try (InputStream is = Files.newInputStream(fs.getPath("Home.xml")))
			{
				if (logger.isInfoEnabled())
				{
					logger.info("Reading Home.xml");
				}
				final JAXBContext jaxbContext = JAXBContext
						.newInstance(HomeXML.class);
				final Unmarshaller jaxbUnmarshaller = jaxbContext
						.createUnmarshaller();
				this.home = (HomeXML) jaxbUnmarshaller.unmarshal(is);
				if (logger.isInfoEnabled())
				{
					logger.info("Loading home structure");
				}
				this.loadHomeStructure();
				if (logger.isInfoEnabled())
				{
					logger.info("Exporting home structure");
				}
				export(this.homeStructure, modelMap, materialList,
						outFile.toPath());
				if (logger.isInfoEnabled())
				{
					logger.info("Home structure exported");
				}
			}
		} catch (URISyntaxException | JAXBException e)
		{
			throw new IOException(e);
		}
	}

	private void loadHomeStructure()
	{

		final String staticNodeId = loadGeometryStructureSpatialsInCache();
		forEachFurnitureItem(this.home, this::loadFurnitureSpatialInCache);

		final List<WallObjectInfo> walls = home.getWall()
				.stream()
				.map(this::createWallObjectInfo)
				.collect(Collectors.toList());
		final List<RoomObjectInfo> rooms = home.getRoom()
				.stream()
				.map(this::createRoomObjectInfo)
				.collect(Collectors.toList());
		final List<FurnitureObjectInfo> furniture = new ArrayList<>();
		forEachFurnitureItem(home, (f) -> {
			furniture.add(createFurnitureObjectInfo(f));
		});
		final String version = home.getVersion();
		final String name = home.getName();
		final String sha1 = getProperty(home, "SHA1");
		this.homeStructure = new HomeJmeBuilding(
				version,
				name,
				sha1,
				"scene.massis",
				wrapUUID("models",".j3o"),
				wrapUUID("navmesh",".j3o"),
				wrapUUID("uniformGrid",".json"),
				wrapUUID("staticCollisionShape",".j3o"),
				staticNodeId,
				wrapUUID("materials"),
				wrapUUID("textures"),
				rooms,
				walls, furniture);

	}

	private String loadGeometryStructureSpatialsInCache()
	{
		final Spatial homeStructure = assetManager
				.loadModel(home.getStructure());
		final List<Geometry> geometries = new ArrayList<>();
		GeometryBatchFactory.gatherGeoms(homeStructure, geometries);
		// sanity check
		for (final Geometry g : geometries)
		{
			if (g.getName() == null || !g.getName().startsWith("MASSISGID"))
			{
				throw new UnsupportedOperationException(
						"All geometries must have an unique uuid. ");
			}
		}

		for (final Geometry g : geometries)
		{
			rawScale(g, 0.01f);
			final String massisGID = g.getName().split("_")[0];
			String spatialId = getSpatialId(massisGID);
			if (spatialId == null)
			{
				spatialId = "SPATIAL_" + massisGID;
				setSpatialId(massisGID, spatialId);
			}
			Node group = (Node) this.modelMap.get(spatialId);
			if (group == null)
			{
				group = new Node();
				modelMap.put(spatialId, group);
			}
			group.attachChild(g);
			loadMaterials(g, null);
		}
		Node staticNode = new Node();
		final String staticNodeId = UUID.randomUUID().toString();
		staticNode.setName(staticNodeId);
		geometries.stream().map(g -> g.clone(false))
				.forEach(staticNode::attachChild);
		staticNode = (Node) GeometryBatchFactory.optimize(staticNode);
		modelMap.put(staticNodeId, staticNode);
		return staticNodeId;
	}

	private RoomObjectInfo createRoomObjectInfo(final Room roomXML)
	{
		String levelId = null;
		final LevelXML lvl = (LevelXML) roomXML.getLevel();
		if (lvl != null)
		{
			levelId = lvl.getId();
		}
		final float elevation = lvl != null
				? Float.valueOf(lvl.getElevation()) * 0.01f
				: 0;
		final String massisGID = getMassisGID(roomXML);
		final String spatialId = getSpatialId(massisGID);
		final String name = roomXML.getName();
		final List<float[]> points = roomXML
				.getPropertyOrTextStyleOrTextureOrPoint().stream()
				.filter(Point.class::isInstance)
				.map(Point.class::cast)
				.map(p -> new float[] {
						Float.valueOf(p.getX()) * 0.01f,
						elevation,
						Float.valueOf(p.getY()) * 0.01f })
				.collect(Collectors.toList());

		final RoomObjectInfo roI = new RoomObjectInfo(
				levelId,
				massisGID,
				spatialId,
				name,
				points);
		return roI;
	}

	private WallObjectInfo createWallObjectInfo(final WallXML obj)
	{
		final WallXML wallXML = obj;
		String levelId = null;
		final LevelXML lvl = (LevelXML) wallXML.getLevel();
		if (lvl != null)
		{
			levelId = lvl.getId();
		}
		final float elevation = lvl != null
				? Float.valueOf(lvl.getElevation()) * 0.01f
				: 0;
		final float heightAtStart = Float.valueOf(wallXML.getHeight())
				* 0.01f;
		float heightAtEnd = heightAtStart;
		if (wallXML.getHeightAtEnd() != null)
		{
			heightAtEnd = Float.valueOf(wallXML.getHeightAtEnd())
					* 0.01f;
		}
		final float thickness = Float.valueOf(wallXML.getThickness()) * 0.01f;
		final String massisGID = getMassisGID(obj);
		final String spatialId = getSpatialId(massisGID);

		final String pointsStr = getProperty(wallXML, "points");
		final float[][] pointsF = new Gson().fromJson(pointsStr,
				float[][].class);

		final List<float[]> points = Arrays.stream(pointsF)
				.map(p -> new float[] {
						p[0] * 0.01f, elevation, p[1] * 0.01f
				}).collect(Collectors.toList());

		final WallObjectInfo wall = new WallObjectInfo(levelId, massisGID,
				spatialId, heightAtStart, heightAtEnd, thickness,
				points);
		return wall;
	}

	private String getSpatialId(final String massisGID)
	{
		return this.spatialIds.get(massisGID);
	}

	private void setSpatialId(final String massisGID, final String spatialId)
	{
		this.spatialIds.put(massisGID, spatialId);
	}

	/**
	 * MUST be cloned if modified
	 * 
	 * @return
	 */
	private Spatial getPrototypeModel(final String spatialId)
	{
		return this.modelMap.get(spatialId);
	}

	private void loadFurnitureSpatialInCache(final FurnitureXML f)
	{

		final String spatialId = f.getModel();

		setSpatialId(getMassisGID(f), spatialId);
		// get spatialId
		Spatial modelSp = this.modelMap.get(spatialId);
		if (modelSp == null)
		{
			modelSp = assetManager.loadModel(spatialId);
			if (modelSp instanceof Node)
			{
				modelSp = GeometryBatchFactory.optimize((Node) modelSp);
				FastLodGenerator.bakeAllLods(modelSp, 1, 0.3f, 0.5f, 0.7f,
						0.9f);
				TangentBinormalGenerator.generate(modelSp, true);
			}
			this.modelMap.put(spatialId, modelSp);
		}

	}

	private FurnitureObjectInfo createFurnitureObjectInfo(
			final FurnitureXML obj)
	{
		final String spatialId = getSpatialId(getMassisGID(obj));
		final Spatial model = getPrototypeModel(spatialId).clone();

		final Matrix4f nt = getNormalizedTransform(model,
				obj.getModelRotationAsMatrix(), 1f);

		final Transform t0 = new Transform();
		t0.fromTransformMatrix(nt);
		float levelElevation = 0;

		if (obj.getLevel() != null)
		{
			final LevelXML lvl = obj.getLevel();
			levelElevation = Float.valueOf(lvl.getElevation());
		}
		final Matrix4f scale = new Matrix4f();

		float pieceWidth = Float.valueOf(obj.getWidth()) * 0.01f;
		final float pieceHeight = Float.valueOf(obj.getHeight()) * 0.01f;
		final float pieceDepth = Float.valueOf(obj.getDepth()) * 0.01f;
		final float pieceElev = Float.valueOf(obj.getElevation()) * 0.01f;
		final float pieceX = Float.valueOf(obj.getX()) * 0.01f;
		final float pieceY = Float.valueOf(obj.getY()) * 0.01f;
		final float pieceLevelElevation = levelElevation * 0.01f;
		final float pieceAngle = Float.valueOf(obj.getAngle());

		if ("true".equals(obj.getModelMirrored()))
		{
			pieceWidth *= -1;
		}
		scale.scale(new Vector3f(pieceWidth, pieceHeight, pieceDepth));
		final Matrix4f orientation = new Matrix4f();
		orientation.fromAngleAxis(-pieceAngle, Vector3f.UNIT_Y);
		orientation.multLocal(scale);
		final Matrix4f pieceTransform = new Matrix4f();
		float z = pieceElev + pieceHeight / 2f;
		z += pieceLevelElevation;
		pieceTransform.setTranslation(pieceX, z, pieceY);
		pieceTransform.multLocal(orientation);

		final Transform t1 = new Transform();
		t1.fromTransformMatrix(pieceTransform);
		final Transform t2 = t0.combineWithParent(t1);
		model.setLocalTransform(t2);
		this.loadMaterial(obj, model);
		model.updateGeometricState();
		final String massisGID = getMassisGID(obj);
		final Map<String, Integer> materials = getMaterials(model);
		String levelId = null;
		if (obj.getLevel() != null)
		{
			levelId = obj.getLevel().getId();
		}
		final FurnitureObjectInfo fInfo = new FurnitureObjectInfo(
				levelId, massisGID, spatialId,
				obj.getName(),
				model.getLocalTranslation(),
				model.getLocalRotation(),
				model.getLocalScale(),
				obj instanceof DoorOrWindowXML,
				materials);

		return fInfo;
	}

	private void loadMaterials(final Spatial model,
			final Map<String, Integer> materials)
	{
		model.depthFirstTraversal(new SceneGraphVisitorAdapter() {
			@Override
			public void visit(final Geometry geom)
			{
				final Material mat = geom.getMaterial();
				int index = -1;
				for (int i = 0; i < materialList.size(); i++)
				{
					if (materialList.get(i).contentEquals(mat))
					{
						index = i;
						break;
					}
				}
				if (index == -1)
				{
					materialList.add(mat);
					index = materialList.size() - 1;
				}
				if (materials != null)
				{
					materials.put(geom.getName(), index);
				}
			}
		});
	}

	private Map<String, Integer> getMaterials(final Spatial model)
	{
		final Map<String, Integer> materials = new HashMap<>();
		loadMaterials(model, materials);
		return materials;
	}

	private void loadMaterial(
			final FurnitureXML obj,
			final Spatial spModel)
	{
		final List<Geometry> geoms = new ArrayList<>();
		spModel.depthFirstTraversal(new SceneGraphVisitorAdapter() {
			@Override
			public void visit(final Geometry geom)
			{
				geoms.add(geom);
			}
		});
		final List<MaterialXML> objMaterials = obj.getMaterial();

		for (final MaterialXML materialXML : objMaterials)
		{
			for (final Geometry g : geoms)
			{
				final Material geomMat = g.getMaterial().clone();
				if (materialXML.getName().equals(geomMat.getName()))
				{
					final TextureXML tex = materialXML.getTexture();
					final String color = materialXML.getColor();
					if (tex != null)
					{
						applyImgTexture(tex.getImage(), geomMat);
					}
					if (!Strings.isNullOrEmpty(color))
					{
						geomMat.setColor("Diffuse", fromHexString(color));
					}
				}
				g.setMaterial(geomMat);
			}
		}
	}

	private static ColorRGBA fromHexString(final String hexString)
	{
		final int hex = new BigInteger(hexString, 16).intValue();
		return fromIntColor(hex);
	}

	private static ColorRGBA fromIntColor(final int argb)
	{
		final java.awt.Color cc = new java.awt.Color(argb);
		return new ColorRGBA(cc.getRed() / 255f, cc.getGreen() / 255f,
				cc.getBlue() / 255f, 1);
	}

	private Material applyImgTexture(final String imgPath,
			final Material original)
	{

		// TODO extension is hardcoded. This might fail
		final Texture2D texture = (Texture2D) assetManager
				.loadTexture(new TextureKey(imgPath) {

					@Override
					public String getExtension()
					{
						if (imgPath.contains("."))
						{
							return super.getExtension();
						} else
						{
							return "jpg";
						}
					}

				});
		texture.setWrap(WrapMode.Repeat);
		original.setTexture("DiffuseMap", texture);
		original.setBoolean("UseMaterialColors", true);
		original.setColor("Ambient",
				new ColorRGBA(0.2f, 0.2f, 0.2f, 1));
		original.setColor("Diffuse",
				new ColorRGBA(1, 1, 1, 1));
		original.setColor("Specular",
				new ColorRGBA(0, 0, 0, 1));
		return original;

	}

	public static Matrix4f getNormalizedTransform(final Spatial node,
			final float[][] modelRotation, final float width)
	{
		// Get model bounding box size
		// var modelBounds = this.getBounds(node);
		final BoundingBox modelBounds = (BoundingBox) node.getWorldBound();
		// var lower = vec3.create();
		final Vector3f lower = new Vector3f();

		// modelBounds.getLower(lower);
		modelBounds.getMin(lower);

		// var upper = vec3.create();
		final Vector3f upper = new Vector3f();
		// modelBounds.getUpper(upper);
		modelBounds.getMax(upper);
		// Translate model to its center
		final Matrix4f translation = new Matrix4f();
		translation.setTranslation(-lower.x - (upper.x - lower.x) / 2,
				-lower.y - (upper.y - lower.y) / 2,
				-lower.z - (upper.z - lower.z) / 2

		);
		// var modelTransform;
		Matrix4f modelTransform = Matrix4f.IDENTITY;
		if (modelRotation != null && modelRotation != null)
		{

			// Get model bounding box size with model rotation
			final Matrix3f mRot = new Matrix3f().set(modelRotation);
			// mat4.mul(modelTransform, modelTransform, translation);

			modelTransform.setRotationQuaternion(
					new Quaternion().fromRotationMatrix(mRot));
			modelTransform.multLocal(translation);

		} else
		{
			modelTransform = translation;
		}
		// Scale model to make it fill a 1 unit wide box
		final Matrix4f scaleOneTransform = new Matrix4f();
		scaleOneTransform.setScale(new Vector3f(
				width / Math.max(getMinimumSize(), upper.x - lower.x),
				width / Math.max(getMinimumSize(), upper.y - lower.y),
				width / Math.max(getMinimumSize(), upper.z - lower.z)));
		scaleOneTransform.multLocal(modelTransform);
		return scaleOneTransform;
	}

	private static float getMinimumSize()
	{
		return 0.0001f;
	}

	private void export(
			final HomeJmeBuilding building,
			final Map<String, Spatial> modelMap,
			final List<Material> materialList,
			final Path zipFile) throws IOException,
			URISyntaxException
	{
		final Map<String, String> env = new HashMap<String, String>();
		Files.deleteIfExists(zipFile);
		env.put("create", "true");
		final URI zipUri = createZipURI(zipFile);
		try (FileSystem fs = FileSystems.newFileSystem(zipUri, env))
		{
			final Path root = fs.getPath("/");
			final Path materialDir = Files.createDirectories(
					root.resolve(building.getMaterialsFolder()));
			final Path texturesDir = Files.createDirectories(
					root.resolve(building.getTexturesFolder()));
			/*
			 * Material & textures export
			 */
			logger.info("Exporting materials and textures");
			exportMaterialsAndTextures(materialDir, texturesDir, materialList);
			final Node outNode = new Node();
			modelMap.forEach((k, v) -> {
				v.setName(k);
				outNode.attachChild(v);
			});
			/*
			 * Store binary scene
			 */
			logger.info("Saving binary scene");
			final BinaryExporter ex = BinaryExporter.getInstance();
			try (OutputStream os = Files.newOutputStream(
					fs.getPath(building.getModelsFile()),
					StandardOpenOption.CREATE_NEW))
			{
				ex.save(outNode, os);
				os.flush();
			}
			/*
			 * Collision shapes & navmesh generation
			 */
			logger.info("Generating navmesh & collision shapes...");
			precomputeStructures(building, this.modelMap);
			logger.info("Saving navmesh");
			try (OutputStream os = Files.newOutputStream(
					fs.getPath(building.getNavMeshFile()),
					StandardOpenOption.CREATE_NEW))
			{
				ex.save(building.getNavMeshLoader().get(), os);
				os.flush();
			}
			/*
			 * Uniform grid generate & save
			 */
			logger.info("Saving navmesh grid");
			try (OutputStream os = Files.newOutputStream(
					fs.getPath(building.getUniformGridFile()),
					StandardOpenOption.CREATE_NEW))
			{
				final OutputStreamWriter w = new OutputStreamWriter(os);
				new GsonBuilder()
						// .setPrettyPrinting()
						.create()
						.toJson(building.getUniformGridLoader().get(), w);
				w.flush();
			}

			/*
			 * store Collision shapes
			 */
			logger.info("Saving collision shapes");
			try (OutputStream os = Files.newOutputStream(
					fs.getPath(building.getStaticCollisionShapeFile()),
					StandardOpenOption.CREATE_NEW))
			{

				final Node node = new Node();
				final AtomicInteger count = new AtomicInteger();
				final List<CollisionShape> css = building
						.getStaticCollisionShapeLoader().get();
				css.forEach(c -> node
						.setUserData("CS_ " + count.getAndIncrement(), c));
				ex.save(node, os);
				os.flush();
			}
			/*
			 * Dump json scene info
			 */
			logger.info("Saving json scene information");
			try (OutputStream os = Files.newOutputStream(
					fs.getPath(building.getDescriptionFile()),
					StandardOpenOption.CREATE_NEW))
			{
				final OutputStreamWriter w = new OutputStreamWriter(os);
				new GsonBuilder()
						.setPrettyPrinting()
						.create()
						.toJson(building, w);
				w.flush();
			}
		}
	}

	private void exportMaterialsAndTextures(
			final Path materialDir,
			final Path texturesDir,
			final List<Material> materials) throws IOException
	{

		final Map<String, String> texAliases = new HashMap<>();
		int count = 0;
		for (final Material mat : materials)
		{
			exportMaterial(mat, materialDir, texturesDir, texAliases, count++);
		}

	}

	private Material exportMaterial(
			final Material material,
			final Path materialDir,
			final Path texturesDir,
			final Map<String, String> textureKeyAliases, final int matId)
			throws IOException
	{
		//
		// material = material.clone();

		if (material.getParam("DiffuseMap") != null)
		{

			final Texture texVal = material.getTextureParam("DiffuseMap")
					.getTextureValue();

			// ?¿
			texVal.setMinFilter(MinFilter.Trilinear);
			TextureKey texKey = (TextureKey) texVal.getKey();
			final Image image = texVal.getImage();
			if (image != null)
			{
				String texPath = textureKeyAliases.get(texKey.getName());
				if (texPath == null)
				{
					final BufferedImage bi = ImageToAwt.convert(image, false,
							true,
							0);
					final String texNameNoEx = "tex_" + UUID.randomUUID().toString();// TEX_COUNT.incrementAndGet();
					final String extension = "png";
					final String texFileName = texNameNoEx + "." + extension;
					final Path texOutputPath = texturesDir.resolve(texFileName);
					try (OutputStream os = Files.newOutputStream(
							texOutputPath,
							StandardOpenOption.CREATE_NEW))
					{
						ImageIO.write(bi, extension, os);
					}
					texPath = texOutputPath.toAbsolutePath().toString();
					// remove / at start
					if (texPath.startsWith("/"))
					{
						texPath = texPath.substring(1, texPath.length());
					}
					textureKeyAliases.put(texKey.getName(), texPath);
				}
				// return null;
				texKey = new TextureKey(texPath, texKey.isFlipY());
				// Y falta el cubeMap y eso
				texVal.setKey(texKey);
				texKey.setGenerateMips(true);
				material.getTextureParam("DiffuseMap").setTextureValue(texVal);
			}

		}

		final String mName = matId + ".j3m";
		final J3MExporter materialExporter = new J3MExporter();
		Files.createDirectories(materialDir);
		final Path mPath = materialDir.resolve(mName);
		try (OutputStream os = Files.newOutputStream(mPath,
				StandardOpenOption.CREATE_NEW))
		{
			materialExporter.save(material, os);
		}
		return material;

	}

	private static void precomputeStructures(
			final HomeJmeBuilding building,
			final Map<String, Spatial> modelsMap)
	{
		final Node world = MassisBuildingLoader.loadAllSpatialsNoBatch(
				building,
				modelsMap,
				Collections.emptyList());

		logger.info("Creating ghost objects");
		final long a = System.currentTimeMillis();
		Spatials.ensureNativeLibsLoaded();
		final List<CollisionShape> css = Spatials.streamGeometries(world)
				.map(Geometry::clone)
				.map(CollisionShapeFactory::createMeshShape)
				.collect(Collectors.toList());
		final long b = System.currentTimeMillis();
		logger.info(
				"Finished creating ghost objects. Took " + (b - a) + " ms.");
		building.setStaticCollisionShapeLoader(
				() -> css);
		final Mesh rawMesh = new DefaultNavMeshFactory().generateRawMesh(world);
		building.setNavMeshLoader(() -> rawMesh);
		logger.info("Creating uniform Grid Graph...");
		UniformGridGraph ug = new UniformGridGraph();
		ug.build((NavMesh) new DefaultNavMeshFactory().buildNavigationMesh(rawMesh), 0.7f);
		building.setUniformGridGraphLoader(() -> ug);
		logger.info("Uniform grid graph created");

	}

}
