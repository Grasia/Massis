package com.massisframework.massis3.commons.spatials;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.jme3.app.LegacyApplication;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Triangle;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.control.Control;
import com.jme3.scene.mesh.IndexBuffer;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.JmeContext;
import com.jme3.util.BufferUtils;
import com.jme3.util.TempVars;
import com.massisframework.massis3.commons.app.EmptyApplication;
import com.massisframework.massis3.commons.spatials.mesh.Circle3dMesh;

public class Spatials {

	/**
	 * @formatter:off
	 */
	public static final String MASSIS_FOLDER_NAME = ".massis";
	public static final Path MASSIS_ASSET_PATH = Paths.get(System.getProperty("user.home"), MASSIS_FOLDER_NAME, "assets");
	//
	public static  final Box			UNIT_BOX		= new Box(1, 1, 1);
	public static  final Sphere			SIMPLE_SPHERE	= new Sphere(16, 16, 1);
	private static final Line			LINE_Y_INSTANCE = new Line(Vector3f.ZERO,Vector3f.UNIT_Y);
	private static final Circle3dMesh	SIMPLE_CIRCLE	= new Circle3dMesh(1);
	/**
	 * @formatter:on
	 */
	private static AtomicBoolean nativeLibsLoaded = new AtomicBoolean(false);
	static
	{
		ensureNativeLibsLoaded();
	}

	public static void ensureNativeLibsLoaded()
	{
		//TODO lock instead of atomic boolean
		if (!nativeLibsLoaded.getAndSet(true))
		{
			final Logger appLogger = Logger
					.getLogger(LegacyApplication.class.getName());
			final Level oldLvl = appLogger.getLevel();
			appLogger.setLevel(Level.OFF);
			final SimpleApplication app = new EmptyApplication(Collections.emptyList());
			app.start(JmeContext.Type.Headless, true);
			app.stop(true);
			appLogger.setLevel(oldLvl);
		}
	}

	public static void rawScale(final Spatial structure, final float scale)
	{
		structure.depthFirstTraversal(new SceneGraphVisitorAdapter() {
			@Override
			public void visit(final Geometry geom)
			{
				final Mesh mesh = geom.getMesh();
				final VertexBuffer vtbuf = mesh
						.getBuffer(VertexBuffer.Type.Position);

				final FloatBuffer fbuff = (FloatBuffer) vtbuf.getData();

				for (int i = 0; i < fbuff.limit(); i++)
				{
					fbuff.put(i, fbuff.get(i) * scale);
				}
				vtbuf.updateData(fbuff);
				mesh.updateCounts();
				mesh.updateBound();
			}
		});
	}

	public static <T extends Control> T getFirstControl(final Spatial spatial,
			final Class<T> type)
	{
		return Spatials.stream(spatial)
				.map(s -> s.getControl(type))
				.filter(c -> c != null)
				.findFirst().orElse(null);
	}

	public static <T extends Control> Iterable<T> getAllControls(
			final Spatial spatial,
			final Class<T> type)
	{
		return Spatials.stream(spatial)
				.map(s -> s.getControl(type))
				.filter(c -> c != null)::iterator;
	}

	public static Iterable<Control> getControls(final Spatial sp)
	{
		final int size = sp.getNumControls();
		final Function<Integer, Control> genFunction = (i) -> sp.getControl(i);
		return new Iterable<Control>() {

			@Override
			public Iterator<Control> iterator()
			{
				return new CtrlIterator<>(size, genFunction);
			}
		};
	}

	public static void forEachMesh(final Spatial sp,
			final Consumer<Mesh> action)
	{
		forEachGeometry(sp, Geometry::getMesh);
	}

	public static Stream<Geometry> streamGeometries(final Spatial sp)
	{
		return Spatials.stream(sp)
				.filter(Geometry.class::isInstance)
				.map(Geometry.class::cast);
	}

	public static Stream<Spatial> stream(final Spatial sp)
	{
		return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(new SpatialIterator(sp), 0),
				false);
	}

	public static Iterable<Spatial> transverse(final Spatial sp)
	{
		return new Iterable<Spatial>() {

			@Override
			public Iterator<Spatial> iterator()
			{
				return new SpatialIterator(sp);
			}

		};
	}

	public static void forEachGeometry(final Spatial sp,
			final Consumer<Geometry> action)
	{
		sp.depthFirstTraversal(new SceneGraphVisitorAdapter() {
			@Override
			public void visit(final Geometry geom)
			{
				action.accept(geom);
			}
		});
	}

	public static class SpatialIterator implements Iterator<Spatial> {

		private final Queue<Spatial> wk;

		public SpatialIterator(final Spatial sp)
		{
			this.wk = new LinkedList<>();
			this.wk.add(sp);
		}

		@Override
		public boolean hasNext()
		{
			return !this.wk.isEmpty();
		}

		@Override
		public Spatial next()
		{
			final Spatial next = this.wk.poll();
			if (next == null)
			{
				throw new NoSuchElementException();
			}
			if (next instanceof Node)
			{
				((Node) next).getChildren().forEach(this.wk::add);
			}
			return next;
		}

	}

	public static void forEachGeom(final Spatial sp,
			final Consumer<Geometry> action)
	{
		if (sp instanceof Node)
		{
			sp.depthFirstTraversal(s -> {
				if (s instanceof Geometry)
				{
					action.accept((Geometry) s);
				}
			});
		} else
		{
			action.accept((Geometry) sp);
		}
	}

	public static List<Geometry> gatherGeoms(final Spatial sp,
			final List<Geometry> store)
	{
		return gatherGeoms(sp, Function.identity(), store);
	}

	public static <T> List<T> gatherGeoms(final Spatial sp,
			final Function<Geometry, T> transform, final List<T> store)
	{
		store.clear();
		forEachGeom(sp, g -> {
			store.add(transform.apply(g));
		});
		return store;
	}

	public static void rawScale(final Mesh mesh, final float scale)
	{

		rawScale(mesh, scale, scale, scale);

	}

	public static void rawScale(final Mesh mesh, final Vector3f scale)
	{
		rawScale(mesh, scale.x, scale.y, scale.z);
	}

	public static Vector3f[] cloneVectorArray(final Vector3f... array)
	{
		final Vector3f[] res = new Vector3f[array.length];
		for (int i = 0; i < array.length; i++)
		{
			res[i] = new Vector3f(array[i]);
		}
		return res;
	}

	public static Vector2f[] cloneVectorArray(final Vector2f... array)
	{
		final Vector2f[] res = new Vector2f[array.length];
		for (int i = 0; i < array.length; i++)
		{
			res[i] = new Vector2f(array[i]);
		}
		return res;
	}

	public static Vector3f[] createVector3Array(final int size)
	{
		final Vector3f[] res = new Vector3f[size];
		for (int i = 0; i < size; i++)
		{
			res[i] = new Vector3f();
		}
		return res;
	}

	public static Vector2f[] createVector2Array(final int size)
	{
		final Vector2f[] res = new Vector2f[size];
		for (int i = 0; i < size; i++)
		{
			res[i] = new Vector2f();
		}
		return res;
	}

	public static void rawScale(final Mesh mesh, final float scaleX,
			final float scaleY,
			final float scaleZ)
	{

		final FloatBuffer fbuff = mesh
				.getFloatBuffer(VertexBuffer.Type.Position);

		for (int i = 0; i < fbuff.limit(); i += 3)
		{
			fbuff.put(i, fbuff.get(i) * scaleX);
			fbuff.put(i + 1, fbuff.get(i + 1) * scaleY);
			fbuff.put(i + 2, fbuff.get(i + 2) * scaleZ);
		}
		mesh.setBuffer(VertexBuffer.Type.Position, 3, fbuff);
		mesh.updateCounts();
		mesh.updateBound();

	}

	public static void createNormals(final Spatial scene)
	{
		createNormals(scene, false);
	}

	public static Geometry createBox(final Vector3f center)
	{
		final Geometry geom = new Geometry("Box", UNIT_BOX);
		geom.setLocalTranslation(center);
		return geom;
	}

	public static Geometry createBox(final Vector3f center, final Material mat)
	{
		final Geometry geom = createBox(center);
		geom.setMaterial(mat);
		return geom;
	}

	public static Geometry createSphere(final Vector3f center)
	{
		final Geometry geom = new Geometry("Sphere", SIMPLE_SPHERE);
		geom.setLocalTranslation(center);
		return geom;
	}

	public static Geometry createCircle(final Vector3f center)
	{
		final Geometry geom = new Geometry("Circle", SIMPLE_CIRCLE);
		geom.setLocalTranslation(center);
		return geom;
	}

	public static float inscribedCircleRadius(final Triangle tri)
	{
		return inscribedCircleRadius(tri.get1(), tri.get2(), tri.get3());
	}

	public static float inscribedCircleRadius(final Vector3f p0,
			final Vector3f p1,
			final Vector3f p2)
	{
		final TempVars tmp = TempVars.get();
		final float a = p0.subtract(p1, tmp.vect1).length();
		final float b = p1.subtract(p2, tmp.vect2).length();
		final float c = p2.subtract(p0, tmp.vect3).length();
		tmp.release();
		return inscribedCircleRadius(a, b, c);
	}

	/**
	 * <pre>
	  https://web.archive.org/web/20161020104654/http://www.efunda.com/math/areas/circleinscribetrianglegen.cfm
	 * </pre>
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public static float inscribedCircleRadius(final float a, final float b,
			final float c)
	{
		final double k = 0.5D * (a + b + c);
		final double radius = Math.sqrt(k * (k - a) * (k - b) * (k - c)) / k;
		return (float) radius;
	}

	public static float inCircleRadius(final float a, final float b,
			final float c)
	{
		final double perimeter = a + b + c;
		final double s = perimeter / 2D;
		final double area = Math.sqrt(s * (s - a) * (s - b) * (s - c));
		return (float) (2 * area / perimeter);
	}

	public static Geometry createLine(final Vector3f a, final Vector3f b)
	{

		final TempVars tempVars = TempVars.get();
		final Vector3f abVect = tempVars.vect1;
		final Matrix3f rotate = tempVars.tempMat3;
		rotate.zero();
		b.subtract(a, abVect);
		final Geometry geom = new Geometry("Line", LINE_Y_INSTANCE);
		rotate.fromStartEndVectors(Vector3f.UNIT_Y, abVect.normalize());
		geom.setLocalRotation(rotate);
		geom.setLocalScale(1, abVect.length(), 1);
		tempVars.release();
		geom.setLocalTranslation(a);
		return geom;
	}

	public static Geometry modifyLineGeometry(final Vector3f a,
			final Vector3f b,
			final Spatial sp)
	{
		Objects.requireNonNull(sp);
		if (!(sp instanceof Geometry))
		{
			throw new IllegalArgumentException(
					"Spatial must be of type " + Geometry.class.getName());
		}
		final Geometry geom = (Geometry) sp;
		Objects.requireNonNull(geom.getMesh());
		if (!(geom.getMesh() instanceof Line))
		{
			throw new IllegalArgumentException(
					"Geometry must have a mesh of type " + Line.class.getName()
							+ ". Found " + geom.getMesh().getClass().getName());
		}

		final TempVars tempVars = TempVars.get();
		final Matrix3f rotate = tempVars.tempMat3;
		rotate.zero();

		final Vector3f abVect = tempVars.vect1;
		b.subtract(a, abVect);
		final Vector3f abVectNorm = tempVars.vect2;
		abVectNorm.set(abVect).normalizeLocal();

		rotate.fromStartEndVectors(Vector3f.UNIT_Y, abVectNorm);
		geom.setLocalRotation(rotate);
		geom.setLocalScale(1, abVect.length(), 1);
		tempVars.release();
		geom.setLocalTranslation(a);
		return geom;
	}

	public static void createNormals(final Spatial scene,
			final boolean overwrite)
	{
		Spatials.forEachMesh(scene, m -> createNormals3(m, overwrite));
	}

	/**
	 * <pre>
	Begin Function CalculateSurfaceNormal (Input Triangle) Returns Vector
	
	Set Vector U to (Triangle.p2 minus Triangle.p1)
	Set Vector V to (Triangle.p3 minus Triangle.p1)
	
	Set Normal.x to (multiply U.y by V.z) minus (multiply U.z by V.y)
	Set Normal.y to (multiply U.z by V.x) minus (multiply U.x by V.z)
	Set Normal.z to (multiply U.x by V.y) minus (multiply U.y by V.x)
	
	Returning Normal
	
	End Function
	 * </pre>
	 * 
	 * @param mesh
	 */
	public static void createNormals2(final Mesh mesh, final boolean overwrite)
	{
		if (mesh.getBuffer(VertexBuffer.Type.Normal) != null && !overwrite)
		{
			// Logger.getLogger(this.getClass().getName())
			// .info(() -> "Model has normals already");
			return;
		}
		mesh.updateCounts();
		mesh.updateBound();
		final int p1 = 0, p2 = 1, p3 = 2;
		final int triCount = mesh.getTriangleCount();
		final TempVars tmp = TempVars.get();

		final Vector3f[] tri = tmp.tri;
		final int[] indices = new int[3];
		final Vector3f U = tmp.vect1;
		final Vector3f V = tmp.vect2;
		final Vector3f Normal = tmp.vect3;

		FloatBuffer normalBuff = mesh.getFloatBuffer(Type.Normal);
		normalBuff = BufferUtils.ensureLargeEnough(normalBuff,
				mesh.getVertexCount() * 3);
		normalBuff.clear();
		for (int i = 0; i < triCount; i++)
		{
			mesh.getTriangle(i, indices);
			mesh.getTriangle(i, tri[p1], tri[p2], tri[p3]);

			// Set Vector U to (Triangle.p2 minus Triangle.p1)
			tri[p1].subtract(tri[p2], U);
			// Set Vector V to (Triangle.p3 minus Triangle.p1)
			tri[p3].subtract(tri[p1], V);
			// Set Normal.x to (multiply U.y by V.z) minus (multiply U.z by V.y)
			Normal.x = U.y * V.z - U.z * V.y;
			// Set Normal.y to (multiply U.z by V.x) minus (multiply U.x by V.z)
			Normal.y = U.z * V.x - U.x * V.z;
			// Normal.z to (multiply U.x by V.y) minus (multiply U.y by V.x)
			Normal.z = U.x * V.y - U.y * V.x;

			// Normal.normalizeLocal();
			BufferUtils.setInBuffer(Normal, normalBuff, indices[0]);
			BufferUtils.setInBuffer(Normal, normalBuff, indices[1]);
			BufferUtils.setInBuffer(Normal, normalBuff, indices[2]);

		}

		mesh.setBuffer(Type.Normal, 3, normalBuff);

		tmp.release();

	}

	private static boolean approximateEquals(final Vector3f a, final Vector3f b)
	{

		return FastMath.approximateEquals(a.x, b.x)
				&& FastMath.approximateEquals(a.y, b.y)
				&& FastMath.approximateEquals(a.z, b.z);
	}

	private static boolean usesVertex(final Triangle tri, final Vector3f a)
	{

		return approximateEquals(tri.get1(), a)
				|| approximateEquals(tri.get2(), a)
				|| approximateEquals(tri.get3(), a);
	}

	public static void createNormals3(final Mesh mesh, final boolean overwrite)
	{
		if (mesh.getBuffer(VertexBuffer.Type.Normal) != null && !overwrite)
		{
			// Logger.getLogger(this.getClass().getName())
			// .info(() -> "Model has normals already");
			return;
		}
		mesh.updateCounts();
		mesh.updateBound();
		final int p1 = 0, p2 = 1, p3 = 2;
		final int triCount = mesh.getTriangleCount();
		final TempVars tmp = TempVars.get();

		final Vector3f[] tri = tmp.tri;
		final int[] indices = new int[3];
		final Vector3f U = tmp.vect1;
		final Vector3f V = tmp.vect2;
		final Vector3f Normal = tmp.vect3;
		final int[] sharedCount = new int[mesh.getVertexCount()];

		FloatBuffer normalBuff = mesh.getFloatBuffer(Type.Normal);
		normalBuff = BufferUtils.ensureLargeEnough(normalBuff,
				mesh.getVertexCount() * 3);
		normalBuff.clear();

		for (int i = 0; i < triCount; i++)
		{
			mesh.getTriangle(i, indices);
			mesh.getTriangle(i, tri[p1], tri[p2], tri[p3]);

			// Set Vector U to (Triangle.p2 minus Triangle.p1)
			tri[p1].subtract(tri[p2], U);
			// Set Vector V to (Triangle.p3 minus Triangle.p1)
			tri[p3].subtract(tri[p1], V);
			// Set Normal.x to (multiply U.y by V.z) minus (multiply U.z by V.y)
			Normal.x = U.y * V.z - U.z * V.y;
			// Set Normal.y to (multiply U.z by V.x) minus (multiply U.x by V.z)
			Normal.y = U.z * V.x - U.x * V.z;
			// Normal.z to (multiply U.x by V.y) minus (multiply U.y by V.x)
			Normal.z = U.x * V.y - U.y * V.x;
			for (int j = 0; j < indices.length; j++)
			{
				sharedCount[indices[j]]++;
			}
			// Normal.normalizeLocal();
			BufferUtils.addInBuffer(Normal, normalBuff, indices[0]);
			BufferUtils.addInBuffer(Normal, normalBuff, indices[1]);
			BufferUtils.addInBuffer(Normal, normalBuff, indices[2]);

		}
		final Vector3f toMult = new Vector3f();
		for (int index = 0; index < sharedCount.length; index++)
		{
			final float count = sharedCount[index];
			if (count > 0)
			{
				toMult.set(1f / count, 1f / count, 1f / count);
				BufferUtils.multInBuffer(toMult, normalBuff, index);
			}
		}

		mesh.setBuffer(Type.Normal, 3, normalBuff);

		tmp.release();

	}

	public static Mesh createLineMesh(final List<Vector3f[]> segments)
	{
		final Mesh m = new Mesh();
		m.setMode(Mesh.Mode.Lines);

		final FloatBuffer pBuff = BufferUtils
				.createFloatBuffer(segments.size() * 3 * 2);
		final IntBuffer indexBuff = BufferUtils
				.createIntBuffer(segments.size() * 2);

		int currentIndex = 0;
		for (final Vector3f[] line : segments)
		{
			pBuff.put(line[0].x);
			pBuff.put(line[0].y);
			pBuff.put(line[0].z);

			pBuff.put(line[1].x);
			pBuff.put(line[1].y);
			pBuff.put(line[1].z);

			indexBuff.put(currentIndex++);
			indexBuff.put(currentIndex++);
		}

		m.setBuffer(Type.Position, 3, pBuff);
		m.setBuffer(Type.Index, 2, indexBuff);
		return m;
	}

	/**
	 * TODO must be optimized
	 * 
	 * @param mesh
	 */
	public static void createNormals(final Mesh mesh, final boolean overwrite)
	{
		if (mesh.getBuffer(VertexBuffer.Type.Normal) != null && !overwrite)
		{
			// Logger.getLogger(this.getClass().getName())
			// .info(() -> "Model has normals already");
			return;
		}
		final IndexBuffer indices = mesh.getIndicesAsList();
		final Vector3f[] vertices = BufferUtils.getVector3Array(
				mesh.getFloatBuffer(VertexBuffer.Type.Position));

		final List<Vector3f> normals = new ArrayList<>(mesh.getVertexCount());
		for (int i = 0; i < mesh.getVertexCount(); i++)
		{
			normals.add(new Vector3f(0, 0, 0));
		}
		// for (std::vector<int>::const_iterator i = indices.begin(); i !=
		// indices.end(); std::advance(i, 3))
		for (int i = 0; /* i < indices.size() */i != indices.size(); i += 3)
		{

			// Vector3 v[3] = { vertices[*i], vertices[*(i+1)], vertices[*(i+2)]
			// };
			final Vector3f[] v = {
					vertices[indices.get(i)],
					vertices[indices.get(i + 1)],
					vertices[indices.get(i + 2)]
			};
			// Vector3f normal = Vector3::cross(v[1] - v[0], v[2] - v[0]);
			final Vector3f normal = v[1].subtract(v[0])
					.cross(v[2].subtract(v[0]));

			for (int j = 0; j < 3; ++j)
			{
				final Vector3f a = v[(j + 1) % 3].subtract(v[j]);
				final Vector3f b = v[(j + 2) % 3].subtract(v[j]);
				final float weight = FastMath
						.acos(a.dot(b) / (a.length() * b.length()));
				normals.get(indices.get(i + j)).addLocal(normal.mult(weight));
			}
		}
		// std::for_each(normals.begin(), normals.end(),
		// std::mem_fun_ref(&Vector3::normalize));
		normals.forEach(Vector3f::normalizeLocal);
		mesh.setBuffer(VertexBuffer.Type.Normal, 3, BufferUtils
				.createFloatBuffer(normals.toArray(new Vector3f[0])));
		mesh.updateBound();
		mesh.updateCounts();
	}

	/**
	 * Based on {@link ArrayList}'s iterator
	 */
	private static class CtrlIterator<E> implements Iterator<E> {
		int cursor; // index of next element to return
		int lastRet = -1; // index of last element returned; -1 if no such
		private final int size;
		private final Function<Integer, E> genFunction;

		public CtrlIterator(final int size,
				final Function<Integer, E> genFunction)
		{
			this.size = size;
			this.genFunction = genFunction;
		}

		@Override
		public boolean hasNext()
		{
			return cursor != size;
		}

		@Override
		public E next()
		{
			final int i = cursor;
			if (i >= size)
				throw new NoSuchElementException();
			cursor = i + 1;
			return genFunction.apply(lastRet = i);
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		@SuppressWarnings("unchecked")
		public void forEachRemaining(final Consumer<? super E> consumer)
		{
			Objects.requireNonNull(consumer);
			int i = cursor;
			if (i >= size)
			{
				return;
			}

			while (i != size)
			{
				consumer.accept(this.genFunction.apply(i++));
			}
			// update once at end of iteration to reduce heap write traffic
			cursor = i;
			lastRet = i - 1;
		}
	}

}
