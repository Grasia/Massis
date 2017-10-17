//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.03.07 at 11:10:50 AM CET 
//

package com.massisframework.massis3.commons.loader.sh3d.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
				"texture"
		})
@XmlRootElement(name = "baseboard")
public class Baseboard {

	@XmlAttribute(name = "attribute", required = true)
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String attribute;
	@XmlAttribute(name = "thickness", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String thickness;
	@XmlAttribute(name = "height", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String height;
	@XmlAttribute(name = "color")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String color;
	protected TextureXML texture;

	/**
	 * Gets the value of the attribute property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getAttribute()
	{
		return attribute;
	}

	/**
	 * Sets the value of the attribute property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setAttribute(final String value)
	{
		this.attribute = value;
	}

	/**
	 * Gets the value of the thickness property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getThickness()
	{
		return thickness;
	}

	/**
	 * Sets the value of the thickness property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setThickness(final String value)
	{
		this.thickness = value;
	}

	/**
	 * Gets the value of the height property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getHeight()
	{
		return height;
	}

	/**
	 * Sets the value of the height property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setHeight(final String value)
	{
		this.height = value;
	}

	/**
	 * Gets the value of the color property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getColor()
	{
		return color;
	}

	/**
	 * Sets the value of the color property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setColor(final String value)
	{
		this.color = value;
	}

	/**
	 * Gets the value of the texture property.
	 * 
	 * @return possible object is {@link TextureXML }
	 * 
	 */
	public TextureXML getTexture()
	{
		return texture;
	}

	/**
	 * Sets the value of the texture property.
	 * 
	 * @param value
	 *            allowed object is {@link TextureXML }
	 * 
	 */
	public void setTexture(final TextureXML value)
	{
		this.texture = value;
	}

}