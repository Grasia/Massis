package com.massisframework.massis3.simulation.ecs;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.simsilica.es.EntityComponent;

@Retention(RUNTIME)
@Target(TYPE)
public @interface TracksComponents {

	public Class<? extends EntityComponent>[] value();
}
