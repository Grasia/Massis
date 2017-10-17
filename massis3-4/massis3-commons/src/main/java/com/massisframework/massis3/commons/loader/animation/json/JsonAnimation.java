package com.massisframework.massis3.commons.loader.animation.json;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JsonAnimation implements Cloneable {

	private AnimationHeader header;
	private List<AnimationFrame> frames;
	// footer?

	public JsonAnimation()
	{
		this.frames = new ArrayList<>();
	}

	public AnimationHeader getHeader()
	{
		return header;
	}

	public void setHeader(final AnimationHeader header)
	{
		this.header = header;
	}

	public List<AnimationFrame> getFrames()
	{
		return frames;
	}

	public void addFrame(final AnimationFrame f)
	{
		this.frames.add(f);
	}

	@Override
	protected JsonAnimation clone()
	{
		final JsonAnimation ja = new JsonAnimation();
		ja.header = header.clone();
		ja.frames = frames.stream().map(AnimationFrame::clone)
				.collect(Collectors.toList());
		return ja;
	}
}
