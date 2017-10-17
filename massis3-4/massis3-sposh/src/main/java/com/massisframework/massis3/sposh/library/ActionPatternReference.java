package com.massisframework.massis3.sposh.library;

import cz.cuni.amis.pogamut.sposh.engine.VariableContext;
import cz.cuni.amis.pogamut.sposh.executor.ActionResult;
import cz.cuni.amis.pogamut.sposh.executor.IAction;
import cz.cuni.amis.pogamut.sposh.executor.Param;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;

/**
 * Action LapReference for Yaposh.
 *
 * @author rpax
 * @param <CONTEXT>
 *            Context class of the action. It's an shared object used by all
 *            primitives. it is used as a shared memory and for interaction with
 *            the environment.
 */
@PrimitiveInfo(name = "Action Pattern", description = "Reference to an action pattern")
public class ActionPatternReference<Context> implements IAction {

	@Override
	public void init(VariableContext params)
	{
		throw new UnsupportedOperationException("This class is for in-editor use only");
	}

	public void init(@Param("$file") String filename,@Param("$name") String name)
	{
		throw new UnsupportedOperationException("This class is for in-editor use only");
	}

	@Override
	public ActionResult run(VariableContext params)
	{
		throw new UnsupportedOperationException("This class is for in-editor use only");
	}

	@Override
	public void done(VariableContext params)
	{
		throw new UnsupportedOperationException("This class is for in-editor use only");
	}


}
