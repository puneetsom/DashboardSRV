//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.05.30 at 03:53:34 PM CDT 
//


package com.amdocs.dashboard.mobile.report;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MetadataTypeAttr.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="MetadataTypeAttr">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="default"/>
 *     &lt;enumeration value="grid"/>
 *     &lt;enumeration value="chart"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MetadataTypeAttr")
@XmlEnum
public enum MetadataTypeAttr {

    @XmlEnumValue("default")
    DEFAULT("default"),
    @XmlEnumValue("grid")
    GRID("grid"),
    @XmlEnumValue("chart")
    CHART("chart");
    private final String value;

    MetadataTypeAttr(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MetadataTypeAttr fromValue(String v) {
        for (MetadataTypeAttr c: MetadataTypeAttr.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}