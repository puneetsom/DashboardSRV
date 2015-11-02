//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.01.30 at 12:51:18 AM CST 
//


package com.amdocs.dashboard.mobile.xyChart;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for chartType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="chartType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="line"/>
 *     &lt;enumeration value="area"/>
 *     &lt;enumeration value="clustered"/>
 *     &lt;enumeration value="stacked"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "chartType")
@XmlEnum
public enum ChartType {

    @XmlEnumValue("line")
    LINE("line"),
    @XmlEnumValue("area")
    AREA("area"),
    @XmlEnumValue("clustered")
    CLUSTERED("clustered"),
    @XmlEnumValue("stacked")
    STACKED("stacked");
    private final String value;

    ChartType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ChartType fromValue(String v) {
        for (ChartType c: ChartType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
