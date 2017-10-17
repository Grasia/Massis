package com.massisframework.massis3.commons.json;

public interface ClassAliasProvider {

	String getClassAlias(Class<?> c);

	Class<?> getClassFromAlias(String alias);

}
