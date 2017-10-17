package com.massisframework.massis3.commons.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanOpenHashSet;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatOpenHashSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;

@SuppressWarnings("unchecked")
public final class CollectionsFactory {

	private CollectionsFactory()
	{
	}

	public static <T> List<T> createList(final Class<T> clazz)
	{
		if (Byte.class.isAssignableFrom(clazz))
		{
			return (List<T>) new ByteArrayList();
		}
		if (Short.class.isAssignableFrom(clazz))
		{
			return (List<T>) new ShortArrayList();
		}
		if (Integer.class.isAssignableFrom(clazz))
		{
			return (List<T>) new IntArrayList();
		}
		if (Long.class.isAssignableFrom(clazz))
		{
			return (List<T>) new LongArrayList();
		}
		if (Float.class.isAssignableFrom(clazz))
		{
			return (List<T>) new FloatArrayList();
		}
		if (Double.class.isAssignableFrom(clazz))
		{
			return (List<T>) new DoubleArrayList();
		}
		if (Boolean.class.isAssignableFrom(clazz))
		{
			return (List<T>) new BooleanArrayList();
		}
		if (Character.class.isAssignableFrom(clazz))
		{
			return (List<T>) new CharArrayList();
		}

		return new ArrayList<T>();
	}

	public static <T> Set<T> createSet(final Class<T> clazz)
	{
		if (Byte.class.isAssignableFrom(clazz))
		{
			return (Set<T>) new ByteOpenHashSet();
		}
		if (Short.class.isAssignableFrom(clazz))
		{
			return (Set<T>) new ShortOpenHashSet();
		}
		if (Integer.class.isAssignableFrom(clazz))
		{
			return (Set<T>) new IntOpenHashSet();
		}
		if (Long.class.isAssignableFrom(clazz))
		{
			return (Set<T>) new LongOpenHashSet();
		}
		if (Float.class.isAssignableFrom(clazz))
		{
			return (Set<T>) new FloatOpenHashSet();
		}
		if (Double.class.isAssignableFrom(clazz))
		{
			return (Set<T>) new DoubleOpenHashSet();
		}
		if (Boolean.class.isAssignableFrom(clazz))
		{
			return (Set<T>) new BooleanOpenHashSet();
		}
		if (Character.class.isAssignableFrom(clazz))
		{
			return (Set<T>) new CharOpenHashSet();
		}

		return new HashSet<T>();
	}

	public static <K, E> Map<K, List<E>> createMapOfLists(final Class<K> clazz,
			final Class<E> elemType)
	{
		return (Map) createMap(clazz, List.class);
	}

	public static <K, E> Map<K, Set<E>> createMapOfSets(final Class<K> clazz,
			final Class<E> elemType)
	{
		return (Map) createMap(clazz, Set.class);
	}

	public static <K, V> Map<K, V> createMap(final Class<K> clazz,
			final Class<V> valueType)
	{
		if (Byte.class.isAssignableFrom(clazz))
		{
			return (Map<K, V>) createByteMap(valueType);
		}
		if (Short.class.isAssignableFrom(clazz))
		{
			return (Map<K, V>) createShortMap(valueType);
		}
		if (Integer.class.isAssignableFrom(clazz))
		{
			return (Map<K, V>) createIntMap(valueType);
		}
		if (Long.class.isAssignableFrom(clazz))
		{
			return (Map<K, V>) createLongMap(valueType);
		}
		if (Float.class.isAssignableFrom(clazz))
		{
			return (Map<K, V>) createFloatMap(valueType);
		}
		if (Double.class.isAssignableFrom(clazz))
		{
			return (Map<K, V>) createDoubleMap(valueType);
		}

		if (Character.class.isAssignableFrom(clazz))
		{
			return (Map<K, V>) createCharMap(valueType);
		}
		return new HashMap<>();
	}

	private static <T> Map<Byte, T> createByteMap(final Class<T> clazz)
	{
//		if (Byte.class.isAssignableFrom(clazz))
//		{
//			return (Map<Byte, T>) new Byte2ByteOpenHashMap();
//		}
//		if (Short.class.isAssignableFrom(clazz))
//		{
//			return (Map<Byte, T>) new Byte2ShortOpenHashMap();
//		}
//		if (Integer.class.isAssignableFrom(clazz))
//		{
//			return (Map<Byte, T>) new Byte2IntOpenHashMap();
//		}
//		if (Long.class.isAssignableFrom(clazz))
//		{
//			return (Map<Byte, T>) new Byte2LongOpenHashMap();
//		}
//		if (Float.class.isAssignableFrom(clazz))
//		{
//			return (Map<Byte, T>) new Byte2FloatOpenHashMap();
//		}
//		if (Double.class.isAssignableFrom(clazz))
//		{
//			return (Map<Byte, T>) new Byte2DoubleOpenHashMap();
//		}
//		if (Boolean.class.isAssignableFrom(clazz))
//		{
//			return (Map<Byte, T>) new Byte2BooleanOpenHashMap();
//		}
//		if (Character.class.isAssignableFrom(clazz))
//		{
//			return (Map<Byte, T>) new Byte2CharOpenHashMap();
//		}
		return new Byte2ObjectOpenHashMap<T>();
	}

	private static <T> Map<Short, T> createShortMap(final Class<T> clazz)
	{
//		if (Byte.class.isAssignableFrom(clazz))
//		{
//			return (Map<Short, T>) new Short2ByteOpenHashMap();
//		}
//		if (Short.class.isAssignableFrom(clazz))
//		{
//			return (Map<Short, T>) new Short2ShortOpenHashMap();
//		}
//		if (Integer.class.isAssignableFrom(clazz))
//		{
//			return (Map<Short, T>) new Short2IntOpenHashMap();
//		}
//		if (Long.class.isAssignableFrom(clazz))
//		{
//			return (Map<Short, T>) new Short2LongOpenHashMap();
//		}
//		if (Float.class.isAssignableFrom(clazz))
//		{
//			return (Map<Short, T>) new Short2FloatOpenHashMap();
//		}
//		if (Double.class.isAssignableFrom(clazz))
//		{
//			return (Map<Short, T>) new Short2DoubleOpenHashMap();
//		}
//		if (Boolean.class.isAssignableFrom(clazz))
//		{
//			return (Map<Short, T>) new Short2BooleanOpenHashMap();
//		}
//		if (Character.class.isAssignableFrom(clazz))
//		{
//			return (Map<Short, T>) new Short2CharOpenHashMap();
//		}
		return new Short2ObjectOpenHashMap<T>();
	}

	private static <T> Map<Integer, T> createIntMap(final Class<T> clazz)
	{
//		if (Byte.class.isAssignableFrom(clazz))
//		{
//			return (Map<Integer, T>) new Int2ByteOpenHashMap();
//		}
//		if (Short.class.isAssignableFrom(clazz))
//		{
//			return (Map<Integer, T>) new Int2ShortOpenHashMap();
//		}
//		if (Integer.class.isAssignableFrom(clazz))
//		{
//			return (Map<Integer, T>) new Int2IntOpenHashMap();
//		}
//		if (Long.class.isAssignableFrom(clazz))
//		{
//			return (Map<Integer, T>) new Int2LongOpenHashMap();
//		}
//		if (Float.class.isAssignableFrom(clazz))
//		{
//			return (Map<Integer, T>) new Int2FloatOpenHashMap();
//		}
//		if (Double.class.isAssignableFrom(clazz))
//		{
//			return (Map<Integer, T>) new Int2DoubleOpenHashMap();
//		}
//		if (Boolean.class.isAssignableFrom(clazz))
//		{
//			return (Map<Integer, T>) new Int2BooleanOpenHashMap();
//		}
//		if (Character.class.isAssignableFrom(clazz))
//		{
//			return (Map<Integer, T>) new Int2CharOpenHashMap();
//		}
		return new Int2ObjectOpenHashMap<T>();
	}

	private static <T> Map<Long, T> createLongMap(final Class<T> clazz)
	{
//		if (Byte.class.isAssignableFrom(clazz))
//		{
//			return (Map<Long, T>) new Long2ByteOpenHashMap();
//		}
//		if (Short.class.isAssignableFrom(clazz))
//		{
//			return (Map<Long, T>) new Long2ShortOpenHashMap();
//		}
//		if (Integer.class.isAssignableFrom(clazz))
//		{
//			return (Map<Long, T>) new Long2IntOpenHashMap();
//		}
//		if (Long.class.isAssignableFrom(clazz))
//		{
//			return (Map<Long, T>) new Long2LongOpenHashMap();
//		}
//		if (Float.class.isAssignableFrom(clazz))
//		{
//			return (Map<Long, T>) new Long2FloatOpenHashMap();
//		}
//		if (Double.class.isAssignableFrom(clazz))
//		{
//			return (Map<Long, T>) new Long2DoubleOpenHashMap();
//		}
//		if (Boolean.class.isAssignableFrom(clazz))
//		{
//			return (Map<Long, T>) new Long2BooleanOpenHashMap();
//		}
//		if (Character.class.isAssignableFrom(clazz))
//		{
//			return (Map<Long, T>) new Long2CharOpenHashMap();
//		}
		return new Long2ObjectOpenHashMap<T>();
	}

	private static <T> Map<Float, T> createFloatMap(final Class<T> clazz)
	{
//		if (Byte.class.isAssignableFrom(clazz))
//		{
//			return (Map<Float, T>) new Float2ByteOpenHashMap();
//		}
//		if (Short.class.isAssignableFrom(clazz))
//		{
//			return (Map<Float, T>) new Float2ShortOpenHashMap();
//		}
//		if (Integer.class.isAssignableFrom(clazz))
//		{
//			return (Map<Float, T>) new Float2IntOpenHashMap();
//		}
//		if (Long.class.isAssignableFrom(clazz))
//		{
//			return (Map<Float, T>) new Float2LongOpenHashMap();
//		}
//		if (Float.class.isAssignableFrom(clazz))
//		{
//			return (Map<Float, T>) new Float2FloatOpenHashMap();
//		}
//		if (Double.class.isAssignableFrom(clazz))
//		{
//			return (Map<Float, T>) new Float2DoubleOpenHashMap();
//		}
//		if (Boolean.class.isAssignableFrom(clazz))
//		{
//			return (Map<Float, T>) new Float2BooleanOpenHashMap();
//		}
//		if (Character.class.isAssignableFrom(clazz))
//		{
//			return (Map<Float, T>) new Float2CharOpenHashMap();
//		}
		return new Float2ObjectOpenHashMap<T>();
	}

	private static <T> Map<Double, T> createDoubleMap(final Class<T> clazz)
	{
//		if (Byte.class.isAssignableFrom(clazz))
//		{
//			return (Map<Double, T>) new Double2ByteOpenHashMap();
//		}
//		if (Short.class.isAssignableFrom(clazz))
//		{
//			return (Map<Double, T>) new Double2ShortOpenHashMap();
//		}
//		if (Integer.class.isAssignableFrom(clazz))
//		{
//			return (Map<Double, T>) new Double2IntOpenHashMap();
//		}
//		if (Long.class.isAssignableFrom(clazz))
//		{
//			return (Map<Double, T>) new Double2LongOpenHashMap();
//		}
//		if (Float.class.isAssignableFrom(clazz))
//		{
//			return (Map<Double, T>) new Double2FloatOpenHashMap();
//		}
//		if (Double.class.isAssignableFrom(clazz))
//		{
//			return (Map<Double, T>) new Double2DoubleOpenHashMap();
//		}
//		if (Boolean.class.isAssignableFrom(clazz))
//		{
//			return (Map<Double, T>) new Double2BooleanOpenHashMap();
//		}
//		if (Character.class.isAssignableFrom(clazz))
//		{
//			return (Map<Double, T>) new Double2CharOpenHashMap();
//		}
		return new Double2ObjectOpenHashMap<T>();
	}

	private static <T> Map<Character, T> createCharMap(final Class<T> clazz)
	{
//		if (Byte.class.isAssignableFrom(clazz))
//		{
//			return (Map<Character, T>) new Char2ByteOpenHashMap();
//		}
//		if (Short.class.isAssignableFrom(clazz))
//		{
//			return (Map<Character, T>) new Char2ShortOpenHashMap();
//		}
//		if (Integer.class.isAssignableFrom(clazz))
//		{
//			return (Map<Character, T>) new Char2IntOpenHashMap();
//		}
//		if (Long.class.isAssignableFrom(clazz))
//		{
//			return (Map<Character, T>) new Char2LongOpenHashMap();
//		}
//		if (Float.class.isAssignableFrom(clazz))
//		{
//			return (Map<Character, T>) new Char2FloatOpenHashMap();
//		}
//		if (Double.class.isAssignableFrom(clazz))
//		{
//			return (Map<Character, T>) new Char2DoubleOpenHashMap();
//		}
//		if (Boolean.class.isAssignableFrom(clazz))
//		{
//			return (Map<Character, T>) new Char2BooleanOpenHashMap();
//		}
//		if (Character.class.isAssignableFrom(clazz))
//		{
//			return (Map<Character, T>) new Char2CharOpenHashMap();
//		}
		return new Char2ObjectOpenHashMap<T>();
	}
}
