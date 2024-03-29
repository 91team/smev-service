//
// This file was generated by the Eclipse Implementation of JAXB, v3.0.1 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2022.08.11 at 05:54:00 PM MSK 
//


package com.nineone.smev.schemas;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PackageType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="PackageType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="Request"/&gt;
 *     &lt;enumeration value="Reply"/&gt;
 *     &lt;enumeration value="Notify"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "PackageType")
@XmlEnum
public enum PackageType {

    @XmlEnumValue("Request")
    REQUEST("Request"),
    @XmlEnumValue("Reply")
    REPLY("Reply"),
    @XmlEnumValue("Notify")
    NOTIFY("Notify");
    private final String value;

    PackageType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PackageType fromValue(String v) {
        for (PackageType c: PackageType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
