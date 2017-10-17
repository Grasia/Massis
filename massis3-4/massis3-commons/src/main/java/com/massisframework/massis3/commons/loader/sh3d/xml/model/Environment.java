//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.03.07 at 11:10:50 AM CET 
//

package com.massisframework.massis3.commons.loader.sh3d.xml.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
		name = "",
		propOrder = {
				"cameraOrObserverCameraOrTexture"
		})
@XmlRootElement(name = "environment")
public class Environment {

	@XmlAttribute(name = "groundColor")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String groundColor;
	@XmlAttribute(name = "skyColor")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String skyColor;
	@XmlAttribute(name = "lightColor")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String lightColor;
	@XmlAttribute(name = "wallsAlpha")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String wallsAlpha;
	@XmlAttribute(name = "allLevelsVisible")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String allLevelsVisible;
	@XmlAttribute(name = "observerCameraElevationAdjusted")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String observerCameraElevationAdjusted;
	@XmlAttribute(name = "ceillingLightColor")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String ceillingLightColor;
	@XmlAttribute(name = "drawingMode")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String drawingMode;
	@XmlAttribute(name = "subpartSizeUnderLight")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String subpartSizeUnderLight;
	@XmlAttribute(name = "photoWidth")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String photoWidth;
	@XmlAttribute(name = "photoHeight")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String photoHeight;
	@XmlAttribute(name = "photoAspectRatio")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String photoAspectRatio;
	@XmlAttribute(name = "photoQuality")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String photoQuality;
	@XmlAttribute(name = "videoWidth")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String videoWidth;
	@XmlAttribute(name = "videoAspectRatio")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String videoAspectRatio;
	@XmlAttribute(name = "videoQuality")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String videoQuality;
	@XmlAttribute(name = "videoFrameRate")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String videoFrameRate;
	@XmlElements({
			@XmlElement(name = "camera", type = Camera.class),
			@XmlElement(name = "observerCamera", type = ObserverCamera.class),
			@XmlElement(name = "texture", type = TextureXML.class)
	})
	protected List<Object> cameraOrObserverCameraOrTexture;

	/**
	 * Gets the value of the groundColor property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getGroundColor()
	{
		return groundColor;
	}

	/**
	 * Sets the value of the groundColor property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setGroundColor(final String value)
	{
		this.groundColor = value;
	}

	/**
	 * Gets the value of the skyColor property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSkyColor()
	{
		return skyColor;
	}

	/**
	 * Sets the value of the skyColor property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSkyColor(final String value)
	{
		this.skyColor = value;
	}

	/**
	 * Gets the value of the lightColor property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLightColor()
	{
		return lightColor;
	}

	/**
	 * Sets the value of the lightColor property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLightColor(final String value)
	{
		this.lightColor = value;
	}

	/**
	 * Gets the value of the wallsAlpha property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getWallsAlpha()
	{
		if (wallsAlpha == null)
		{
			return "0";
		} else
		{
			return wallsAlpha;
		}
	}

	/**
	 * Sets the value of the wallsAlpha property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setWallsAlpha(final String value)
	{
		this.wallsAlpha = value;
	}

	/**
	 * Gets the value of the allLevelsVisible property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getAllLevelsVisible()
	{
		if (allLevelsVisible == null)
		{
			return "false";
		} else
		{
			return allLevelsVisible;
		}
	}

	/**
	 * Sets the value of the allLevelsVisible property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setAllLevelsVisible(final String value)
	{
		this.allLevelsVisible = value;
	}

	/**
	 * Gets the value of the observerCameraElevationAdjusted property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getObserverCameraElevationAdjusted()
	{
		if (observerCameraElevationAdjusted == null)
		{
			return "true";
		} else
		{
			return observerCameraElevationAdjusted;
		}
	}

	/**
	 * Sets the value of the observerCameraElevationAdjusted property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setObserverCameraElevationAdjusted(final String value)
	{
		this.observerCameraElevationAdjusted = value;
	}

	/**
	 * Gets the value of the ceillingLightColor property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCeillingLightColor()
	{
		return ceillingLightColor;
	}

	/**
	 * Sets the value of the ceillingLightColor property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCeillingLightColor(final String value)
	{
		this.ceillingLightColor = value;
	}

	/**
	 * Gets the value of the drawingMode property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDrawingMode()
	{
		if (drawingMode == null)
		{
			return "FILL";
		} else
		{
			return drawingMode;
		}
	}

	/**
	 * Sets the value of the drawingMode property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDrawingMode(final String value)
	{
		this.drawingMode = value;
	}

	/**
	 * Gets the value of the subpartSizeUnderLight property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSubpartSizeUnderLight()
	{
		if (subpartSizeUnderLight == null)
		{
			return "0";
		} else
		{
			return subpartSizeUnderLight;
		}
	}

	/**
	 * Sets the value of the subpartSizeUnderLight property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSubpartSizeUnderLight(final String value)
	{
		this.subpartSizeUnderLight = value;
	}

	/**
	 * Gets the value of the photoWidth property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPhotoWidth()
	{
		if (photoWidth == null)
		{
			return "400";
		} else
		{
			return photoWidth;
		}
	}

	/**
	 * Sets the value of the photoWidth property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPhotoWidth(final String value)
	{
		this.photoWidth = value;
	}

	/**
	 * Gets the value of the photoHeight property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPhotoHeight()
	{
		if (photoHeight == null)
		{
			return "300";
		} else
		{
			return photoHeight;
		}
	}

	/**
	 * Sets the value of the photoHeight property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPhotoHeight(final String value)
	{
		this.photoHeight = value;
	}

	/**
	 * Gets the value of the photoAspectRatio property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPhotoAspectRatio()
	{
		if (photoAspectRatio == null)
		{
			return "VIEW_3D_RATIO";
		} else
		{
			return photoAspectRatio;
		}
	}

	/**
	 * Sets the value of the photoAspectRatio property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPhotoAspectRatio(final String value)
	{
		this.photoAspectRatio = value;
	}

	/**
	 * Gets the value of the photoQuality property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPhotoQuality()
	{
		if (photoQuality == null)
		{
			return "0";
		} else
		{
			return photoQuality;
		}
	}

	/**
	 * Sets the value of the photoQuality property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPhotoQuality(final String value)
	{
		this.photoQuality = value;
	}

	/**
	 * Gets the value of the videoWidth property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getVideoWidth()
	{
		if (videoWidth == null)
		{
			return "320";
		} else
		{
			return videoWidth;
		}
	}

	/**
	 * Sets the value of the videoWidth property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setVideoWidth(final String value)
	{
		this.videoWidth = value;
	}

	/**
	 * Gets the value of the videoAspectRatio property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getVideoAspectRatio()
	{
		if (videoAspectRatio == null)
		{
			return "RATIO_4_3";
		} else
		{
			return videoAspectRatio;
		}
	}

	/**
	 * Sets the value of the videoAspectRatio property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setVideoAspectRatio(final String value)
	{
		this.videoAspectRatio = value;
	}

	/**
	 * Gets the value of the videoQuality property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getVideoQuality()
	{
		if (videoQuality == null)
		{
			return "0";
		} else
		{
			return videoQuality;
		}
	}

	/**
	 * Sets the value of the videoQuality property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setVideoQuality(final String value)
	{
		this.videoQuality = value;
	}

	/**
	 * Gets the value of the videoFrameRate property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getVideoFrameRate()
	{
		if (videoFrameRate == null)
		{
			return "25";
		} else
		{
			return videoFrameRate;
		}
	}

	/**
	 * Sets the value of the videoFrameRate property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setVideoFrameRate(final String value)
	{
		this.videoFrameRate = value;
	}

	/**
	 * Gets the value of the cameraOrObserverCameraOrTexture property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the cameraOrObserverCameraOrTexture property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getCameraOrObserverCameraOrTexture().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Camera }
	 * {@link ObserverCamera } {@link TextureXML }
	 * 
	 * 
	 */
	public List<Object> getCameraOrObserverCameraOrTexture()
	{
		if (cameraOrObserverCameraOrTexture == null)
		{
			cameraOrObserverCameraOrTexture = new ArrayList<Object>();
		}
		return this.cameraOrObserverCameraOrTexture;
	}

}