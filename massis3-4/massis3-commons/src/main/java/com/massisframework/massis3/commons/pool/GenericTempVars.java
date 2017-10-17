/*
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.massisframework.massis3.commons.pool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Based on {@link com.jme3.util.TempVars}
 */
public class GenericTempVars {

	private static Map<Class, ThreadLocal<TempVarsStack<? extends GenericTempVar>>> tmpMap = new ConcurrentHashMap<>();

	/**
	 * Allow X instances of TempVars in a single thread.
	 */
	private static final int STACK_SIZE = 5;

	/**
	 * <code>TempVarsStack</code> contains a stack of TempVars. Every time
	 * TempVars.get() is called, a new entry is added to the stack, and the
	 * index incremented. When TempVars.release() is called, the entry is
	 * checked against the current instance and then the index is decremented.
	 */
	private static class TempVarsStack<I> implements ReleaseAble {

		int index = 0;
		GenericTempVar[] tempVars = new GenericTempVar[STACK_SIZE];

		@Override
		public void release()
		{
			// Return it to the stack
			this.index--;

			// // Check if it is actually there
			// if (this.tempVars[this.index] != var)
			// {
			// throw new IllegalStateException(
			// "An instance of TempVars has not been released in a called
			// method!");
			// }
		}
	}

	/**
	 * This instance of TempVars has been retrieved but not released yet.
	 */
	private final boolean isUsed = false;

	private GenericTempVars()
	{
	}

	/**
	 * Acquire an instance of the TempVar class. You have to release the
	 * instance after use by calling the release() method. If more than
	 * STACK_SIZE (currently 5) instances are requested in a single thread then
	 * an ArrayIndexOutOfBoundsException will be thrown.
	 * 
	 * @return A TempVar instance
	 */
	public static <T extends GenericTempVar> T get(final Class<T> type)
	{
		ThreadLocal<TempVarsStack<? extends GenericTempVar>> tL = tmpMap
				.get(type);
		if (tL == null)
		{
			tL = ThreadLocal.withInitial(TempVarsStack::new);
			tmpMap.put(type, tL);
		}
		final TempVarsStack<T> stack = (TempVarsStack<T>) tL.get();

		GenericTempVar instance = stack.tempVars[stack.index];

		if (instance == null)
		{
			// Create new
			instance = newInstance(type, stack);

			// Put it in there
			stack.tempVars[stack.index] = instance;
		}

		stack.index++;

		// instance.isUsed = true;

		return (T) instance;
	}

	private static <T extends GenericTempVar> T newInstance(final Class<T> type,
			final TempVarsStack t)
	{
		try
		{
			return type.getConstructor(ReleaseAble.class).newInstance(t);
		} catch (final Exception e)
		{
			throw new RuntimeException();
		}
	}

}
