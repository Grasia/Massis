package com.massisframework.massis3.commons.collections;

import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class StreamUtil {

	@SafeVarargs
	public static <T> Stream<T> concat(Iterable<? extends T>... iterables)
	{

		Stream[] streams = Arrays
				.stream(iterables)
				.map(it -> it.spliterator())
				.map(si -> StreamSupport.stream(si, false))
				.toArray(s -> new Stream[s]);
		return concat(streams);

	}
	@SafeVarargs
	public static <T> Stream<T> concat(Stream<? extends T>... streams)
	{
		Stream<T> stream = null;
		if (streams.length == 0)
		{
			stream = Stream.empty();
		} else if (streams.length == 1)
		{
			stream = (Stream<T>) streams[0];
		} else
		{
			stream = Stream.concat(streams[0], streams[1]);
		}
		for (int i = 2; i < streams.length; i++)
		{
			stream = Stream.concat(stream, streams[i]);
		}
		return stream;
	}

}
