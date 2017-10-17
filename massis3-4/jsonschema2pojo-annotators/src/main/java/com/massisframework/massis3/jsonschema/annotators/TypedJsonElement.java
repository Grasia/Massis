package com.massisframework.massis3.jsonschema.annotators;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__type")
public interface TypedJsonElement {

}
