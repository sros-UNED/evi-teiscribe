package com.application.components.TEITextStruct;

import java.io.Serializable;

import org.apache.commons.lang3.StringEscapeUtils;

import static com.application.components.Constants.LABELATTRIBUTEPREFIX;

/**
 * This class stores an attribute of a label node.
 * 
 * @author Miguel Urízar Salinas
 * 
 * @see com.application.components.TEITextStruct.TextStruct
 * 
 */
public class TSAttribute implements Serializable {
    private static final long serialVersionUID = 7866402703494512071L;

    private String name;
    private String value;

    /**
     * Create new attribute
     * 
     * @param name
     *            Name of the attribute
     * @param value
     *            Value of the attribute
     */
    public TSAttribute(String name, String value) {
	this.name = name;
	this.value = value;
    }

    /**
     * Generates the inner HTML.
     * 
     * @return String with the inner HTML for showing the structure
     */
    public String GenerateInnerHTML() {
	return LABELATTRIBUTEPREFIX + StringEscapeUtils.escapeHtml4(name) + "=\"" + StringEscapeUtils.escapeHtml4(value) + "\" ";
    }

    /**
     * Generates the XML.
     * 
     * @return String with the inner XML for showing the structure
     */
    public String GenerateInnerXML() {
	return " " + StringEscapeUtils.escapeXml(name) + "=\"" + StringEscapeUtils.escapeXml(value) + "\"";
    }

    /**
     * @return Name of the attribute
     */
    public String GetName() {
	return name;
    }

    /**
     * @return Value of the attribute
     */
    public String GetValue() {
	return value;
    }

    /**
     * @param newName
     *            New name of the attribute
     */
    public void SetName(String newName) {
	name = newName;
    }

    /**
     * @param newValue
     *            New value of the attribute
     */
    public void SetValue(String newValue) {
	value = newValue;
    }
}
