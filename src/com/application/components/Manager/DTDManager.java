package com.application.components.Manager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

import com.application.components.TEITextStruct.TextMiddleNode;
import com.application.components.TEITextStruct.TextNode;
import com.application.components.dtdParser.Attribute;
import com.application.components.dtdParser.DTD;
import com.application.components.dtdParser.DTDParser;
import com.application.components.dtdParser.ElementType;
import com.application.components.dtdParser.Group;
import com.application.components.dtdParser.Particle;
import com.application.components.dtdParser.Reference;

import static com.application.components.Constants.STRINGTYPE;
import static com.application.components.Constants.FILETYPE;

import org.xml.sax.*;

import com.application.language.Labels;

/**
 * Class to manage a file with DTD format
 * 
 * @author Miguel Urízar Salinas
 * 
 */
public class DTDManager implements FileManager, Serializable {
    private static final long serialVersionUID = -1232084043614615246L;

    /** DTD structure loaded from the selected file */
    DTD dtd = null;

    /**
     * Constructor for a dtd file manager
     * 
     * @param src
     *            File name of the dtd
     * @throws Exception
     *             problem reading the file
     */
    public DTDManager(String src, int type) throws Exception {
	// Create an InputSource
	InputSource source = null;
	if (type ==FILETYPE)
	{
	    source = new InputSource(new InputStreamReader(new FileInputStream(src),StandardCharsets.UTF_8));
	} else if (type ==STRINGTYPE) {
	    source = new InputSource( new StringReader( src ) );
	} else {
	    throw new Exception("No type defined.");
	}
	DTDParser parser = new DTDParser();
	// Create Hashtable for the xml prefixes. Neccesary to work with this
	// parser
	Hashtable<String, String> hashtableTEI = new Hashtable<String, String>() {
	    private static final long serialVersionUID = 3502840718901960075L;

	    {
		put("xml", "http://www.w3.org/XML/1998/namespace");
		put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
	    }
	};
	// Read the dtd file being external to the xml file
	dtd = parser.parseExternalSubset(source, hashtableTEI);
    }
    
    

    /**
     * Gives the elementType that has the selected name
     * 
     * @param name
     *            Universal Name of the Label structure to return
     * @return Label structure with the selected name
     */
    private ElementType GetElement(String name) {
	Enumeration<ElementType> e = dtd.elementTypes.elements();
	ElementType valor;
	while (e.hasMoreElements()) {
	    valor = e.nextElement();
	    if (valor.name.getUniversalName().equalsIgnoreCase(name)) {
		return valor;
	    }
	}
	return null;
    }

    /**
     * {@inheritDoc}
     */
    public ArrayList<String> ExtractElements(String name) {

	ArrayList<String> arrayList = new ArrayList<String>();
	ElementType labelStruct = GetElement(name);
	if (labelStruct == null) {
	    return null;
	}
	Enumeration<ElementType> e = labelStruct.children.elements();
	if (e.hasMoreElements()) {
	    ElementType valor;
	    while (e.hasMoreElements()) {
		valor = e.nextElement();
		// Dummy values are added for supporting complex DTD schemas,
		// they are not useful
		if (!valor.name.getUniversalName().startsWith("_DUMMY_")) {
		    arrayList.add(valor.name.getUniversalName());
		}
	    }
	    Collections.sort(arrayList, String.CASE_INSENSITIVE_ORDER);
	    return arrayList;
	} else {
	    return null;
	}
    }

    /**
     * Returns a list of attributes of a label
     * 
     * @param name
     *            Name of the Label to extract the attributes
     * @return Enumeration of Strings with the names of the attributes
     */
    public Enumeration<Attribute> ExtractAttributes2(String name) {
	return GetElement(name).attributes.elements();
    }

    /**
     * {@inheritDoc}
     */
    public ArrayList<String> ExtractAttributeNames(String name) {

	ArrayList<String> arrayList = new ArrayList<String>();
	Enumeration<Attribute> algo = ExtractAttributes2(name);

	if (algo.hasMoreElements()) {
	    Attribute valor;
	    while (algo.hasMoreElements()) {
		valor = algo.nextElement();
		arrayList.add(valor.name.getLocalName());
	    }
	    Collections.sort(arrayList, String.CASE_INSENSITIVE_ORDER);
	    return arrayList;
	} else {
	    return null;
	}
    }

    /**
     * {@inheritDoc}
     */
    public String ValidateXMLNode(TextNode node) {

	// Error for showing
	String error = "";
	// Extract the label type
	ElementType TEILabel = GetElement(node.GetLabelName());
	// label does not exist in the dtd
	if (TEILabel == null) {
	    return Labels.getString("noLabelInSchema");
	}
	// Type of node (only data, elements or mixed)
	// Elements and mixed data go on checking for the structure
	switch (TEILabel.contentType) {
	// Any content is allowed, so no checks
	case ElementType.CONTENT_ANY:
	    return "";
	    // It must have no content inside
	case ElementType.CONTENT_EMPTY:
	    if (!node.GetBeginningText().trim().isEmpty())
		error += Labels.getString("illegalText") + "\n";
	    if (!node.GetInnerLabels().isEmpty())
		error += Labels.getString("noSubLabelsAlowed");
	    return error;
	    // Only text is allowed
	case ElementType.CONTENT_PCDATA:
	    if (!node.GetInnerLabels().isEmpty())
		error += Labels.getString("noSubLabelsAlowed");
	    return error;
	    // Only elements are allowed, no pcdata
	case ElementType.CONTENT_ELEMENT:
	    if (IsTextInNode(node))
		error += Labels.getString("illegalText");
	    break;
	// Mixed text is allowed
	case ElementType.CONTENT_MIXED:
	    break;
	// Don't know what goes inside (error)
	default:
	    return Labels.getString("structNotDefined");
	}

	// Extract he structure of the node
	Particle nodeStruct = TEILabel.content;

	// create the different possibilities for the structure
	HashSet<Integer> set = new HashSet<Integer>();
	set.add(0);

	HashSet<Integer> answers = checkGroup(set, nodeStruct, node);
	// Check the answers
	boolean correct = false;
	int expectedValue = node.GetInnerLabels().size();
	int maxValue = 0;
	for (int i : answers) {
	    if (i > maxValue)
		maxValue = i;
	}
	if (maxValue == expectedValue)
	    correct = true;
	if (!correct) {
	    return Labels.getString("illegalLabel") + "&#147" + node.GetInnerLabels().get(maxValue).GetNode().GetLabelName() + "&#148.";
	}
	return error;
    }

    /**
     * Checks a SubStructure of only nodes developing the possible abstract trees and removing the incompatible ones.
     * 
     * @param values
     *            Values defining the abstract tree
     * @param particle
     *            SubStructure of only nodes to check the different values
     * @param node
     *            XML node to validate
     * @return HashSet of possible positions having parsed this element
     */
    private HashSet<Integer> checkGroup(HashSet<Integer> values, Particle particle, TextNode node) {

	// Create the hashSet for the different options
	HashSet<Integer> answer = new HashSet<Integer>();

	// Type of particle
	switch (particle.type) {

	// Is an Element without subgroups
	case Particle.TYPE_ELEMENTTYPEREF:
	    answer = ElementCheck(node, values, (Reference) particle);
	    break;

	// If we have a sequence list of elements, all are mandatory
	case Particle.TYPE_SEQUENCE:
	    Group group = (Group) particle;
	    // For iterating we need the values in the answer
	    answer.addAll(values);
	    // Iterate through all the sequential groups
	    for (int i = 0; i < group.members.size(); i++) {
		// We need to pass through the answers of last group to new
		// group
		HashSet<Integer> tempAnswer = new HashSet<Integer>();
		tempAnswer.addAll(answer);
		// The answer is changed with new valid answers
		answer = checkGroup(tempAnswer, (Particle) group.members.elementAt(i), node);
	    }

	    // If group is not required we add original values
	    if (!group.isRequired)
		answer.addAll(values);

	    // If we can repeat the element
	    if (group.isRepeatable) {
		// Variable for checking if another round gives new variables
		HashSet<Integer> beforeAnswer = answer;
		// Repeat while we have new values each round
		do {
		    // Add last round values
		    answer.addAll(beforeAnswer);
		    // For iterating we need the values in the answer
		    beforeAnswer = answer;
		    HashSet<Integer> afterAnswer;
		    // Iterate through all the sequential groups
		    for (int i = 0; i < group.members.size(); i++) {
			// We need to pass through the answers of last group to
			// new group
			afterAnswer = checkGroup(beforeAnswer, (Particle) group.members.elementAt(i), node);
			beforeAnswer = afterAnswer;
		    }
		    // If new values are a subGroup of the old ones we have no
		    // new values, stop
		} while (!hashSetContainsSecond(answer, beforeAnswer));
	    }
	    break;
	// If we have a list of choices, we can choose only one of those sub
	// elements
	case Particle.TYPE_CHOICE:
	    Group group2 = (Group) particle;
	    // Check the subGroup for first time
	    for (int i = 0; i < group2.members.size(); i++) {
		answer.addAll(checkGroup(values, (Particle) group2.members.elementAt(i), node));
	    }
	    // If group is not required we add original values
	    if (!group2.isRequired)
		answer.addAll(values);
	    // If group is repeatable
	    if (group2.isRepeatable) {
		// Variable for checking if another round gives new variables
		HashSet<Integer> beforeAnswer = new HashSet<Integer>();
		beforeAnswer.addAll(answer);
		// Repeat while we have new values each round
		do {
		    // Add last round values
		    answer.addAll(beforeAnswer);
		    beforeAnswer = new HashSet<Integer>();
		    // Iterate over all elements in the choice
		    for (int i = 0; i < group2.members.size(); i++) {
			beforeAnswer.addAll(checkGroup(answer, (Particle) group2.members.elementAt(i), node));
		    }
		    // If new values are a subGroup of the old ones we have no
		    // new values, stop
		} while (!hashSetContainsSecond(answer, beforeAnswer));
	    }
	    break;
	// If no type of group is defined (this is an error) but answer will be
	// empty, so the error is implicit.
	default:
	    break;
	}
	// We return the answer generated through code
	return answer;
    }

    /**
     * Checks if set2 is contained in set1.
     * <p>
     * Used to go through loops avoiding infinite ones
     * </p>
     * 
     * @param set1
     *            Set that must have all values of set2
     * @param set2
     *            Set that must be contained in set1
     * @return True if set2 is contained in set1
     */
    private boolean hashSetContainsSecond(HashSet<Integer> set1, HashSet<Integer> set2) {
	// Temporal hashSet
	HashSet<Integer> tempSet = new HashSet<Integer>();
	// We add both sets to the temporal one
	tempSet.addAll(set1);
	tempSet.addAll(set2);
	// If adding both sets does not have new elements in respect of the
	// first set then set2 is a subset of set 1
	if (tempSet.size() == set1.size()) {
	    return true;
	}
	// Groups differ
	return false;
    }

    /**
     * Check a single label in multiple abstract syntax trees and develops all the possible trees this label allows.
     * <p>
     * If the formal language of the node allows repetition of this single label, all the possible trees are returned.
     * </p>
     * 
     * @param node
     *            Label node of the textStruct to compare
     * @param positions
     *            Possible positions at which the label can start
     * @param ref
     *            Label to use as reference
     * @return HashSet of possible positions having parsed this element
     */
    private HashSet<Integer> ElementCheck(TextNode node, HashSet<Integer> positions, Reference ref) {

	// Dummy values are added for supporting complex DTD schemas, they are
	// not useful, so are not taken in account.
	if (ref.elementType.name.getUniversalName().startsWith("_DUMMY_"))
	    return positions;

	// Create the array of answers
	HashSet<Integer> answer = new HashSet<Integer>();

	// Check every answer
	for (int i : positions) {
	    answer.addAll(ElementCheckUnity(node, i, ref));
	}
	return answer;
    }

    /**
     * Check a single label in a single abstract syntax tree and develops all the possible trees this label allows.
     * <p>
     * If the formal language of the node allows repetition of this single label, all the possible trees are returned.
     * </p>
     * 
     * @param node
     *            Label node of the textStruct to compare
     * @param position
     *            Position of the label in which we are
     * @param ref
     *            Label to use as reference
     * @return HashSet of possible positions having parsed this element
     */
    private HashSet<Integer> ElementCheckUnity(TextNode node, int position, Reference ref) {

	// Create the hashSet for the different options
	HashSet<Integer> answer = new HashSet<Integer>();

	// if it is not required we can use the one that we have now
	if (!ref.isRequired) {
	    answer.add(position);
	}

	// Get the name
	String labelName;
	// The position is bigger than the labels we have so is incomplete
	if (position >= node.GetInnerLabels().size()) {
	    return answer;
	} else {
	    labelName = node.GetInnerLabels().get(position).GetNode().GetLabelName();
	}

	// Expected name of the element
	String expectedName = ref.elementType.name.getUniversalName();

	// If the element is the one needed
	if (expectedName.equalsIgnoreCase(labelName)) {
	    answer.add(position + 1);
	}

	// If we can repeat the element
	if (ref.isRepeatable) {
	    int relativePosition = position + 1;
	    int maxPositon = node.GetInnerLabels().size();
	    // Variable to know if we have to go on
	    boolean goOn = true;
	    while ((relativePosition < maxPositon) && (goOn)) {
		// If the new name makes sense with the expected name
		if (node.GetInnerLabels().get(relativePosition).GetNode().GetLabelName().equalsIgnoreCase(expectedName)) {
		    relativePosition++;
		    answer.add(relativePosition);
		} else {
		    goOn = false;
		}
	    }
	}
	return answer;
    }

    /**
     * Check if we have free text in a node.
     * <p>
     * Checks all the possible text places (at the beginning ad end and between labels
     * </p>
     * 
     * @param node
     *            Node to make the check
     * @return Returns true if there is text and false if not.
     */
    private boolean IsTextInNode(TextNode node) {
	boolean textChecker = false;
	// Check the beginning text. Use trim for removing white spaces
	if (!node.GetBeginningText().trim().isEmpty()) {
	    textChecker = true;
	}
	ArrayList<TextMiddleNode> innerLabels = node.GetInnerLabels();
	for (int i = 0; i < innerLabels.size(); i++) {
	    if (!innerLabels.get(i).GetAfterText().trim().isEmpty())
		textChecker = true;
	}
	return textChecker;
    }

    /*
     * public void ExtractStructure(String name) { ElementType labelStruct = GetElement(name); if (labelStruct == null) {
     * System.out.println("No structure with name " + name + "."); return; } System.out.print(name + "\n\t");
     * 
     * if (labelStruct.content != null) { PrintGroup(labelStruct.content); System.out.println(); } else { System.out.println("Empty"); } }
     * 
     * private String PrintGroupString(Particle content) { String answer = ""; switch (content.type) { case Particle.TYPE_UNKNOWN: answer += "Unknown"; break;
     * case Particle.TYPE_ELEMENTTYPEREF: Reference reference = (Reference)content; // Dummy values are added for supporting complex DTD schemas, they are not
     * useful if ( !reference.elementType.name.getUniversalName().startsWith("_DUMMY_") ) { answer += reference.elementType.name.getUniversalName(); } break;
     * case Particle.TYPE_SEQUENCE: Group group1 = (Group)content; answer += "("; for( int i=0; i < group1.members.size() ; i ++ ){ answer +=
     * PrintGroupString((Particle)group1.members.elementAt(i)); if (i + 1 != group1.members.size()) answer += " , "; } answer += ")"; break; case
     * Particle.TYPE_CHOICE: Group group = (Group)content; answer += "["; for( int i=0; i < group.members.size() ; i ++ ){ answer +=
     * PrintGroupString((Particle)group.members.elementAt(i)); if (i + 1 != group.members.size()) answer += "|"; } answer += "]"; break; default: answer +=
     * "\nError not defined type " + content.type; break; } answer += PrintSymbol(content.isRepeatable , content.isRequired); return answer; }
     * 
     * private void PrintGroup(Particle content) { System.out.println(PrintGroupString(content)); }
     * 
     * private String PrintSymbol(boolean rep, boolean req) { if (rep == true) { if (req == true) { return "+"; } else { return "*"; } } else { if (req ==
     * false) return "?"; } return ""; }
     * 
     * public void PrintAllElementStructures(){
     * 
     * @SuppressWarnings("unchecked") Enumeration<ElementType> e = dtd.elementTypes.elements(); ElementType valor; while(e.hasMoreElements()){ valor =
     * e.nextElement(); if (valor.content != null) { if (valor.content.type == Particle.TYPE_ELEMENTTYPEREF) { //TYPE_ELEMENTTYPEREF
     * System.out.print("TYPE_ELEMENTTYPEREF\n\t"); ExtractStructure(valor.name.getUniversalName()); } } //ExtractStructure(valor.name.getUniversalName()); } }
     */

}
