//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.01.15 at 09:44:39 PM CST 
//


package com.amdocs.dashboard.mobile.assets;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AssetTypeAttribute.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="AssetTypeAttribute">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="image"/>
 *     &lt;enumeration value="json"/>
 *     &lt;enumeration value="xml"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "AssetTypeAttribute")
@XmlEnum
public enum AssetTypeAttribute {

    @XmlEnumValue("image")
    IMAGE("image"),
    @XmlEnumValue("json")
    JSON("json"),
    @XmlEnumValue("xml")
    XML("xml");
    private final String value;

    AssetTypeAttribute(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AssetTypeAttribute fromValue(String v) {
        for (AssetTypeAttribute c: AssetTypeAttribute.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}