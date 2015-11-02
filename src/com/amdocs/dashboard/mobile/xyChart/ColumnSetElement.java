//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.01.31 at 10:22:21 AM CST 
//


package com.amdocs.dashboard.mobile.xyChart;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for columnSetElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="columnSetElement">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="columnSeries" type="{http://www.amdocs.com/dashboard/mobile/XYChart}columnSeriesElement" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" type="{http://www.amdocs.com/dashboard/mobile/XYChart}chartType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "columnSetElement", propOrder = {
    "columnSeries"
})
public class ColumnSetElement {

    protected List<ColumnSeriesElement> columnSeries;
    @XmlAttribute
    protected ChartType type;

    /**
     * Gets the value of the columnSeries property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the columnSeries property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getColumnSeries().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ColumnSeriesElement }
     * 
     * 
     */
    public List<ColumnSeriesElement> getColumnSeries() {
        if (columnSeries == null) {
            columnSeries = new ArrayList<ColumnSeriesElement>();
        }
        return this.columnSeries;
    }

    public boolean isSetColumnSeries() {
        return ((this.columnSeries!= null)&&(!this.columnSeries.isEmpty()));
    }

    public void unsetColumnSeries() {
        this.columnSeries = null;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link ChartType }
     *     
     */
    public ChartType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChartType }
     *     
     */
    public void setType(ChartType value) {
        this.type = value;
    }

    public boolean isSetType() {
        return (this.type!= null);
    }

}