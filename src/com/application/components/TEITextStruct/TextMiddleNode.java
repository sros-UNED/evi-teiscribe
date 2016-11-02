package com.application.components.TEITextStruct;

import static com.application.components.Constants.XMLWHITESPACE;

import java.io.Serializable;

import org.apache.commons.lang3.StringEscapeUtils;

import com.application.widgets.client.TextEditorElements.AttributeSenderStruct;

/**
 * This class is for registering a single TextNode and text shown after the label and before the next label.
 * 
 * @author Miguel Urízar Salinas
 * 
 * @see com.application.components.TEITextStruct.TextStruct
 *
 */
public class TextMiddleNode implements Serializable {
    private static final long serialVersionUID = 1935844954678147888L;

    /** Text shown at the beginning of the label */
    private String afterText = "";

    /** Stores the different attributes of the TEI label */
    private TextNode node = null;

    /**
     * Constructor for middle node with an already created node
     * 
     * @param string
     *            After text to put in the node
     * @param newNode
     *            Previously generated node
     */
    public TextMiddleNode(String string, TextNode newNode) {
	SetAfterText(string);
	SetNode(newNode);
    }

    /**
     * Generates the inner HTML of this middle node.
     * 
     * @return String with the inner HTML for showing the node
     */
    public String GenerateInnerHTML(boolean rootNode) {
	// An space to allow selections
	return GetNode().GenerateInnerHTML(rootNode) + (char) 0xA0 + StringEscapeUtils.escapeHtml4(GetAfterText());
    }

    /**
     * Inserts a new node in this node or any of the child nodes.
     * <p>
     * Only useful for new nodes, not modifying them.
     * </p>
     * 
     * @param name
     *            Name of the label of the new node
     * @param value
     *            Text to insert in the new node
     * @param parent
     *            Parent to contain the new node
     * @param identifier
     *            Number that identifies the new node
     * @param afterText
     *            Text to insert after the new node in the parent
     * @return 0 if node is correctly inserted or -1 if there was a problem inserting it
     */
    public int AddLabel(String name, String value, int parent, int identifier, String afterText) {
	if (GetNode() != null)
	    return GetNode().AddLabel(name, value, parent, identifier, afterText);
	else
	    return -1;
    }

    /**
     * Inserts or deletes a list of attributes to the label defined by id.
     * 
     * @param attributesToSend
     *            List of attributes modified to update
     * @param id
     *            Id of the node to insert the attributes
     * @return 0 if success, -1 if node does not exist or the number of attributes that generated error.
     */
    public int AddModifyAttributes(AttributeSenderStruct[] attributesToSend, int id) {
	return GetNode().AddModifyAttributes(attributesToSend, id);
    }

    /**
     * Changes the beginning text of the label defined by id.
     * 
     * @param newText
     *            Text to update
     * @param id
     *            Identification number of the node to change the text
     * @return 0 if success or -1 if node does not exist
     */
    public int ModifyBeginningText(String newText, int id) {
	return GetNode().ModifyBeginningText(newText, id);
    }

    /**
     * Changes the ending text of the label defined by id.
     * 
     * @param newText
     *            Text to update
     * @param id
     *            Identification number of the node to change the text
     * @return 0 if success or -1 if node does not exist
     */
    public int ModifyEndingText(String newText, int id) {
	if (GetNode().GetHTMLIdentifier() == id) {
	    SetAfterText(newText.trim().replace((char) 0xA0, ' '));
	    return 0;
	}
	return GetNode().ModifyEndingText(newText, id);
    }

    /**
     * Changes the label name defined by id.
     * 
     * @param labelId
     *            Label identification to change the id
     * @param newLabelName
     *            New name for the label
     * @return 0 if success or -1 if node does not exist
     */
    public int ChangeLabel(int labelId, String newLabelName) {
	return GetNode().ChangeLabel(labelId, newLabelName);
    }

    /**
     * Removes the label with selected id.
     * 
     * @param id
     *            Identification number for the label to remove
     * @return 0 if success or -1 if node does not exist
     */
    public int RemoveLabel(int id) {
	int answer = GetNode().RemoveLabel(id);
	return answer;
    }

    /**
     * Creates a new label with the selected text offsets defined, it changes automatically child nodes.
     * 
     * @param parentId
     *            Identification number for the parent Label where the child node will be created
     * @param newLabelName
     *            Name of the new label to be generated.
     * @param beginTextId
     *            Id of the label related to the afterText where the new label starts. -1 implies that the new label starts in the innerText of the parent node.
     * @param beginTextOffset
     *            Offset where the text starts.
     * @param endTextId
     *            Id of the label related to the afterText where the new label ends. -1 implies that the new label ends in the innerText of the parent node.
     * @param endTextOffset
     *            Offset where the text ends.
     */
    public int CreateLabel(int parentId, String newLabelName, int beginTextId, int beginTextOffset, int endTextId, int endTextOffset, int newLabelId) {
	return GetNode().CreateLabel(parentId, newLabelName, beginTextId, beginTextOffset, endTextId, endTextOffset, newLabelId);
    }

    /**
     * Generates an XML fragment using this middleNode.
     * 
     * @return String with the inner XML of the file
     */
    public String GenerateInnerXML(int depth) {
	// Generate the variable
	String innerXML = GetNode().GenerateInnerXML(depth);
	// If we have aftertext
	if (!GetAfterText().isEmpty()) {
	    // Add the beginning white space
	    for (int i = 0; i < depth; i++) {
		innerXML += XMLWHITESPACE;
	    }
	    // Add the aftertext
	    innerXML += StringEscapeUtils.escapeXml(GetAfterText()) + "\n";
	}
	return innerXML;
    }

    /**
     * @return Value of afterText
     */
    public String GetAfterText() {
	return afterText;
    }

    /**
     * @param afterText
     *            New value for afterText
     */
    public void SetAfterText(String afterText) {
	this.afterText = afterText;
    }

    /**
     * @return Value of node
     */
    public TextNode GetNode() {
	return node;
    }

    /**
     * @param node
     *            New value for node.
     */
    public void SetNode(TextNode node) {
	this.node = node;
    }
}
