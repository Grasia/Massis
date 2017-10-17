package com.massisframework.massis3.commons.app.system;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.massisframework.massis3.commons.app.server.MassisSystem;

@Retention(RUNTIME)
@Target(TYPE)
public @interface RequiresSystems {

	Class<? extends MassisSystem>[] value();
}
