package com.application.components.TEITextStruct;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.application.components.Manager.FileManager;
import com.application.widgets.client.TextEditorElements.AttributeSenderStruct;
import com.google.gwt.xml.client.Element;

import static com.application.components.Constants.XMLWHITESPACE;

/**
 * This class is for registering a single node storing a TEI label with its label name, child label nodes, attributes and inner text.
 * 
 * @author Miguel Urízar Salinas
 * 
 * @see com.application.components.TEITextStruct.TextStruct
 */
public class TextNode implements Serializable {
    private static final long serialVersionUID = -7896900175049543705L;

    /** Stores the sublabels */
    private ArrayList<TextMiddleNode> innerLabels = new ArrayList<TextMiddleNode>();

    /**
     * Stores the text at the beginning of the label until a new child label appears
     */
    private String beginningText = "";

    /** Stores the name of the label */
    private String labelName = "";

    /** Stores the unique identifier name */
    private int HTMLIdentifier;

    /** Stores the attributes */
    private ArrayList<TSAttribute> attributes = new ArrayList<TSAttribute>();

    /** Stores the TEI validation error that can have the node */
    private String errors = "";

    /**
     * Constructor for a new label node that does not have children
     * 
     * @param label
     *            Label name of the new node
     * @param text
     *            Inside text to insert in the label
     * @param identifierNumber
     *            Unique number to identify the node
     */
    public TextNode(String label, String text, int identifierNumber) {
	SetLabelName(label);
	SetHTMLIdentifier(identifierNumber);
	beginningText = text;
    }

    /**
     * Constructor for a new label node that has children
     * 
     * @param label
     *            Label name of the new node
     * @param text
     *            Text placed at the beginning of the label
     * @param identifierName
     *            Unique number to identify the node
     * @param labels
     *            Array of TextMiddleNodes that represent the child nodes of the new Label node
     */
    public TextNode(String label, String text, int identifierName, ArrayList<TextMiddleNode> labels) {
	this(label, text, identifierName);
	if (labels != null)
	    innerLabels = labels;
	else
	    innerLabels.clear();
    }

    /**
     * Constructor for creating an empty node
     */
    public TextNode() {
	SetHTMLIdentifier(-1);
    }

    /**
     * Generates the inner HTML of this node.
     * 
     * @return String with the inner HTML for showing the node
     */
    public String GenerateInnerHTML(boolean rootNode) {
	String innerHTML = "<div  class=\"";
	// Define the css and class depending on the label name

	if (rootNode) {
	    innerHTML += "globalText_element";
	} else {
	    innerHTML += "text_element";
	}
	// Change css if there is an error
	if (!errors.isEmpty()) {
	    innerHTML += " error_text_element";
	    innerHTML += "\" " + "title=\"" + errors;
	}

	if (rootNode) {
	    innerHTML += "\" rootElement=\"0";
	} else {
	    innerHTML += "\" rootElement=\"1";
	}

	innerHTML += "\" label=\"" + StringEscapeUtils.escapeHtml4(GetLabelName()) + "\" id=\"" + Integer.toString(GetHTMLIdentifier()) + "\" ";
	// Add attributes
	for (int i = 0; i < attributes.size(); i++) {
	    innerHTML += attributes.get(i).GenerateInnerHTML();
	}
	// An space to allow selections
	innerHTML += ">" + (char) 0xA0 + StringEscapeUtils.escapeHtml4(beginningText);
	// Add innerLabels
	for (int i = 0; i < innerLabels.size(); i++) {
	    innerHTML += innerLabels.get(i).GenerateInnerHTML(false);
	}
	innerHTML += "</div>";
	return innerHTML;
    }

    /**
     * Generates an XML fragment using this Node.
     * 
     * @return String with the inner XML of the node
     */
    public String GenerateInnerXML(int depth) {
	// Generate the variable
	String innerXML = "";
	// Add the depth
	for (int i = 0; i < depth; i++) {
	    innerXML += XMLWHITESPACE;
	}
	// Add the labelname
	innerXML += "<" + StringEscapeUtils.escapeXml(GetLabelName());

	// Add attributes
	for (int i = 0; i < attributes.size(); i++) {
	    innerXML += attributes.get(i).GenerateInnerXML();
	}
	// We have no child nodes for this node
	if (innerLabels.isEmpty()) {
	    // If we have no innertext (type <TEI/>)
	    if (beginningText.isEmpty()) {
		innerXML += "/>\n";
		// If we have innertext (type <TEI> blablabla </TEI>)
	    } else {
		innerXML += ">" + StringEscapeUtils.escapeXml(beginningText) + "</" + StringEscapeUtils.escapeXml(GetLabelName()) + ">\n";
	    }
	} else {

	    innerXML += ">" + StringEscapeUtils.escapeXml(beginningText) + "\n";
	    // Add innerLabels
	    for (int i = 0; i < innerLabels.size(); i++) {
		innerXML += innerLabels.get(i).GenerateInnerXML(depth + 1);
	    }
	    // Add the depth
	    for (int i = 0; i < depth; i++) {
		innerXML += XMLWHITESPACE;
	    }
	    innerXML += "</" + StringEscapeUtils.escapeXml(GetLabelName()) + ">\n";
	}
	return innerXML;
    }

    /**
     * Inserts an attribute and its value.
     * <p>
     * If the attribute already exists, it is modified with the new value.
     * </p>
     * 
     * @param name
     *            Name of the attribute to modify or create.
     * @param value
     *            Value of the attribute to insert or modify.
     * @return 0 if attribute is correctly inserted or updated or -1 if it was impossible to insert it
     */
    public int AddModifyAttribute(String name, String value) {

	boolean attributeAlreadyIn = false;
	// If attribute is inside we modify it
	for (int i = 0; (i < attributes.size()) && (attributeAlreadyIn == false); i++) {
	    if (attributes.get(i).GetName().equalsIgnoreCase(name)) {
		if (value.length() == 0) {
		    attributes.remove(i);
		} else {
		    attributes.get(i).SetValue(value);
		}
		attributeAlreadyIn = true;
	    }
	}
	// If attribute is new
	if (attributeAlreadyIn == false) {
	    if (value.length() != 0) {
		TSAttribute newAttribute = new TSAttribute(name, value);
		attributes.add(newAttribute);
	    } else {
		return 0;
	    }
	}
	return 0;
    }

    /**
     * Creates a new Label in the parent defined.
     * 
     * @param name
     *            Name of the new label
     * @param value
     *            inner text of the new label
     * @param parent
     *            parent identifier whewre the node is inserted
     * @param identifier
     *            Identifier number for the new label to add
     * @param afterText
     *            Text that goes after the new label
     * @return 0 if node added successfully and -1 if there was an error.
     */
    public int AddLabel(String name, String value, int parent, int identifier, String afterText) {
	if (parent == GetHTMLIdentifier()) {
	    // Remove duplicate spaces and spaces at the beginning or end for
	    // the text inserted
	    TextNode newNode = new TextNode(name, value, identifier);
	    TextMiddleNode newMiddleNode = new TextMiddleNode(afterText, newNode);
	    innerLabels.add(newMiddleNode);
	    return 0;
	} else {
	    // Run other nodes to search for the node
	    for (int i = 0; i < innerLabels.size(); i++) {

		if (innerLabels.get(i).AddLabel(name, value, parent, identifier, afterText) == 0) {
		    return 0;
		}
	    }
	    return -1;
	}
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

	// If is this node
	if (id == GetHTMLIdentifier()) {
	    int errors = 0;
	    for (int i = 0; i < attributesToSend.length; i++) {
		if (attributesToSend[i] != null)
		    errors += AddModifyAttribute(attributesToSend[i].name, attributesToSend[i].value);
		else
		    errors++;
	    }
	    return errors;
	}
	// Check if id is a subnode
	else {
	    for (int i = 0; i < innerLabels.size(); i++) {
		// -1 Implies is not this node, 0 represents a successful
		// insertion and positive values return number of attributes
		// that were not possible to update
		int response = innerLabels.get(i).AddModifyAttributes(attributesToSend, id);
		if (response >= 0) {
		    return response;
		}
	    }
	    return -1;
	}

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
	// If is this node
	if (id == GetHTMLIdentifier()) {
	    beginningText = newText.trim().replace((char) 0xA0, ' ');
	    return 0;
	    // Check if id is a subnode
	} else {
	    for (int i = 0; i < innerLabels.size(); i++) {
		// -1 Implies is not this node, 0 represents a successful
		// insertion
		if (innerLabels.get(i).ModifyBeginningText(newText, id) == 0)
		    return 0;
	    }
	    return -1;
	}
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
	// Go through the inner nodes searching for the node
	for (int i = 0; i < innerLabels.size(); i++) {
	    // -1 Implies is not this node, 0 represents a successful insertion
	    if (innerLabels.get(i).ModifyEndingText(newText, id) == 0)
		return 0;
	}
	return -1;
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
	// If is this node
	if (labelId == GetHTMLIdentifier()) {
	    SetLabelName(newLabelName);
	    return 0;
	} else {
	    for (int i = 0; i < innerLabels.size(); i++) {
		// -1 Implies is not this node, 0 represents a successful
		// insertion
		if (innerLabels.get(i).ChangeLabel(labelId, newLabelName) == 0)
		    return 0;
	    }
	    return -1;
	}
    }

    /**
     * Removes the label with selected id.
     * 
     * @param id
     *            Identification number for the label to remove
     * @return 0 if success or -1 if node does not exist and 1 if is needed to change this node
     */
    public int RemoveLabel(int id) {
	// If is this node
	if (id == GetHTMLIdentifier()) {
	    return 1;
	} else {
	    for (int i = 0; i < innerLabels.size(); i++) {
		// -1 Implies is not this node, 0 represents a successful
		// insertion
		int answer = innerLabels.get(i).RemoveLabel(id);
		// The node has been removed
		if (answer == 0) {
		    return 0;
		}
		// This is the node to remove
		if (answer == 1) {
		    TextMiddleNode nodeToRemove = innerLabels.get(i);

		    // For the beforeText
		    if (i == 0) {
			// Is first node so all the data goes to beforeText
			beginningText += nodeToRemove.GetNode().beginningText;
		    } else {
			// Beforetext goes to the aftertext of the previous node
			innerLabels.get(i - 1).SetAfterText((innerLabels.get(i - 1).GetAfterText() + nodeToRemove.GetNode().beginningText));
		    }

		    // For the afterText
		    // Node to remove has no children
		    if (nodeToRemove.GetNode().innerLabels.size() == 0) {
			// Is the first node, text goes in the beginning
			if (i == 0) {
			    beginningText += nodeToRemove.GetAfterText();
			    // The afterText goes at the end of the previous
			    // node
			} else {
			    innerLabels.get(i - 1).SetAfterText((innerLabels.get(i - 1).GetAfterText() + nodeToRemove.GetAfterText()));
			}
			// The afterText goes at the end of the last child node
		    } else {
			int lastChild = nodeToRemove.GetNode().innerLabels.size() - 1;
			nodeToRemove.GetNode().innerLabels.get(lastChild).SetAfterText(
				(nodeToRemove.GetNode().innerLabels.get(lastChild).GetAfterText() + nodeToRemove.GetAfterText()));
		    }

		    // Add the middleNodes to this position
		    for (int j = nodeToRemove.GetNode().innerLabels.size() - 1; j >= 0; j--) {
			innerLabels.add(i + 1, nodeToRemove.GetNode().innerLabels.get(j));
		    }
		    innerLabels.remove(i);
		    return 0;
		}
	    }
	    return -1;
	}
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
	// We have to create node in here
	if (parentId == GetHTMLIdentifier()) {
	    String newInnerText, newAfterText;
	    int insertionPoint;
	    ArrayList<TextMiddleNode> newInnerLabels = new ArrayList<TextMiddleNode>();
	    // New label starts inside the actual node
	    if (beginTextId == -1) {
		// New label ends inside the actual node
		if (endTextId == -1) {
		    // Generate the new inner and after texts
		    // If is empty it grabbed the space generated in the client
		    // for allowing selection
		    if (beginningText.isEmpty()) {
			newInnerText = "";
			newAfterText = "";
		    } else {
			newInnerText = beginningText.substring(beginTextOffset, endTextOffset);
			newAfterText = beginningText.substring(endTextOffset);
			beginningText = beginningText.substring(0, beginTextOffset);
		    }

		    // New label ends in other node
		} else {

		    // Generate the new inner text
		    newInnerText = beginningText.substring(beginTextOffset);
		    beginningText = beginningText.substring(0, beginTextOffset);

		    // Search for child node
		    int endChildNode = GetChildNode(endTextId);
		    if (endChildNode < 0)
			return -1;

		    // Extract the text in the after.
		    // If is empty it grabbed the space generated in the client
		    // for allowing selection
		    if (innerLabels.get(endChildNode).GetAfterText().isEmpty()) {
			newAfterText = "";
		    } else {
			newAfterText = innerLabels.get(endChildNode).GetAfterText().substring(endTextOffset);
			innerLabels.get(endChildNode).SetAfterText(innerLabels.get(endChildNode).GetAfterText().substring(0, endTextOffset));
		    }

		    // Add the middleNodes of new label
		    for (int i = endChildNode; i >= 0; i--) {
			newInnerLabels.add(0, innerLabels.get(i));
			innerLabels.remove(i);
		    }
		}
		insertionPoint = 0;

		// New label starts in the after of a node
	    } else {

		// Search for beginning child node
		int beginChildNode = GetChildNode(beginTextId);
		if (beginChildNode < 0)
		    return -1;
		// Same node, modify afterText
		if (beginTextId == endTextId) {
		    // Generate the new inner and after texts
		    // If is empty it grabbed the space generated in the client
		    // for allowing selection
		    if (innerLabels.get(beginChildNode).GetAfterText().isEmpty()) {
			newInnerText = "";
			newAfterText = "";
		    } else {
			newInnerText = innerLabels.get(beginChildNode).GetAfterText().substring(beginTextOffset, endTextOffset);
			newAfterText = innerLabels.get(beginChildNode).GetAfterText().substring(endTextOffset);
			innerLabels.get(beginChildNode).SetAfterText(innerLabels.get(beginChildNode).GetAfterText().substring(0, beginTextOffset));
		    }

		    // different node
		} else {
		    // Generate the new inner text
		    newInnerText = innerLabels.get(beginChildNode).GetAfterText().substring(beginTextOffset);
		    innerLabels.get(beginChildNode).SetAfterText(innerLabels.get(beginChildNode).GetAfterText().substring(0, beginTextOffset));

		    // Search for child node
		    int endChildNode = GetChildNode(endTextId);
		    if (endChildNode < 0)
			return -1;
		    // Generate the new afterText
		    // If is empty it grabbed the space generated in the client
		    // for allowing selection
		    if (innerLabels.get(endChildNode).GetAfterText().isEmpty()) {
			newAfterText = "";
		    } else {
			newAfterText = innerLabels.get(endChildNode).GetAfterText().substring(endTextOffset);
			innerLabels.get(endChildNode).SetAfterText(innerLabels.get(endChildNode).GetAfterText().substring(0, endTextOffset));
		    }

		    // Add the middleNodes of new label
		    for (int i = endChildNode; i > beginChildNode; i--) {
			newInnerLabels.add(0, innerLabels.get(i));
			innerLabels.remove(i);
		    }
		}
		insertionPoint = beginChildNode + 1;
	    }

	    // Create new node
	    TextMiddleNode newMiddleNode = new TextMiddleNode(newAfterText, new TextNode(newLabelName, newInnerText, newLabelId, newInnerLabels));
	    // Insert the new node
	    this.innerLabels.add(insertionPoint, newMiddleNode);
	    return 0;

	    // Is not this node, we check sub nodes
	} else {
	    for (int i = 0; i < innerLabels.size(); i++) {
		// -1 Implies is not this node, 0 represents a successful
		// insertion
		if (innerLabels.get(i).CreateLabel(parentId, newLabelName, beginTextId, beginTextOffset, endTextId, endTextOffset, newLabelId) == 0)
		    return 0;
	    }
	    // No sub node was the parent node
	    return -1;
	}
    }

    /**
     * Get the position in {@link TextNode#innerLabels} of a defined node.
     * <p>
     * If the node is not a child node, a -1 is returned.
     * </p>
     * 
     * @param childId
     *            Identification number of the child node to search
     * @return Number of node or -1 if selected id is not a child node
     */
    private int GetChildNode(int childId) {
	for (int i = 0; i < innerLabels.size(); i++) {
	    // Check if the id is the same
	    if (innerLabels.get(i).GetNode().GetHTMLIdentifier() == childId)
		return i;
	}
	return -1;
    }

    /**
     * 
     * Function to read an xml file and create this node.
     * <p>
     * It has to fulfill the XML requirements
     * </p>
     * 
     * @param DOMNode
     *            Node represented in the DOM parser
     * @param nodeNumber
     *            Node identifier that has this new node
     * @return Number of nodes that are included in the structure after parsing this node or -1 if there was a problem.
     */
    public int FillNodeFromFile(Node DOMNode, int nodeNumber) {

	int numberOfNodes = nodeNumber + 1;
	// Define the nodeNumber
	SetHTMLIdentifier(nodeNumber);
	// Define the label
	SetLabelName(DOMNode.getNodeName());

	// For extracting attributes
	NamedNodeMap attributes = DOMNode.getAttributes();
	// If there are attributes
	if (attributes != null) {
	    // get the number of nodes in this map
	    int numAttrs = attributes.getLength();
	    // iterate through all the attributes
	    for (int i = 0; i < numAttrs; i++) {
		Attr attr = (Attr) attributes.item(i);
		AddModifyAttribute(attr.getNodeName(), attr.getNodeValue());
	    }
	}
	NodeList children = DOMNode.getChildNodes();
	int numberOfChildren = children.getLength();

	// The first node can be #text
	int i = 0;
	if (numberOfChildren > 0) {
	    if (children.item(0).getNodeType() == Element.TEXT_NODE) {
		// Assign inner text
		beginningText = children.item(0).getNodeValue().trim();
		i++;
	    }
	    // Assign subnodes that can have a #text afterwards
	    for (; i < numberOfChildren; i++) {
		// Create a new node
		TextNode subNode = new TextNode();
		// Fill the node
		numberOfNodes = subNode.FillNodeFromFile(children.item(i), numberOfNodes);
		// There was a problem creating child Node
		if (numberOfNodes == -1) {
		    return -1;
		}
		numberOfNodes++;

		// Check if there is another #text after the node
		String rawSubText = "";
		if (i + 1 < numberOfChildren) {
		    if (children.item(i + 1).getNodeType() == Element.TEXT_NODE) {
			rawSubText = children.item(i + 1).getNodeValue().trim();
			i++;
		    }
		}
		// Add the subNode
		TextMiddleNode newMiddleNode = new TextMiddleNode(rawSubText, subNode);
		innerLabels.add(newMiddleNode);
	    }
	    // We have an empty type or a comment or another special
	} else {
	    // TODO ZZZ Allow comments
	    beginningText = "";
	    return numberOfNodes;
	}
	return numberOfNodes;
    }

    /**
     * Validates the node with the XML structure and stores the errors in the node if there are.
     * 
     * @param manager
     *            of the TEI structure
     */
    public int validateXMLNode(FileManager manager) {
	int error = 0;
	errors = manager.ValidateXMLNode(this);
	if (!errors.isEmpty())
	    error = -1;
	// Add innerLabels
	for (int i = 0; i < innerLabels.size(); i++) {
	    int validation = innerLabels.get(i).GetNode().validateXMLNode(manager);
	    if (validation != 0)
		error = -1;
	}
	return error;
    }

    /**
     * @return Value of variable labelName
     */
    public String GetLabelName() {
	return labelName;
    }

    /**
     * @param labelName
     *            Name of the new label
     */
    public void SetLabelName(String labelName) {
	this.labelName = labelName;
    }

    /**
     * @return Value of variable HTMLIdentifier
     */
    public int GetHTMLIdentifier() {
	return HTMLIdentifier;
    }

    /**
     * @param hTMLIdentifier
     *            the hTMLIdentifier to set
     */
    public void SetHTMLIdentifier(int hTMLIdentifier) {
	HTMLIdentifier = hTMLIdentifier;
    }

    /**
     * @param Errors
     *            The string with errors to show
     */
    public void SetError(String Errors) {
	errors = Errors;
    }

    /**
     * @return Value of variable beginningText
     */
    public String GetBeginningText() {
	return beginningText;
    }

    /**
     * @return Value of variable innerLabels
     */
    public ArrayList<TextMiddleNode> GetInnerLabels() {
	return innerLabels;
    }
}
