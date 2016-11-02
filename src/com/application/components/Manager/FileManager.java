package com.application.components.Manager;

import java.util.ArrayList;
import com.application.components.TEITextStruct.TextNode;

/**
 * Interface for working with different TEI label descriptor files such as DTD or RELAX NG
 * 
 * @author Miguel Urízar Salinas
 *
 */
public interface FileManager {
    /**
     * Returns a list of sub labels of a label, if there are no sub labels for this label, null is returned
     * 
     * @param name
     *            Name of the Label to extract the sub labels
     * @return Enumeration of Strings with the names of the sub labels or null if no sub labels exist for this label
     */
    public ArrayList<String> ExtractElements(String name);

    /**
     * Returns the names of the attributes of the label defined by name. An empty ArrayList is returned if label has no attributes
     * 
     * @param name
     *            Name of the Label to extract the attributes
     * @return Enumeration of attributes of the selected label. Can be empty.
     */
    public ArrayList<String> ExtractAttributeNames(String name);

    /**
     * Validates a node with the structure defined.
     * <p>
     * Generates all the possible simplified abstract syntax trees of the sentence using the formal language defined in the TEI structure file. When a
     * syntactically well-formed structure is found an empty string is returned. If no well-formed structure is detected, an error of the most complete
     * structure is returned. When two abstract syntax trees are equivalent, only one is stored, avoiding infinite loops.
     * </p>
     * 
     * @param node
     *            XML node to validate
     * @return String with the error or "" if all correct.
     */
    public String ValidateXMLNode(TextNode node);

}
