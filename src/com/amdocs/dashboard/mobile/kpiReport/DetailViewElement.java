//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.03.14 at 02:00:09 PM CDT 
//


package com.amdocs.dashboard.mobile.kpiReport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DetailViewElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DetailViewElement">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.amdocs.com/dashboard/mobile/report}KpiReportElement">
 *       &lt;attribute name="excludedLevels" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DetailViewElement")
public class DetailViewElement
    extends KpiReportElement
{

    @XmlAttribute
    protected String excludedLevels;

    /**
     * Gets the value of the excludedLevels property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExcludedLevels() {
        return excludedLevels;
    }

    /**
     * Sets the value of the excludedLevels property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExcludedLevels(String value) {
        this.excludedLevels = value;
    }

    public boolean isSetExcludedLevels() {
        return (this.excludedLevels!= null);
    }

}
