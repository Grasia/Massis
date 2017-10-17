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
@XmlType(name = "")
@XmlRootElement(name = "textStyle")
public class TextStyle {

	@XmlAttribute(name = "attribute")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String attribute;
	@XmlAttribute(name = "fontName")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String fontName;
	@XmlAttribute(name = "fontSize", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String fontSize;
	@XmlAttribute(name = "bold")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String bold;
	@XmlAttribute(name = "italic")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String italic;

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
	 * Gets the value of the fontName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getFontName()
	{
		return fontName;
	}

	/**
	 * Sets the value of the fontName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setFontName(final String value)
	{
		this.fontName = value;
	}

	/**
	 * Gets the value of the fontSize property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getFontSize()
	{
		return fontSize;
	}

	/**
	 * Sets the value of the fontSize property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setFontSize(final String value)
	{
		this.fontSize = value;
	}

	/**
	 * Gets the value of the bold property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getBold()
	{
		if (bold == null)
		{
			return "false";
		} else
		{
			return bold;
		}
	}

	/**
	 * Sets the value of the bold property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setBold(final String value)
	{
		this.bold = value;
	}

	/**
	 * Gets the value of the italic property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getItalic()
	{
		if (italic == null)
		{
			return "false";
		} else
		{
			return italic;
		}
	}

	/**
	 * Sets the value of the italic property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setItalic(final String value)
	{
		this.italic = value;
	}

}
