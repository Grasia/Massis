package com.massisframework.massis3.core.systems.debug;

import java.util.HashMap;
import java.util.Map;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;
import com.massisframework.massis3.commons.app.system.AbstractMassisSystem;
import com.massisframework.massis3.commons.spatials.Materials;
import com.massisframework.massis3.core.systems.required.SceneLoaderSystem;

public class Text3DManagerSystem extends AbstractMassisSystem {

	private Map<Character, Spatial> characters;

	@Override
	protected void simpleInitialize()
	{
		this.characters = new HashMap<Character, Spatial>();

	}

	private Spatial loadChar(char c)
	{
		Spatial charSp = this.characters.get(c);
		if (charSp == null)
		{

			charSp = this.getState(SceneLoaderSystem.class).getAssetManager()
					.loadModel("Models/Text/alphabet/" + c + ".obj");

			this.characters.put(c, charSp);
		}
		return charSp.deepClone();
	}

	public Spatial loadText(String text)
	{
		return this.loadText(text, ColorRGBA.Yellow);
	}

	public Spatial loadText(String text, ColorRGBA color)
	{
		text = text.replace("\n", " ");
		char[] letters = text.toCharArray();
		Node textNodeLeftToRight = new Node();
		TempVars tmp = TempVars.get();
		float offsetX = 0;

		for (int i = 0; i < letters.length; i++)
		{
			Spatial sp = loadChar(letters[i]);
			sp.updateModelBound();
			BoundingBox bbox = (BoundingBox) sp.getWorldBound();
			float maxX = bbox.getMax(tmp.vect1).x;
			sp.setLocalRotation(tmp.quat1
					.fromAngles(new float[] { FastMath.PI / 2, 0, 0 }));
			sp.setLocalTranslation(offsetX, 0, 0);
			sp.setMaterial(Materials.newUnshaded(color, false));
			offsetX += maxX;
			textNodeLeftToRight.attachChild(sp);
		}
		Spatial textNodeRightToLeft = textNodeLeftToRight.deepClone();

		textNodeRightToLeft.move(offsetX, 0, 0);
		textNodeRightToLeft.setLocalRotation(
				tmp.quat2.fromAngleNormalAxis(FastMath.PI, Vector3f.UNIT_Y));
		tmp.release();
		Node textNode = new Node();

		textNodeLeftToRight
				.move(-((BoundingBox) textNodeLeftToRight.getWorldBound())
						.getXExtent(), 0, 0);
		textNodeRightToLeft
				.move(-((BoundingBox) textNodeRightToLeft.getWorldBound())
						.getXExtent(), 0, 0);

		textNode.attachChild(textNodeLeftToRight);
		textNode.attachChild(textNodeRightToLeft);
		return textNode;

	}

	@Override
	protected void onDisable()
	{

	}

	@Override
	protected void onEnable()
	{

	}

	@Override
	public void update()
	{

	}

	@Override
	public void graphicalUpdate(Node systemNode)
	{
		
	}
}
