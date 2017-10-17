package com.massisframework.massis3.commons.json;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import io.gsonfire.GsonFireBuilder;
import io.gsonfire.PostProcessor;
import io.gsonfire.TypeSelector;

@SuppressWarnings("rawtypes")
public class TypeSerializer implements
		PostProcessor,
		TypeSelector,
		ClassAliasProvider {

	public static final String TYPE_KEY = "__type";
	public static final String TYPE_ID_KEY = "__typeId";

	private final BiMap<Class, String> classMap;
	private final BiMap<Class, Short> idMap;
	private final Set<Class> interfaces;

	public TypeSerializer()
	{
		this.classMap = HashBiMap.create();
		this.interfaces = new HashSet<>();
		this.idMap = HashBiMap.create();
	}

	public void registerOn(final GsonFireBuilder fireBuilder)
	{
		this.interfaces.forEach(itf -> {
			fireBuilder.registerPostProcessor(itf, this);
			fireBuilder.registerTypeSelector(itf, this);
		});
	}

	@Override
	public String getClassAlias(final Class c)
	{
		String typeName = this.classMap.get(c);
		if (typeName == null)
		{
			typeName = c.getName();
		}
		return typeName;
	}

	@Override
	public Class<?> getClassFromAlias(final String alias)
	{
		final Class cls = this.classMap.inverse().get(alias);
		if (cls == null)
		{
			try
			{
				return Class.forName(alias);
			} catch (final ClassNotFoundException e)
			{
				return null;
			}
		}
		return cls;
	}

	public <I> TypeSerializer registerClasses(
			final Supplier<String> nameSupplier,
			final Supplier<Short> idSupplier,
			final Collection<Class<? extends I>> implTypes)
	{
		implTypes.forEach(c -> {
			this.registerClass(c, nameSupplier.get(),
					idSupplier.get());
		});
		return this;
	}

	public <I> TypeSerializer registerClasses(
			final Class<I> interfaceType,
			final Supplier<String> nameSupplier,
			final Supplier<Short> idSupplier,
			final Collection<Class<? extends I>> implTypes)
	{
		implTypes.forEach(c -> {
			this.registerClass(interfaceType, c, nameSupplier.get(),
					idSupplier.get());
		});
		return this;
	}

	public <I, C extends I> TypeSerializer registerClass(
			final Class<I> interfaceType,
			final Class<C> implType,
			final String name, final short id)
	{
		this.interfaces.add(interfaceType);
		this.classMap.put(implType, name);
		this.idMap.put(implType, id);
		return this;
	}

	public <I, C extends I> TypeSerializer registerClass(
			final Class<C> implType,
			final String name, final short id)
	{
		this.classMap.put(implType, name);
		this.idMap.put(implType, id);
		System.err.println(implType + " - " + name + " - " + id);

		return this;
	}

	public <I, C extends I> TypeSerializer registerType(
			final Class<I> itf)
	{
		this.interfaces.add(itf);
		return this;
	}

	@Override
	public Class<?> getClassForElement(final JsonElement readElement)
	{
		final String typeName = readElement.getAsJsonObject().get(TYPE_KEY)
				.getAsString();
		return this.getClassFromAlias(typeName);
	}

	@Override
	public void postDeserialize(final Object result, final JsonElement src,
			final Gson gson)
	{

	}

	@Override
	public void postSerialize(final JsonElement result, final Object src,
			final Gson gson)
	{
		String typeName = this.classMap.get(src.getClass());
		if (typeName == null)
		{
			typeName = src.getClass().getName();
		}
		final Short typeId = this.idMap.get(src.getClass());
		if (typeId != null)
		{
			result.getAsJsonObject().addProperty(TYPE_ID_KEY, typeId);
		}
		result.getAsJsonObject().addProperty(TYPE_KEY, typeName);

	}

}
