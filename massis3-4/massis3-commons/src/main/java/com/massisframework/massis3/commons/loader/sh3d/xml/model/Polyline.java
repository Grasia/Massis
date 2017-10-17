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
import javax.xml.bind.annotation.XmlIDREF;
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
				"property",
				"point"
		})
@XmlRootElement(name = "polyline")
public class Polyline {

	@XmlAttribute(name = "level")
	@XmlIDREF
	protected Object level;
	@XmlAttribute(name = "thickness")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String thickness;
	@XmlAttribute(name = "capStyle")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String capStyle;
	@XmlAttribute(name = "joinStyle")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String joinStyle;
	@XmlAttribute(name = "dashStyle")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String dashStyle;
	@XmlAttribute(name = "startArrowStyle")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String startArrowStyle;
	@XmlAttribute(name = "endArrowStyle")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String endArrowStyle;
	@XmlAttribute(name = "color")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String color;
	@XmlAttribute(name = "closedPath")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String closedPath;
	protected List<Property> property;
	@XmlElement(required = true)
	protected List<Point> point;

	/**
	 * Gets the value of the level property.
	 * 
	 * @return possible object is {@link Object }
	 * 
	 */
	public Object getLevel()
	{
		return level;
	}

	/**
	 * Sets the value of the level property.
	 * 
	 * @param value
	 *            allowed object is {@link Object }
	 * 
	 */
	public void setLevel(final Object value)
	{
		this.level = value;
	}

	/**
	 * Gets the value of the thickness property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getThickness()
	{
		if (thickness == null)
		{
			return "1";
		} else
		{
			return thickness;
		}
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
	 * Gets the value of the capStyle property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCapStyle()
	{
		if (capStyle == null)
		{
			return "BUTT";
		} else
		{
			return capStyle;
		}
	}

	/**
	 * Sets the value of the capStyle property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCapStyle(final String value)
	{
		this.capStyle = value;
	}

	/**
	 * Gets the value of the joinStyle property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getJoinStyle()
	{
		if (joinStyle == null)
		{
			return "MITER";
		} else
		{
			return joinStyle;
		}
	}

	/**
	 * Sets the value of the joinStyle property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setJoinStyle(final String value)
	{
		this.joinStyle = value;
	}

	/**
	 * Gets the value of the dashStyle property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDashStyle()
	{
		if (dashStyle == null)
		{
			return "SOLID";
		} else
		{
			return dashStyle;
		}
	}

	/**
	 * Sets the value of the dashStyle property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDashStyle(final String value)
	{
		this.dashStyle = value;
	}

	/**
	 * Gets the value of the startArrowStyle property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getStartArrowStyle()
	{
		if (startArrowStyle == null)
		{
			return "NONE";
		} else
		{
			return startArrowStyle;
		}
	}

	/**
	 * Sets the value of the startArrowStyle property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setStartArrowStyle(final String value)
	{
		this.startArrowStyle = value;
	}

	/**
	 * Gets the value of the endArrowStyle property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getEndArrowStyle()
	{
		if (endArrowStyle == null)
		{
			return "NONE";
		} else
		{
			return endArrowStyle;
		}
	}

	/**
	 * Sets the value of the endArrowStyle property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setEndArrowStyle(final String value)
	{
		this.endArrowStyle = value;
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
	 * Gets the value of the closedPath property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getClosedPath()
	{
		if (closedPath == null)
		{
			return "false";
		} else
		{
			return closedPath;
		}
	}

	/**
	 * Sets the value of the closedPath property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setClosedPath(final String value)
	{
		this.closedPath = value;
	}

	/**
	 * Gets the value of the property property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the property property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getProperty().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Property
	 * }
	 * 
	 * 
	 */
	public List<Property> getProperty()
	{
		if (property == null)
		{
			property = new ArrayList<Property>();
		}
		return this.property;
	}

	/**
	 * Gets the value of the point property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the point property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getPoint().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Point }
	 * 
	 * 
	 */
	public List<Point> getPoint()
	{
		if (point == null)
		{
			point = new ArrayList<Point>();
		}
		return this.point;
	}
}
