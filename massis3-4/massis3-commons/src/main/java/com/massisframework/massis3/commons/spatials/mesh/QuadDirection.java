package com.massisframework.massis3.commons.spatials.mesh;

import com.jme3.math.Vector3f;

/**
 * @formatter:off
 * @author rpax
 *
 */
public enum QuadDirection {

	Z_POS(  new float[]{-0.5f,-0.5f,0.0f,0.5f,-0.5f,0.0f,0.5f,0.5f,0.0f,-0.5f,0.5f,0.0f,},
			new float[]{+0.0f,+0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,}),
	
	Z_NEG(new float[]{0.5f,-0.5f,0f,-0.5f,-0.5f,0f,-0.5f,0.5f,0f,0.5f,0.5f,0f,},new float[]{0f,0.0f,-1.0f,0f,0.0f,-1.0f,0f,0.0f,-1.0f,0f,0.0f,-1.0f}),
	X_POS(new float[]{0.0f,-0.5f,0.5f,0.0f,-0.5f,-0.5f,0.0f,0.5f,-0.5f,0.0f,0.5f,0.5f,},new float[]{1f,0.0f,0.0f,1f,0.0f,0.0f,1f,0.0f,0.0f,1f,0.0f,0.0f}),
	X_NEG(new float[]{0.0f,-0.5f,-0.5f,0.0f,-0.5f,0.5f,0.0f,0.5f,0.5f,0.0f,0.5f,-0.5f,},new float[]{-1f,0.0f,0.0f,-1f,0.0f,0.0f,-1f,0.0f,0.0f,-1f,0.0f,0.0f}),
	Y_NEG(new float[]{-0.5f,0.0f,-0.5f,0.5f,0.0f,-0.5f,0.5f,0.0f,0.5f,-0.5f,0.0f,0.5f,},new float[]{0.0f,-1f,0.0f,0.0f,-1f,0.0f,0.0f,-1f,0.0f,0.0f,-1f,0.0f}),
	Y_POS(new float[] { -0.5f, 0.0f, 0.5f, 0.5f, 0.0f, 0.5f, 0.5f, 0.0f, -0.5f, -0.5f, 0.0f, -0.5f, },
			new float[] { 0.0f, 1f, 0.0f, 0.0f, 1f, 0.0f, 0.0f, 1f, 0.0f, 0.0f, 1f,0.0f});

	private float[] points;
	private float[] normals;

	// public static float[] X_NEG_POINTS=
	QuadDirection(float[] points, float[] normals) {
		this.points = points;
		this.normals = normals;
	}
	
	public float[] getPoints() {
		return this.points;
	}
	public float[] getNormals() {
		return this.normals;
	}
	
	public Vector3f getNormal(int n,Vector3f store) {
	return store.set(this.normals[n*3], this.normals[n*3+1], this.normals[n*3+2]);
	}
	public Vector3f getPoint(int n,Vector3f store) {
	return store.set(this.points[n*3], this.points[n*3+1], this.points[n*3+2]);
	}
}