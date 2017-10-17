package com.massisframework.massis3.commons.loader.sh3d.xml;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.massisframework.massis3.commons.loader.sh3d.xml.model.DoorOrWindowXML;
import com.massisframework.massis3.commons.loader.sh3d.xml.model.FurnitureGroup;
import com.massisframework.massis3.commons.loader.sh3d.xml.model.FurnitureXML;
import com.massisframework.massis3.commons.loader.sh3d.xml.model.HomeXML;
import com.massisframework.massis3.commons.loader.sh3d.xml.model.PieceOfFurnitureXML;
import com.massisframework.massis3.commons.loader.sh3d.xml.model.Property;
import com.massisframework.massis3.commons.loader.sh3d.xml.model.Room;
import com.massisframework.massis3.commons.loader.sh3d.xml.model.WallXML;

public class XMLConversionUtil {

	public static Map<String, String> buildProperties(final List<?> obj)
	{
		return obj.stream().filter(Property.class::isInstance)
				.map(Property.class::cast)
				.collect(Collectors.toMap(p -> p.getName(), p -> p.getValue()));
	}

	public static <T> List<T> filterFor(final List<Object> obj,
			final Class<T> clazz)
	{
		return obj.stream()
				.filter(clazz::isInstance)
				.map(clazz::cast)
				.collect(Collectors.toList());
	}

	public static String getMassisGID(final Object obj)
	{
		return getProperty(obj, "MASSISGID");
	}

	public static void forEachFurnitureItem(final HomeXML home,
			final Consumer<FurnitureXML> action)
	{
		for (final Object obj : home.getAllFurniture())
		{
			furnitureVisitorFn(obj, action);
		}
	}

	private static void furnitureVisitorFn(final Object obj,
			final Consumer<FurnitureXML> action)
	{
		if (obj instanceof FurnitureXML)
		{
			action.accept((FurnitureXML) obj);
		} else if (obj instanceof FurnitureGroup)
		{
			for (final Object f : ((FurnitureGroup) obj).getAllFurniture())
			{
				furnitureVisitorFn(f, action);
			}
		}
	}

	public static String getProperty(final Object obj, final String key)
	{
		if (obj instanceof Collection)
		{
			return getPropertyValueInCollection((Collection) obj, key);
		}
		if (obj instanceof HomeXML)
		{
			return getPropertyValueInCollection(((HomeXML) obj).getProperty(),
					key);
		}
		if (obj instanceof WallXML)
		{
			return getPropertyValueInCollection(
					((WallXML) obj).getPropertyOrTextureOrBaseboard(), key);
		}
		if (obj instanceof Room)
		{
			return getPropertyValueInCollection(
					((Room) obj).getPropertyOrTextStyleOrTextureOrPoint(), key);
		}
		if (obj instanceof PieceOfFurnitureXML)
		{
			return getPropertyValueInCollection(
					((PieceOfFurnitureXML) obj).getProperty(), key);
		}
		if (obj instanceof DoorOrWindowXML)
		{
			return getPropertyValueInCollection(
					((DoorOrWindowXML) obj).getProperty(), key);
		}
		throw new UnsupportedOperationException(
				"Cannot get property of object of type "
						+ obj.getClass().getName());
	}

	public static String getPropertyValueInCollection(
			final Collection<?> col, final String key)
	{
		return col.stream()
				.filter(Property.class::isInstance)
				.map(Property.class::cast)
				.filter(p -> p.getName().equals(key))
				.map(p -> p.getValue())
				.findAny()
				.orElse(null);
	}

	public static Object getByMassisGID(final String idToSearch,
			final Collection<?>... collections)
	{
		for (final Collection<?> collection : collections)
		{
			for (final Object homeItem : collection)
			{
				final String massisGID = getMassisGID(homeItem);
				if (idToSearch.equals(massisGID))
				{
					return homeItem;
				}
			}
		}
		return null;
	}

	public static URI createZipURI(final Path zipFile)
	{
		return createZipURI(zipFile.toFile());
	}

	public static URI createZipURI(final File zipFile)
	{
		return createZipURI(zipFile.toURI());
	}

	public static URI createZipURI(final URI zipFileURI)
	{
		URI zipUri;
		try
		{
			zipUri = new URI("jar:" + zipFileURI.getScheme(),
					zipFileURI.getPath(),
					null);
			return zipUri;

		} catch (final URISyntaxException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Object findObjectWithMassisGID(final HomeXML home,
			final String massisGID)
	{
		return getByMassisGID(massisGID,
				home.getRoom(),
				home.getWall(),
				home.getAllFurniture());
	}

}
