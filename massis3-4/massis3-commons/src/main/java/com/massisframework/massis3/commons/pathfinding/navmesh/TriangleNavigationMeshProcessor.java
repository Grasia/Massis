package com.massisframework.massis3.commons.pathfinding.navmesh;

import java.util.ArrayList;
import java.util.List;

import com.jme3.ai.navmesh.ICell;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.massisframework.massis3.commons.loader.MassisSceneLoader;
import com.massisframework.massis3.commons.pathfinding.navmesh.impl.DefaultNavMeshFactory;
import com.massisframework.massis3.commons.pathfinding.navmesh.impl.DefaultNavigationMeshPathFinder;

public class TriangleNavigationMeshProcessor implements NavigationMeshProcessor {

	private NavigationMesh<ICell> navmesh;
	private NavigationMeshPathFinder<ICell> pf;
	private NavMeshCellFinder cellFinder;

	private MassisSceneLoader sceneLoader;

	public TriangleNavigationMeshProcessor(
			MassisSceneLoader sceneLoaderService)
	{
		this.sceneLoader = sceneLoaderService;
		this.loadNavMesh();
	}

	private NavigationMesh<ICell> loadNavMesh()
	{
		Mesh rawMesh = sceneLoader.loadRawNavMesh();
		DefaultNavMeshFactory nmf = new DefaultNavMeshFactory();
		this.navmesh = nmf.buildNavigationMesh(rawMesh);
		this.pf = new DefaultNavigationMeshPathFinder<>(this.navmesh);
		this.cellFinder = new NavMeshCellFinderImpl(this.navmesh);
		return this.navmesh;
	}

	@Override
	public Vector3f getWorldMax()
	{
		return ((BoundingBox) this.navmesh.getUnderlayingMesh().getBound())
				.getMax(new Vector3f());
	}

	@Override
	public Vector3f getWorldMin()
	{
		return ((BoundingBox) this.navmesh.getUnderlayingMesh().getBound())
				.getMin(new Vector3f());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.massisframework.massis3.simulation.states.navigation.
	 * NavigationMeshHolder#intersects(int, com.jme3.math.Vector3f, float)
	 */
	@Override
	public boolean intersects(int cellId, Vector3f point, float r)
	{
		return contains(cellId, point) || intersectsCircle(cellId, point, r);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.massisframework.massis3.simulation.states.navigation.
	 * NavigationMeshHolder#contains(int, com.jme3.math.Vector3f)
	 */
	@Override
	public boolean contains(int cellId, Vector3f point)
	{
		return this.navmesh.getCell(cellId).contains(point);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.massisframework.massis3.simulation.states.navigation.
	 * NavigationMeshHolder#intersectsCircle(int, com.jme3.math.Vector3f, float)
	 */
	@Override
	public boolean intersectsCircle(int cellId, Vector3f point, float r)
	{
		return this.navmesh.getCell(cellId).intersectsCircle(point, r);
	}

	private static ThreadLocal<List<ICell>> findPath_path_int_TL = ThreadLocal
			.withInitial(ArrayList::new);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.massisframework.massis3.simulation.states.navigation.
	 * NavigationMeshHolder#findPath(int, int, int, java.util.List)
	 */
	@Override
	public FindPathResult findPath(
			int start,
			int goal,
			int pathMaxLength,
			List<Integer> store)
	{
		ensureNavMeshLoaded();
		List<ICell> store1 = findPath_path_int_TL.get();
		store.clear();
		store1.clear();

		FindPathResult res = pf.findPath(
				this.navmesh.getCell(start),
				this.navmesh.getCell(goal),
				pathMaxLength, store1);

		store1.stream().map(ICell::getIndex).forEach(store::add);
		return res;

	}

	private int findNearestCell(Vector3f location, ICell lastChecked)
	{
		this.ensureNavMeshLoaded();
		ICell r = this.cellFinder
				.findNearestCell(location, lastChecked);
		if (r == null)
			// throw new RuntimeException("Nearest cell couldnt be located");
			return -1;
		return r.getIndex();
	}

	@Override
	public int findNearestCell(Vector3f location, int lastChecked)
	{
		this.ensureNavMeshLoaded();
		ICell lcc = lastChecked >= 0 ? navmesh.getCell(lastChecked) : null;
		return findNearestCell(location, lcc);
	}

	@Override
	public int findNearestCell(Vector3f location)
	{
		this.ensureNavMeshLoaded();
		return findNearestCell(location, null);
	}

	private void ensureNavMeshLoaded()
	{

	}

	public NavigationMesh<ICell> getNavMesh()
	{
		this.ensureNavMeshLoaded();
		return this.navmesh;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.massisframework.massis3.simulation.states.navigation.
	 * NavigationMeshHolder#snapPointToCell(com.jme3.math.Vector3f,
	 * java.lang.Integer, com.jme3.math.Vector3f)
	 */
	@Override
	public Vector3f snapPointToCell(Vector3f point, Integer cellId,
			Vector3f store)
	{
		this.ensureNavMeshLoaded();
		store.set(this.navmesh.getCell(cellId).snapPoint(point.clone()));
		return store;
	}

	public ICell getCell(int cellId)
	{
		return this.navmesh.getCell(cellId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.massisframework.massis3.simulation.states.navigation.
	 * NavigationMeshHolder#nearestObstaclePoint(com.jme3.math.Vector3f, int)
	 */
	@Override
	public Vector3f nearestObstaclePoint(Vector3f point, int cellIdHint)
	{
		this.ensureNavMeshLoaded();
		ICell cellHint = (cellIdHint >= 0) ? this.navmesh.getCell(cellIdHint)
				: null;
		return NavigationMeshUtil.nearestObstaclePoint(point, cellHint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.massisframework.massis3.simulation.states.navigation.
	 * NavigationMeshHolder#getCellCenter(int)
	 */
	@Override
	public Vector3f getCellCenter(int cellId)
	{
		this.ensureNavMeshLoaded();
		return this.navmesh.getCell(cellId).getCenter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.massisframework.massis3.simulation.states.navigation.
	 * NavigationMeshHolder#getTriangle(int)
	 */
	@Override
	public Vector3f[] getTriangle(int cellId)
	{
		this.ensureNavMeshLoaded();
		return this.navmesh.getCell(cellId).getTriangle();
	}

	@Override
	public boolean isInPath(Iterable<Integer> generatedPath, Vector3f position,
			float radius)
	{
		for (int cellId : generatedPath)
		{
			if (this.intersects(cellId, position, radius))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public float distanceToNearestCellNeighbor(int cellId)
	{
		// LinesUtils.distance2D(a, b)
		float minDist = Float.MAX_VALUE;
		ICell icell = this.navmesh.getCell(cellId);
		for (int i = 0; i < icell.getNumLinks(); i++)
		{
			Vector3f wmp = icell.getWallMidpoint(i);
			minDist = Math.min(icell.getCenter().distance(wmp), minDist);
		}
		return minDist;
	}

	private Vector3f midPointBetween(ICell a, ICell b)
	{
		for (int j = 0; j < 3; j++)
		{
			if (a.getLink(j) == b)
			{
				return a.getWallMidpoint(j);
			}
		}
		throw new RuntimeException();
	}

	private float angleBetween(Vector2f origin, int a, int b)
	{
		return angleBetween(origin, this.navmesh.getCell(a),
				this.navmesh.getCell(b));
	}

	private float angleBetween(Vector2f origin, ICell a, ICell b)
	{
		for (int j = 0; j < 3; j++)
		{
			if (a.getLink(j) == b)
			{
				Vector2f ptA = a.getWall(j).getPointA();
				Vector2f ptB = a.getWall(j).getPointB();
				// detect angle
				Vector2f p1 = ptA.clone().subtractLocal(origin)
						.normalizeLocal();
				Vector2f p2 = ptB.clone().subtractLocal(origin)
						.normalizeLocal();
				if (Vector2f.isValidVector(p1) && Vector2f.isValidVector(p2))
				{
					float angle = p1.angleBetween(p2);
					return angle;
				} else
				{
					return Float.NaN;
				}
			}
		}
		// throw new RuntimeException();
		return Float.NaN;
	}

	public List<Vector3f> funnel(
			Vector3f start,
			List<Integer> icellPath,
			Vector3f goal)
	{
		List<Vector3f> ret = new ArrayList<>();
		ret.add(start.clone());

		Vector2f origin = new Vector2f(start.x, start.z);
		int currentSegment = 0;
		for (int i = 0; i < icellPath.size(); i++)
		{
			if (this.navmesh.getCell(icellPath.get(i)).contains(origin))
			{
				currentSegment = i;
				break;
			}
		}
		if (currentSegment + 1 >= icellPath.size())
		{
			// ret.add(start.clone());
			return ret;
		}
		float minAngle = angleBetween(origin, icellPath.get(currentSegment),
				icellPath.get(currentSegment + 1));

		while (currentSegment < icellPath.size() - 1)
		{
			int multiplier = minAngle < 0 ? -1 : 1;
			minAngle = Float.MAX_VALUE;
			for (int i = currentSegment; i < icellPath.size() - 1; i++)
			{
				int a = icellPath.get(i);
				int b = icellPath.get(i + 1);
				float angle = angleBetween(origin, a, b);
				if (!Float.isNaN(angle) && angle * multiplier < minAngle*multiplier)
				{
					minAngle = angle;// * multiplier;
				} else
				{
					Vector3f mp = midPointBetween(
							this.navmesh.getCell(a),
							this.navmesh.getCell(b));
					// Vector3f mp2=this.navmesh.getCell(b).getCenter();
					origin.set(mp.x, mp.z);
					ret.add(mp.clone());

					break;
				}
				currentSegment = i + 1;

			}
		}
		ret.add(goal.clone());
		return ret;
	}

	@Override
	public Vector3f getRandomPointInCell(int cellId)
	{
		return this.navmesh.getCell(cellId).getRandomPoint();
	}

	@Override
	public Mesh getUnderlayingMesh()
	{
		return this.navmesh.getUnderlayingMesh();
	}

	@Override
	public int getNumCells()
	{
		return this.navmesh.getNumCells();
	}
}
