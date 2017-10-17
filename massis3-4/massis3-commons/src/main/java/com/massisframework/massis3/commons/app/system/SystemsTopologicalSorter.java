package com.massisframework.massis3.commons.app.system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemsTopologicalSorter {
	private static final Logger log = LoggerFactory.getLogger(SystemsTopologicalSorter.class);

	public static List<Object> sort(
			Collection<?> states)
	{
		Collection<Class> stateClasses = states.stream()
				.map(s -> s.getClass())
				.collect(Collectors.toList());
		List<Class> sortedByClass = sortByClass(stateClasses);
		List<Object> sorted = new ArrayList<>();
		for (Class sType : sortedByClass)
		{

			Object state = states
					.stream()
					.filter(sType::isInstance)
					.findAny().orElseThrow(() -> new java.util.NoSuchElementException(
							"Missing required MassisState: " + sType.getName()));
			sorted.add(state);
		}
		return sorted;
	}

	public static List<Class> sortByClass(
			Class... states)
	{
		return sortByClass(Arrays.asList(states));
	}

	public static List<Class> sortByClass(
			Collection<Class> states)
	{
		/**
		 * @formatter:off
			L ← Empty list that will contain the sorted elements
			S ← Set of all nodes with no incoming edges
			while S is non-empty do
			    remove a node n from S
			    add n to tail of L
			    for each node m with an edge e from n to m do
			        remove edge e from the graph
			        if m has no other incoming edges then
			            insert m into S
			if graph has edges then
			    return error (graph has at least one cycle)
			else 
			    return L (a topologically sorted order)
		 */
		/**
		 * @formatter:on
		 */
		Map<Class, List<Class>> inputs = new HashMap<>();
		states.forEach(s -> inputs.put(s, new ArrayList<>()));

		states.stream().map(s -> getRequired(s)).flatMap(r -> r.stream())
				.forEach(s -> inputs.put(s, new ArrayList<>()));

		for (Class stateType : states)
		{
			for (Class req : getRequired(stateType))
			{
				List<Class> inList = inputs.get(req);
				inList.add(stateType);
			}
		}
		/**
		 * L ← Empty list that will contain the sorted elements
		 */
		List<Class> L = new ArrayList<>();
		/**
		 * S ← Set of all nodes with no incoming edges
		 */
		Queue<Class> S = new LinkedList<>();
		inputs.entrySet()
				.stream()
				.filter(e -> e.getValue().isEmpty())
				.map(e -> e.getKey())
				.forEach(S::add);
		// while S is non-empty do
		while (!S.isEmpty())
		{
			// remove a node n from S
			Class n = S.poll();
			// add n to tail of L
			L.add(n);
			// for each node m with an edge e from n to m do
			getRequired(n).forEach(m -> {
				inputs.get(m).remove(n);
				if (inputs.get(m).isEmpty())
				{
					S.add(m);
				}
			});
			// if m has no other incoming edges then insert m into S

		}
		List<Class> cyclicDependencyStates = inputs
				.entrySet()
				.stream()
				.filter(e -> !e.getValue().isEmpty())
				.map(e -> e.getKey())
				.collect(Collectors.toList());
		if (!cyclicDependencyStates.isEmpty() && log.isWarnEnabled())
		{
			StringBuilder cyclicStatesSb = new StringBuilder();
			cyclicDependencyStates.forEach(s -> {
				cyclicStatesSb.append(s.getName() + "\n");
			});
			log.warn("Ciclic dependency in MassisState requirements! initialization might fail."
					+ " Involved MassisStates: \n" + cyclicStatesSb);

		}
		// System.out.println(inputs);
		states.stream().filter(s -> !L.contains(s)).forEach(s -> L.add(0, s));
		L.forEach(c -> {
			printRequired(c);
		});
		Collections.reverse(L);
		return L;
	}

	private static void printRequired(Class state)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(state.getSimpleName()).append(" : ");
		sb.append("[");
		for (Class req : getRequired(state))
		{
			sb.append(req.getSimpleName()).append(",");
		}
		if (sb.charAt(sb.length() - 1) == ',')
		{
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("]");
		System.out.println(sb);
	}

	private static List<Class> getRequired(Class state)
	{
		RequiresSystems ann = (RequiresSystems) state.getAnnotation(RequiresSystems.class);
		if (ann == null)
			return Collections.emptyList();
		return Arrays.asList(ann.value());
	}

}
