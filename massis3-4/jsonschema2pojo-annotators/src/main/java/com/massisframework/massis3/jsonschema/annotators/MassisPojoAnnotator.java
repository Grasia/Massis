package com.massisframework.massis3.jsonschema.annotators;

import java.io.StringWriter;
import java.util.List;

import org.jsonschema2pojo.AbstractAnnotator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;

//import io.vertx.codegen.annotations.DataObject;
//import io.vertx.core.json.JsonObject;

public class MassisPojoAnnotator extends AbstractAnnotator {

	@Override
	public void propertyOrder(JDefinedClass clazz, JsonNode propertiesNode)
	{

		if (clazz.fields().size() == 0)
		{
			JMethod ctor = clazz.constructor(0);
			ctor.body()._return();
			ctor.mods().setPublic();
		}

		clazz._implements(TypedJsonElement.class);
		addDataObjectConstructor(clazz, propertiesNode);
		createToJsonMethod(clazz);

		//clazz.annotate(DataObject.class);
//		clazz.annotate(clazz.owner().ref("io.vertx.codegen.annotations.DataObject"));
		//clazz.annotate(Serializable.class);
		clazz.annotate(clazz.owner().ref("com.jme3.network.serializing.Serializable"));
		clazz.annotate(JsonTypeName.class).param("value", clazz.name());
		clazz.annotate(JsonIgnoreProperties.class).param("ignoreUnknown", true);
		clazz.annotate(JsonTypeInfo.class)
				.param("defaultImpl", clazz)
				.param("use", JExpr.direct("JsonTypeInfo.Id.NAME"))
				.param("property", "__type");

	}

	private void addDataObjectConstructor(JDefinedClass clazz,
			JsonNode propertiesNode)
	{
		JMethod ctor = clazz.constructor(1);
		//ctor.param(JsonObject.class, "json");
		ctor.param(clazz.owner().ref("io.vertx.core.json.JsonObject"), "json");
		// ctor.body().directStatement(fromJsonBody());
		JBlock body = ctor.body();
		clazz.fields().forEach((fName, f) -> {
			if ((f.mods().getValue() & JMod.STATIC) == JMod.STATIC)
			{
				// continue;
				return;
			}

			String jsonName = f.annotations().stream()
					.filter(ann -> ann.getAnnotationClass().name().equals("JsonProperty"))
					.map(ann -> ann.getAnnotationMembers())
					.map(members -> members.get("value"))
					.map(member -> {
						StringWriter sw = new StringWriter();
						member.generate(new JFormatter(sw));
						return sw.toString();
					}).findAny().get();

			JFieldRef thisField = JExpr.refthis(f.name());
			String invokeConvertValue = null;
			if (f.type().fullName().startsWith(List.class.getName()))
			{
				JClass typeParam = ((JClass) f.type()).getTypeParameters()
						.get(0);
				String typeName = typeParam.erasure().fullName() + "[]";
				invokeConvertValue = "java.util.Arrays.asList("
						+ invokeConvertValue(typeName, jsonName) + ")";

			} else
			{
				invokeConvertValue = invokeConvertValue(f.type().fullName(), jsonName);
			}
			JExpression convertValueInvocation = JExpr
					.direct(invokeConvertValue);

			body._if(ctor.params().get(0).invoke("containsKey").arg(JExpr.direct(jsonName)))._then()
					.assign(thisField, convertValueInvocation);

		});

	}

	private static String invokeConvertValue(String className, String jsonName)
	{
		return new StringBuilder()
				.append("io.vertx.core.json.Json.mapper.convertValue")
				.append("(json.getValue(").append(jsonName).append(")")
				.append(",").append(className + ".class")
				.append(")")
				.toString();
	}

	private void createToJsonMethod(JDefinedClass clazz)
	{
		JMethod method = clazz.method(1, clazz.owner().ref("io.vertx.core.json.JsonObject"), "toJson");
		method.body()._return(JExpr.direct("JsonObject.mapFrom(this)"));
	}

}
