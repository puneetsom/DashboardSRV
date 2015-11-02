//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.04.15 at 09:02:26 PM CDT 
//


package com.amdocs.dashboard.mobile.xyChart;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for XYChartElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="XYChartElement">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Service" type="{http://www.amdocs.com/dashboard/mobile/XYChart}ServiceElement"/>
 *         &lt;element name="seriesSet" type="{http://www.amdocs.com/dashboard/mobile/XYChart}seriesSetElement"/>
 *         &lt;element name="axisSet" type="{http://www.amdocs.com/dashboard/mobile/XYChart}axisSetElement"/>
 *         &lt;element name="formatterSet" type="{http://www.amdocs.com/dashboard/mobile/XYChart}formatterSetElement" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="locale" type="{http://www.w3.org/2001/XMLSchema}string" default="en_US" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="height" type="{http://www.w3.org/2001/XMLSchema}short" />
 *       &lt;attribute name="width" type="{http://www.w3.org/2001/XMLSchema}short" />
 *       &lt;attribute name="sliderField" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "XYChartElement", propOrder = {
    "service",
    "seriesSet",
    "axisSet",
    "formatterSet"
})
public class XYChartElement {

    @XmlElement(name = "Service", required = true)
    protected ServiceElement service;
    @XmlElement(required = true)
    protected SeriesSetElement seriesSet;
    @XmlElement(required = true)
    protected AxisSetElement axisSet;
    protected FormatterSetElement formatterSet;
    @XmlAttribute
    protected String locale;
    @XmlAttribute
    protected String name;
    @XmlAttribute
    protected Short height;
    @XmlAttribute
    protected Short width;
    @XmlAttribute
    protected String sliderField;

    /**
     * Gets the value of the service property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceElement }
     *     
     */
    public ServiceElement getService() {
        return service;
    }

    /**
     * Sets the value of the service property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceElement }
     *     
     */
    public void setService(ServiceElement value) {
        this.service = value;
    }

    public boolean isSetService() {
        return (this.service!= null);
    }

    /**
     * Gets the value of the seriesSet property.
     * 
     * @return
     *     possible object is
     *     {@link SeriesSetElement }
     *     
     */
    public SeriesSetElement getSeriesSet() {
        return seriesSet;
    }

    /**
     * Sets the value of the seriesSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link SeriesSetElement }
     *     
     */
    public void setSeriesSet(SeriesSetElement value) {
        this.seriesSet = value;
    }

    public boolean isSetSeriesSet() {
        return (this.seriesSet!= null);
    }

    /**
     * Gets the value of the axisSet property.
     * 
     * @return
     *     possible object is
     *     {@link AxisSetElement }
     *     
     */
    public AxisSetElement getAxisSet() {
        return axisSet;
    }

    /**
     * Sets the value of the axisSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link AxisSetElement }
     *     
     */
    public void setAxisSet(AxisSetElement value) {
        this.axisSet = value;
    }

    public boolean isSetAxisSet() {
        return (this.axisSet!= null);
    }

    /**
     * Gets the value of the formatterSet property.
     * 
     * @return
     *     possible object is
     *     {@link FormatterSetElement }
     *     
     */
    public FormatterSetElement getFormatterSet() {
        return formatterSet;
    }

    /**
     * Sets the value of the formatterSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link FormatterSetElement }
     *     
     */
    public void setFormatterSet(FormatterSetElement value) {
        this.formatterSet = value;
    }

    public boolean isSetFormatterSet() {
        return (this.formatterSet!= null);
    }

    /**
     * Gets the value of the locale property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocale() {
        if (locale == null) {
            return "en_US";
        } else {
            return locale;
        }
    }

    /**
     * Sets the value of the locale property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocale(String value) {
        this.locale = value;
    }

    public boolean isSetLocale() {
        return (this.locale!= null);
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    public boolean isSetName() {
        return (this.name!= null);
    }

    /**
     * Gets the value of the height property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public short getHeight() {
        return height;
    }

    /**
     * Sets the value of the height property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setHeight(short value) {
        this.height = value;
    }

    public boolean isSetHeight() {
        return (this.height!= null);
    }

    public void unsetHeight() {
        this.height = null;
    }

    /**
     * Gets the value of the width property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public short getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setWidth(short value) {
        this.width = value;
    }

    public boolean isSetWidth() {
        return (this.width!= null);
    }

    public void unsetWidth() {
        this.width = null;
    }

    /**
     * Gets the value of the sliderField property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSliderField() {
        return sliderField;
    }

    /**
     * Sets the value of the sliderField property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSliderField(String value) {
        this.sliderField = value;
    }

    public boolean isSetSliderField() {
        return (this.sliderField!= null);
    }

}