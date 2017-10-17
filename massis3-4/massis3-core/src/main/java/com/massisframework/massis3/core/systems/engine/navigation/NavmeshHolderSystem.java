/**
 * 
 */
package com.massisframework.massis3.core.systems.engine.navigation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.ai.navmesh.ICell;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.app.system.RequiresSystems;
import com.massisframework.massis3.commons.pathfinding.UniformGridGraph;
import com.massisframework.massis3.commons.pathfinding.navmesh.FindPathResult;
import com.massisframework.massis3.commons.pathfinding.navmesh.NavigationMeshProcessor;
import com.massisframework.massis3.commons.pathfinding.navmesh.TriangleNavigationMeshProcessor;
import com.massisframework.massis3.core.systems.required.SceneLoaderSystem;

import io.netty.util.internal.ThreadLocalRandom;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * @author rpax
 *
 */
@RequiresSystems({
		SceneLoaderSystem.class,
})
public class NavmeshHolderSystem extends AbstractMassisSystem {

	private static final Logger log = LoggerFactory.getLogger(NavmeshHolderSystem.class);
	private Mesh rawmesh;
	// FIXME change with impl
	private NavigationMeshProcessor triNavMesh;
	private UniformGridGraph ug;

	@Override
	protected void simpleInitialize()
	{
	}

	@Override
	protected void onDisable()
	{

	}

	@Override
	protected void onEnable()
	{

	}

	public Vector3f getCellCenter(int cellId, Vector3f store)
	{
		return store.set(this.getTriNavMesh().getCellCenter(cellId));
	}

	public Vector3f getCellCenter(int cellId)
	{
		return this.getCellCenter(cellId, new Vector3f());
	}

	@Override
	public void update()
	{

	}

	private static ThreadLocal<List<Integer>> getRandomCellReachableFrom_cells_TL = ThreadLocal
			.withInitial(IntArrayList::new);

	public int getRandomCellReachableFrom(int cellId)
	{
		ICell cell = null;
		FindPathResult fpr = null;
		List<Integer> cells = getRandomCellReachableFrom_cells_TL.get();
		do
		{
			int target = ThreadLocalRandom.current()
					.nextInt(this.getTriNavMesh().getNumCells());
			cell = getTriNavMesh().getCell(target);
			cells.clear();

			fpr = getTriNavMesh().findPath(cellId, target, 4, cells);
		} while (!fpr.isSuccess());
		return cell.getIndex();
	}

	public UniformGridGraph getUniformGridGraph()
	{
		if (this.ug == null)
		{
			this.ug = this.getState(SceneLoaderSystem.class).loadUniformGridGraph();
		}
		return this.ug;
	}

	public Mesh getRawmesh()
	{
		if (rawmesh == null)
		{

			if (log.isInfoEnabled())
			{
				log.info("Raw mesh is not loaded. Loading rawmesh");
			}

			this.rawmesh = getState(SceneLoaderSystem.class).loadRawNavMesh();
		}
		return rawmesh;
	}

	public TriangleNavigationMeshProcessor getTriNavMesh()
	{
		if (this.triNavMesh == null)
		{

			if (log.isInfoEnabled())
			{
				log.info("Triangle navmesh not loaded. Loading triangle navmesh");
			}

			this.triNavMesh = getState(SceneLoaderSystem.class).loadTriangleNavMeshProcessor();
		}
		return (TriangleNavigationMeshProcessor) triNavMesh;
	}

}
