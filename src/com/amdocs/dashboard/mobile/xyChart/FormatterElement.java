//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.02.04 at 11:07:31 AM CST 
//


package com.amdocs.dashboard.mobile.xyChart;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for formatterElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="formatterElement">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="type" type="{http://www.amdocs.com/dashboard/mobile/XYChart}fieldType" />
 *       &lt;attribute name="maxFractionalDigits" type="{http://www.w3.org/2001/XMLSchema}byte" />
 *       &lt;attribute name="abbreviationThreshold" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="abbreviationFractionalDigits" type="{http://www.w3.org/2001/XMLSchema}byte" default="1" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "formatterElement")
public class FormatterElement {

    @XmlAttribute
    protected String id;
    @XmlAttribute
    protected FieldType type;
    @XmlAttribute
    protected Byte maxFractionalDigits;
    @XmlAttribute
    protected Long abbreviationThreshold;
    @XmlAttribute
    protected Byte abbreviationFractionalDigits;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    public boolean isSetId() {
        return (this.id!= null);
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link FieldType }
     *     
     */
    public FieldType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link FieldType }
     *     
     */
    public void setType(FieldType value) {
        this.type = value;
    }

    public boolean isSetType() {
        return (this.type!= null);
    }

    /**
     * Gets the value of the maxFractionalDigits property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public byte getMaxFractionalDigits() {
        return maxFractionalDigits;
    }

    /**
     * Sets the value of the maxFractionalDigits property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setMaxFractionalDigits(byte value) {
        this.maxFractionalDigits = value;
    }

    public boolean isSetMaxFractionalDigits() {
        return (this.maxFractionalDigits!= null);
    }

    public void unsetMaxFractionalDigits() {
        this.maxFractionalDigits = null;
    }

    /**
     * Gets the value of the abbreviationThreshold property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getAbbreviationThreshold() {
        return abbreviationThreshold;
    }

    /**
     * Sets the value of the abbreviationThreshold property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setAbbreviationThreshold(long value) {
        this.abbreviationThreshold = value;
    }

    public boolean isSetAbbreviationThreshold() {
        return (this.abbreviationThreshold!= null);
    }

    public void unsetAbbreviationThreshold() {
        this.abbreviationThreshold = null;
    }

    /**
     * Gets the value of the abbreviationFractionalDigits property.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public byte getAbbreviationFractionalDigits() {
        if (abbreviationFractionalDigits == null) {
            return ((byte) 1);
        } else {
            return abbreviationFractionalDigits;
        }
    }

    /**
     * Sets the value of the abbreviationFractionalDigits property.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setAbbreviationFractionalDigits(byte value) {
        this.abbreviationFractionalDigits = value;
    }

    public boolean isSetAbbreviationFractionalDigits() {
        return (this.abbreviationFractionalDigits!= null);
    }

    public void unsetAbbreviationFractionalDigits() {
        this.abbreviationFractionalDigits = null;
    }

}
