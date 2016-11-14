package com.application.components.TEITextStruct;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.application.components.Constants;
import com.application.components.Manager.FileManager;
import com.application.language.Labels;
import com.application.texteditor.TextEditorUI;
import com.application.widgets.client.TextEditorElements.AttributeSenderStruct;
import com.vaadin.ui.Notification;

import static com.application.components.Constants.FILETYPE;
import static com.application.components.Constants.STRINGTYPE;
import static com.application.components.Constants.XMLHEAD1;
import static com.application.components.Constants.XMLHEAD2DTD1;
import static com.application.components.Constants.XMLHEAD2DTD2;
import static com.application.components.Constants.TEIPROJECTNAME;

/**
 * Represents an structured TEI file simplifying the manipulation.
 * 
 * <p>
 * This structure is composed of a root node that should be a TEI label.
 * </p>
 * 
 * <p>
 * Each node is composed of a list of attributes and a list of children middle nodes. Also includes the text that is present before the first child node.
 * </p>
 * <p>
 * Each child middle node is composed by a node and a text that is present just after the label
 * </p>
 * <p>
 * Each attribute is composed of a name and a value
 * </p>
 * <p>
 * This is an example of structure<br>
 * 
 * <pre>
 * Node
 * /----------------------------------------------------\
 * |                                                    |
 * |            Middle Node                             |
 * |          /--------------------------------------\  |
 * |          | Node                                 |  |
 * |          | /--------------\                     |  |
 * |          | |  "SomeText"  | "Middle Node text"  |  |
 * |"SomeText"| \--------------/                     |  |
 * |          |                                      |  |
 * |          \--------------------------------------/  |
 * |                                                    |
 * \----------------------------------------------------/
 * </pre>
 * 
 * @author Miguel Urízar Salinas
 * 
 *
 */
public class TextStruct implements Serializable {
    private static final long serialVersionUID = 2125377047076617494L;

    /** Stores the root node (supposed to be TEI) */
    private TextNode rootNode = null;

    /** Defines the next identifier value to use in the structure */
    private int numberOfNodes = 0;

    /**
     * Generates a structure using a file as its source
     * 
     * @param src
     *            Path of the file to use as source
     * @param type
     *            Type of the file, can be {@link Constants#FILETYPE} for an XML file or {@link Constants#STRINGTYPE} for a Byte file.
     * @throws IOException
     *             If any IO errors occur.
     * @throws ParserConfigurationException
     *             If the file has an incorrect format.
     * @throws SAXException
     *             If any parse errors occur.
     */
    public TextStruct(String src, int type) throws IOException, ParserConfigurationException, SAXException {
	try {
	    // Open the file and parse it as DOM object
	    Document doc = null;
	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    dbFactory.setIgnoringComments(true);
	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    if (type == FILETYPE) {
		doc = dBuilder.parse(src);
	    } else if (type == STRINGTYPE) {
		doc = dBuilder.parse(new InputSource(new StringReader(src)));
	    } else {
		throw new IOException("No type defined.");
	    }

	    // Normalize the file
	    doc.getDocumentElement().normalize();

	    // Get root element
	    Node root = doc.getDocumentElement();

	    // Create root node
	    rootNode = new TextNode();

	    // Iterate through the whole file
	    numberOfNodes = rootNode.FillNodeFromFile(root, numberOfNodes) + 1;
	    if (numberOfNodes < 1) {
		throw new ParserConfigurationException();
	    }
	    // If there is an exception
	} catch (Exception e) {
	    if (type == FILETYPE) {
		TextEditorUI.getCurrent().logMessage(Level.SEVERE, "Error creating the text structure from file \"" + src + "\".", true);
	    } else {
		TextEditorUI.getCurrent().logMessage(Level.SEVERE, "Error creating the text structure from string.", true);
	    }
	    throw e;
	}
    }

    /**
     * Constructor for a new empty structure, generating TEI node and header and text.
     */
    public TextStruct() {
	// Create a new struct manually
	rootNode = new TextNode("TEI", "", numberOfNodes);
	rootNode.AddModifyAttribute("xmlns","http://www.tei-c.org/ns/1.0");
	numberOfNodes++;
	createGenericHeaderText();
    }

    /**
     * Generates the inner HTML to use in the {@link com.application.widgets.TextEditor}.
     * 
     * @return String with the inner HTML for showing the structure
     */
    public String GenerateInnerHTML() {
	return rootNode.GenerateInnerHTML(true);
    }

    /**
     * Generates a generic Header and Text labels, children of TEI.
     */
    private void createGenericHeaderText() {

	// Create new header node
	TextNode teiHeaderNode = new TextNode("teiHeader", "", numberOfNodes++);
	TextMiddleNode teiHeaderMiddleNode = new TextMiddleNode("", teiHeaderNode);
	rootNode.GetInnerLabels().add(teiHeaderMiddleNode);
	// Create file Desc
	TextNode teiFileDescNode = new TextNode("fileDesc", "", numberOfNodes++);
	TextMiddleNode teiFileDescMiddleNode = new TextMiddleNode("", teiFileDescNode);
	teiHeaderNode.GetInnerLabels().add(teiFileDescMiddleNode);
	// Create titleStmt
	TextNode teiTitleStmtNode = new TextNode("titleStmt", "", numberOfNodes++);
	TextMiddleNode teiTitleStmtMiddleNode = new TextMiddleNode("", teiTitleStmtNode);
	teiFileDescNode.GetInnerLabels().add(teiTitleStmtMiddleNode);
	// Create title
	TextNode teiTitleNode = new TextNode("title", "Title", numberOfNodes++);
	TextMiddleNode teiTitleMiddleNode = new TextMiddleNode("", teiTitleNode);
	teiTitleStmtNode.GetInnerLabels().add(teiTitleMiddleNode);
	// Create publicationStmt
	TextNode teiPublicationStmtTitleNode = new TextNode("publicationStmt", "", numberOfNodes++);
	TextMiddleNode teiPublicationStmtTitleMiddleNode = new TextMiddleNode("", teiPublicationStmtTitleNode);
	teiFileDescNode.GetInnerLabels().add(teiPublicationStmtTitleMiddleNode);
	// Create p
	TextNode teiPPublicationStmtTitleNode = new TextNode("p", "Publication information", numberOfNodes++);
	TextMiddleNode teiPPublicationStmtTitleMiddleNode = new TextMiddleNode("", teiPPublicationStmtTitleNode);
	teiPublicationStmtTitleNode.GetInnerLabels().add(teiPPublicationStmtTitleMiddleNode);
	// Create publicationStmt
	TextNode teiSourceDescTitleNode = new TextNode("sourceDesc", "", numberOfNodes++);
	TextMiddleNode teiSourceDescTitleMiddleNode = new TextMiddleNode("", teiSourceDescTitleNode);
	teiFileDescNode.GetInnerLabels().add(teiSourceDescTitleMiddleNode);
	// Create p
	TextNode teiPSourceDescTitleNode = new TextNode("p", "Information about the source", numberOfNodes++);
	TextMiddleNode teiPSourceDescTitleMiddleNode = new TextMiddleNode("", teiPSourceDescTitleNode);
	teiSourceDescTitleNode.GetInnerLabels().add(teiPSourceDescTitleMiddleNode);
	// Create new text node
	TextNode teiTextNode = new TextNode("text", "", numberOfNodes++);
	TextMiddleNode teiTextMiddleNode = new TextMiddleNode("", teiTextNode);
	rootNode.GetInnerLabels().add(teiTextMiddleNode);
	// Create new body node
	TextNode teiBodyNode = new TextNode("body", "", numberOfNodes++);
	TextMiddleNode teiBodyMiddleNode = new TextMiddleNode("", teiBodyNode);
	teiTextNode.GetInnerLabels().add(teiBodyMiddleNode);
    }

    /**
     * Generates a generic text label.
     */
    private void createGenericText() {

	// Create new body node
	TextNode teiBodyNode = new TextNode("text", "", numberOfNodes++);
	TextMiddleNode teiBodyMiddleNode = new TextMiddleNode("", teiBodyNode);
	rootNode.GetInnerLabels().add(teiBodyMiddleNode);
    }

    /**
     * Generates the HTML to use in the {@link com.application.widgets.TextEditor} only showing the Header label.
     * 
     * @return String with the inner teiHeader HTML for showing the structure
     */
    public String GenerateInnerTeiHeaderHTML() {
	if (rootNode.GetInnerLabels().size() < 1) {
	    // Show error
	    Notification.show(Labels.getString("error"), Labels.getString("noHeaderBody"), Notification.Type.TRAY_NOTIFICATION);
	    createGenericHeaderText();
	}
	return rootNode.GetInnerLabels().get(0).GetNode().GenerateInnerHTML(true);
    }

    /**
     * Generates the HTML to use in the {@link com.application.widgets.TextEditor} only showing the text label.
     * 
     * @return String with the inner text HTML for showing the structure
     */
    public String GenerateInnerBodyHTML() {
	// If there is no header, a generic body and header is created
	if (rootNode.GetInnerLabels().size() < 1) {
	    // Show error
	    Notification.show(Labels.getString("error"), Labels.getString("noHeaderBody"), Notification.Type.TRAY_NOTIFICATION);
	    createGenericHeaderText();
	}
	// If there is no body, a generic one is created
	if (rootNode.GetInnerLabels().size() < 2) {
	    // Show error
	    Notification.show(Labels.getString("error"), Labels.getString("noBody"), Notification.Type.TRAY_NOTIFICATION);
	    createGenericText();
	}
	if (rootNode.GetInnerLabels().size() > 2)
	    rootNode.GetInnerLabels().get(1).GetNode().SetError(Labels.getString("moreThanOneBody"));
	return rootNode.GetInnerLabels().get(1).GetNode().GenerateInnerHTML(true);
    }

    /**
     * Generates an XML file using this structure.
     * 
     * @return string with the generated XML.
     */
    public String GenerateInnerXML() {
	return XMLHEAD1 + XMLHEAD2DTD1 + TEIPROJECTNAME + XMLHEAD2DTD2 + rootNode.GenerateInnerXML(0);
    }

    /**
     * Creates a new Label in the parent defined.
     * 
     * @param name
     *            Name of the new label
     * @param value
     *            inner text of the new label
     * @param parent
     *            parent identifier
     * @param afterText
     *            Text to define after the label inserted
     * @return Identifier number of the node added
     */
    public int AddNewLabel(String name, String value, int parent, String afterText) {

	int success = rootNode.AddLabel(name, value, parent, numberOfNodes, afterText);
	if (success == 0) {
	    numberOfNodes++;
	    return numberOfNodes - 1;
	}
	return success;
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
	return rootNode.AddModifyAttributes(attributesToSend, id);
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
	return rootNode.ModifyBeginningText(newText, id);
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
	return rootNode.ModifyEndingText(newText, id);
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
    public int changeLabel(int labelId, String newLabelName) {
	return rootNode.ChangeLabel(labelId, newLabelName);
    }

    /**
     * Removes the label with selected id.
     * 
     * @param id
     *            Identification number for the label to remove
     * @return 0 if success or -1 if node does not exist
     */
    public int removeLabel(int id) {
	return rootNode.RemoveLabel(id);
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
    public int createLabel(int parentId, String newLabelName, int beginTextId, int beginTextOffset, int endTextId, int endTextOffset) {

	int answer = rootNode.CreateLabel(parentId, newLabelName, beginTextId, beginTextOffset, endTextId, endTextOffset, numberOfNodes);
	// If node inserted correctly, number of nodes increase by 1
	if (answer == 0) {
	    numberOfNodes++;
	}
	return answer;
    }

    /**
     * Validate the whole XML structure depending on a TEI structure
     * 
     * @param manager
     *            Structure to used to validate the file
     * @return 0 if all correct or -1 if there is an error and -2 if manager does not exist
     */
    public int validateXMLStructure(FileManager manager) {

	if (manager != null) {
	    return rootNode.validateXMLNode(manager);
	} else {
	    rootNode.SetError(Labels.getString("noSchema"));
	    return -2;
	}
    }

}
