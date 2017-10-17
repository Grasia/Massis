package com.massisframework.massis3.commons.raycast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.objects.PhysicsGhostObject;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.util.TempVars;
import com.massisframework.massis3.commons.spatials.Spatials;

public class NativeRayCaster implements SceneRayCaster {

	static
	{
		Spatials.ensureNativeLibsLoaded();
	}

	private Collection<CollisionShape> collisionShapes;
	private Semaphore physicsSpaceSem;
	private boolean released;
	private PhysicsSpace physicsSpace;
	private static final Logger logger = LoggerFactory
			.getLogger(NativeRayCaster.class);

	public NativeRayCaster()
	{
		this.released = false;
		this.physicsSpaceSem = new Semaphore(1);
		this.collisionShapes = new ArrayList<>();
		this.physicsSpace = new PhysicsSpace();
	}

	/* (non-Javadoc)
	 * @see com.massisframework.massis3.commons.raycast.SceneRayCaster#addCollisionMesh(com.jme3.scene.Mesh)
	 */
	@Override
	public void addCollisionMesh(Mesh m)
	{
		this.addCollisionShape(new MeshCollisionShape(m));
	}

	/* (non-Javadoc)
	 * @see com.massisframework.massis3.commons.raycast.SceneRayCaster#addCollisionShapes(java.lang.Iterable)
	 */
	@Override
	public void addCollisionShapes(Iterable<CollisionShape> cshapes)
	{
		try
		{
			this.physicsSpaceSem.acquire();
			cshapes.forEach(cs -> {
				this.collisionShapes.add(cs);
				this.physicsSpace.add(new PhysicsGhostObject(cs));
			});

		} catch (InterruptedException e)
		{
			logger.error("Interrupted while adquiring", e);
		} finally
		{
			this.physicsSpaceSem.release();
		}

	}

	/* (non-Javadoc)
	 * @see com.massisframework.massis3.commons.raycast.SceneRayCaster#addCollisionShape(com.jme3.bullet.collision.shapes.CollisionShape)
	 */
	@Override
	public void addCollisionShape(CollisionShape cs)
	{
		try
		{
			this.physicsSpaceSem.acquire();
			this.collisionShapes.add(cs);
			this.physicsSpace.add(new PhysicsGhostObject(cs));
		} catch (InterruptedException e)
		{
			logger.error("Interrupted while adquiring", e);
		} finally
		{
			this.physicsSpaceSem.release();
		}

	}

	private static ThreadLocal<List<PhysicsRayTestResult>> nearestHit_TL = ThreadLocal
			.withInitial(ArrayList::new);

	/* (non-Javadoc)
	 * @see com.massisframework.massis3.commons.raycast.SceneRayCaster#rayCastNearestHit(com.jme3.math.Vector3f, com.jme3.math.Vector3f, com.jme3.math.Vector3f)
	 */
	@Override
	public boolean rayCastNearestHit(
			Vector3f from,
			Vector3f to,
			Vector3f store)
	{

		if (this.released)
		{
			throw new IllegalStateException("Physics space was destroyed");
		}
		List<PhysicsRayTestResult> results = nearestHit_TL.get();
		results.clear();
		this.physicsSpace.rayTest(from, to, results);
		float minDistance = Float.MAX_VALUE;
		store.zero();
		Vector3f nearest = store;
		TempVars tmp = TempVars.get();
		boolean hasHit = !results.isEmpty();
		for (PhysicsRayTestResult res : results)
		{
			// PhysicsCollisionObject co = res.getCollisionObject();
			Vector3f rayHit = to.subtract(from, tmp.vect1)
					.multLocal(res.getHitFraction());
			float distance = rayHit.length();
			if (distance < minDistance)
			{
				Vector3f contactPoint = rayHit.addLocal(from);
				minDistance = distance;
				nearest.set(contactPoint);
			}

		}
		tmp.release();
		return hasHit;
	}

	/* (non-Javadoc)
	 * @see com.massisframework.massis3.commons.raycast.SceneRayCaster#cleanup()
	 */
	@Override
	public void cleanup()
	{
		if (logger.isInfoEnabled())
		{
			logger.info("Destroying physics space");
		}
		this.released = true;
		this.physicsSpace.destroy();
		this.physicsSpace = null;

	}

}
