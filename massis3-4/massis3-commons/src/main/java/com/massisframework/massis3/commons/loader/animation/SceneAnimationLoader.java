package com.massisframework.massis3.commons.loader.animation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jme3.animation.Animation;
import com.jme3.animation.BoneTrack;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import com.massisframework.massis3.commons.loader.animation.json.JsonAnimTrack;

public class SceneAnimationLoader implements AssetLoader {

	// TODO find generic way
	private static final String[] boneNames = {
			"", "Hips", "LHipJoint", "LeftUpLeg", "LeftLeg", "LeftFoot",
			"LeftToeBase", "LowerBack", "Spine", "Spine1", "LeftShoulder",
			"LeftArm", "LeftForeArm", "LeftHand", "LThumb", "LeftFingerBase",
			"LeftHandFinger1", "Neck", "Neck1", "Head", "RightShoulder",
			"RightArm", "RightForeArm", "RightHand", "RThumb",
			"RightFingerBase", "RightHandFinger1", "RHipJoint", "RightUpLeg",
			"RightLeg", "RightFoot", "RightToeBase"

	};

	@Override
	public Object load(final AssetInfo assetInfo) throws IOException
	{
		try (InputStream is = assetInfo.openStream())
		{
			final JsonObject obj = new Gson()
					.fromJson(new InputStreamReader(is), JsonObject.class);

			final JsonAnimTrack[] tracks = new Gson()
					.fromJson(obj.get("animationData"), JsonAnimTrack[].class);
			final String aName = new File(assetInfo.getKey().getName())
					.getName();
			final float maxTime = Arrays.stream(tracks)
					.map(JsonAnimTrack::getTimes)
					.map(t -> t[t.length - 1])
					.min(Float::compare)
					.orElse(0f);
			final Animation anim = new Animation(aName, maxTime);
			for (final JsonAnimTrack t : tracks)
			{
				final String boneName = t.getBoneName();
				// int boneIndex = sp.getControl(SkeletonControl.class)
				// .getSkeleton().getBoneIndex(boneName);
				int boneIndex = -1;
				for (int i = 0; i < boneNames.length; i++)
				{
					if (boneNames[i].equals(boneName))
					{
						boneIndex = i;
						break;
					}
				}

				if (boneIndex < 0)
				{
					continue;
				}
				final BoneTrack bt = new BoneTrack(boneIndex,
						t.getTimes(), t.getTranslations(), t.getRotations());
				anim.addTrack(bt);
			}
			return anim;
		}

	}

}
