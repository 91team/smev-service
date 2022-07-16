//
// This file was generated by the Eclipse Implementation of JAXB, v3.0.1 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2022.07.15 at 07:37:50 PM MSK 
//


package com.nineone.smev.schemas;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PackageAction.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="PackageAction"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="SmevRequest"/&gt;
 *     &lt;enumeration value="Ping"/&gt;
 *     &lt;enumeration value="OK"/&gt;
 *     &lt;enumeration value="ERROR"/&gt;
 *     &lt;enumeration value="MessageSent"/&gt;
 *     &lt;enumeration value="MessageProcessing"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "PackageAction")
@XmlEnum
public enum PackageAction {

    @XmlEnumValue("SmevRequest")
    SMEV_REQUEST("SmevRequest"),
    @XmlEnumValue("Ping")
    PING("Ping"),
    OK("OK"),
    ERROR("ERROR"),
    @XmlEnumValue("MessageSent")
    MESSAGE_SENT("MessageSent"),
    @XmlEnumValue("MessageProcessing")
    MESSAGE_PROCESSING("MessageProcessing");
    private final String value;

    PackageAction(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PackageAction fromValue(String v) {
        for (PackageAction c: PackageAction.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}