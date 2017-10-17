package com.massisframework.massis3.sposh.library;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import cz.cuni.amis.pogamut.sposh.elements.PoshParser;
import cz.cuni.amis.pogamut.sposh.elements.PoshPlan;

public class PlanParserUtils {

	private static final Map<URL, PoshPlan> POSH_PLANS = new HashMap<>();

	public static PoshPlan parsePlan(URL url)
	{
		synchronized (POSH_PLANS)
		{
			PoshPlan plan = POSH_PLANS.get(url);
			if (plan == null)
			{
				try (InputStream is = url.openStream())
				{
					PoshParser parser = new PoshParser(is);
					plan = parser.parsePlan();
				} catch (Exception e)
				{
					throw new RuntimeException(e);
				}
				POSH_PLANS.put(url, plan);
			}
			return plan;
		}
	}

}
