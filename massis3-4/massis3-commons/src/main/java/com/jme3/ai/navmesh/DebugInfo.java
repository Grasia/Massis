/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.ai.navmesh;

import java.util.ArrayList;
import java.util.List;

import com.jme3.ai.navmesh.Path.Waypoint;
import com.jme3.math.Vector3f;

/**
 * Debug nformation from a pathfinding search.
 * 
 * @author sploreg
 */
public class DebugInfo {
	private final List<Vector3f> preOptWaypoints = new ArrayList<Vector3f>();
	private final List<Cell> plannedCells = new ArrayList<Cell>();
	private List<Vector3f> wpPositions;
	private Vector3f startPos, endPos;
	private Waypoint failedVisibleWaypoint;
	private Waypoint farthestTestedWaypoint;
	private ICell failedCell;
	private final List<Cell> passedCells = new ArrayList<Cell>();
	private ICell endingCell;
	private Vector3f startLocation;

	public void reset()
	{
		if (preOptWaypoints != null)
			preOptWaypoints.clear();
		if (plannedCells != null)
			plannedCells.clear();
		if (wpPositions != null)
			wpPositions.clear();
		startPos = null;
		endPos = null;
		failedVisibleWaypoint = null;
		farthestTestedWaypoint = null;
		failedCell = null;
		if (passedCells != null)
			passedCells.clear();
		endingCell = null;
		startLocation = null;
	}

	public void setWaypointPositions(final List<Vector3f> wpPositions)
	{
		this.wpPositions = wpPositions;
	}

	public List<Vector3f> getWpPositions()
	{
		return wpPositions;
	}

	public Vector3f getEndPos()
	{
		return endPos;
	}

	public void setEndPos(final Vector3f endPos)
	{
		this.endPos = endPos;
	}

	public Vector3f getStartPos()
	{
		return startPos;
	}

	public void setStartPos(final Vector3f startPos)
	{
		this.startPos = startPos;
	}

	public void setFailedVisibleWaypoint(final Waypoint testPoint)
	{
		this.failedVisibleWaypoint = testPoint;
	}

	public Waypoint getFailedVisibleWaypoint()
	{
		return failedVisibleWaypoint;
	}

	public void setFarthestTestedWaypoint(final Waypoint farthest)
	{
		this.farthestTestedWaypoint = farthest;
	}

	public Waypoint getFarthestTestedWaypoint()
	{
		return farthestTestedWaypoint;
	}

	public void setFailedCell(final ICell failed)
	{
		this.failedCell = failed;
	}

	public void addPassedCell(final Cell passed)
	{
		this.passedCells.add(passed);
	}

	public void setEndingCell(final ICell ending)
	{
		this.endingCell = ending;
	}

	public ICell getEndingCell()
	{
		return endingCell;
	}

	public ICell getFailedCell()
	{
		return failedCell;
	}

	public List<Cell> getPassedCells()
	{
		return passedCells;
	}

	public void setStartLocation(final Vector3f loc)
	{
		this.startLocation = loc;
	}

	public Vector3f getStartLocation()
	{
		return startLocation;
	}

	void addPlannedCell(final Cell cell)
	{
		plannedCells.add(cell);
	}

	public List<Cell> getPlannedCells()
	{
		return plannedCells;
	}

	void addPreOptWaypoints(final Vector3f wp)
	{
		preOptWaypoints.add(wp);
	}

	public List<Vector3f> getPreOptWaypoints()
	{
		return preOptWaypoints;
	}

}
