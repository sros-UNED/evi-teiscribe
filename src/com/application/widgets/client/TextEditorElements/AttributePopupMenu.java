package com.application.widgets.client.TextEditorElements;

import java.util.ArrayList;
import java.util.Iterator;

import com.application.widgets.client.TextEditor.TextEditorConnector;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

import static com.application.components.Constants.LABELATTRIBUTEPREFIX;

/**
 * Menu that shows the attributes of a label and also allows to delete and change it.
 * 
 * @author Miguel Urízar Salinas
 *
 */
public class AttributePopupMenu extends PopupPanel {

    /** Panel where all attributes are shown */
    FlexTable attributePanel;
    /** Panel where the title and header is shown */
    FlowPanel titlePanel;
    /** Labels at the header of the window */
    InlineLabel title, close;
    /** Buttons to interact with the menu */
    Button noSaveBottom, saveBottom, deleteLabelButton;
    /** Strings with the language*/
    String errorAttribute, noAttribute;
    /** id of the label*/
    int id;
    /** Flag to know if there are no attributes*/
    boolean noAttributes = false;
    /** Label represented as an Element in the DOM */
    Element labelElement;
    /** Handler for deleting label*/
    HandlerRegistration deleteLabelHandler;
    /** Connector to the main window to update changes*/
    private final TextEditorConnector parentConnector;

    /**
     * Constructor for the attribute popup menu
     * 
     * @param connector
     *            TextEditorConnector to send the response
     * @param noSave
     *            String defining label of no save button. Is necessary because the language is only server side.
     * @param save String defining label of save button. Is necessary because the language is only server side.
     * @param error String defining error message. Is necessary because the language is only server side.
     * @param noAttribute String defining no attribute message. Is necessary because the language is only server side.
     * @param deleteLabel String defining delete label message. Is necessary because the language is only server side.
     */
    public AttributePopupMenu(TextEditorConnector connector, String noSave, String save, String error, String noAttribute, String deleteLabel) {
	super(true);

	parentConnector = connector;

	errorAttribute = error;
	errorAttribute = noAttribute;

	setStyleName("popupAttributesTextEditor");

	titlePanel = new FlowPanel();
	titlePanel.setStyleName("TitleAttributePopupPanel");
	this.setWidget(titlePanel);

	close = new InlineLabel("x");
	close.setStyleName("CloseTitleAttributePopupPanel");
	close.addClickHandler(new ClickHandler() {
	    @Override
	    public void onClick(ClickEvent event) {
		hide();
	    }
	});

	deleteLabelButton = new Button(deleteLabel);
	deleteLabelButton.setStyleName("DeleteLabelAttributePopupPanel");

	noSaveBottom = new Button(noSave);
	noSaveBottom.setStyleName("CancelTitleAttributePopupPanel");
	noSaveBottom.addClickHandler(new ClickHandler() {
	    @Override
	    public void onClick(ClickEvent event) {
		hide();
	    }
	});

	saveBottom = new Button(save);
	saveBottom.setStyleName("SaveTitleAttributePopupPanel");
	saveBottom.addClickHandler(new ClickHandler() {
	    @Override
	    public void onClick(ClickEvent event) {
		updateAttributes();
		hide();
	    }
	});
    }

    /**
     * Creates the structure of the attributes PopupMenu and shows it
     * 
     * @param attributes
     *            Attributes from the server to show
     * @param id
     *            Id of the label to know where to update attributes
     * @param name
     *            Name of the label to show
     */
    public void createAttributesPopupMenu(ArrayList<String> attributes, final int id, String name) {
	// Extract the element
	this.id = id;
	labelElement = DOM.getElementById(Integer.toString(id));
	if (labelElement == null) {
	    title = new InlineLabel("Error, no element");
	} else {
	    title = new InlineLabel(name);
	}
	// Add click handler for delete label
	if (deleteLabelHandler != null) {
	    deleteLabelHandler.removeHandler();
	}
	deleteLabelHandler = deleteLabelButton.addClickHandler(new ClickHandler() {
	    @Override
	    public void onClick(ClickEvent event) {
		parentConnector.deleteLabel(id);
		hide();
	    }
	});
	title.setStyleName("TitleNameTitleAttributePopupPanel");
	title.addClickHandler(new ClickHandler() {
	    @Override
	    public void onClick(ClickEvent event) {
		// It has a parent Element
		String parentLabel = labelElement.getParentElement().getAttribute("label");
		if (parentLabel != "") {
		    parentConnector.setAddLabelId(id);
		    parentConnector.changeLabel = true;
		    parentConnector.createLabelPopupMenu(parentLabel, event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
		}
	    }
	});

	titlePanel.clear();
	titlePanel.add(title);
	titlePanel.add(close);
	titlePanel.add(new HTML("<hr class=\"hrTitleAttributePopupPanel\" />"));

	titlePanel.add(deleteLabelButton);
	titlePanel.add(new HTML("<hr class=\"hrTitleAttributePopupPanel\" />"));

	addAllAttributesToPopupMenu(attributes);

	titlePanel.add(attributePanel);
	titlePanel.add(new HTML("<hr class=\"hrTitleAttributePopupPanel\" />"));

	titlePanel.add(noSaveBottom);
	titlePanel.add(saveBottom);

	this.show();
    }

    /**
     * Creates the window upwards or downwards depending on where the mouse is placed.
     * 
     * @param heigth
     *            Actual height of the mouse.
     * @param width
     *            Actual width of the mouse.
     */
    public void setPopupVariablePositon(int heigth, int width) {
	int finalHeigth;
	if (heigth < (Window.getClientHeight() - 300)) {
	    finalHeigth = heigth;
	} else {
	    finalHeigth = heigth - 290;
	}

	super.setPopupPosition(width, finalHeigth);
    }

    /**
     * Populates the attribute panel with the attributes that already have values and also from the server
     * @param attributes Attributes to add
     */
    private void addAllAttributesToPopupMenu(ArrayList<String> attributes) {

	attributePanel = new FlexTable();
	attributePanel.setStyleName("attributesTableAttributePopupPanel");
	int i = 0;
	final JsArray<Node> existingAttributes = getAttributes(labelElement);

	// Introduce the attributes that already exist
	for (int j = 0; j < existingAttributes.length(); j++) {
	    final Node node = existingAttributes.get(j);
	    String attributeName = node.getNodeName();
	    if (attributeName.toLowerCase().startsWith(LABELATTRIBUTEPREFIX)) {
		addAttributeToPopupSelector(i, attributeName.substring(LABELATTRIBUTEPREFIX.length()), node.getNodeValue());
		i++;
	    }
	}
	// We don't have attributes from server
	if (attributes == null) {
	    if (i == 0) {
		noAttributes = true;
		Label error = new Label(errorAttribute);
		error.setStyleName("errorAttributesAttributePopupPanel");
		attributePanel.setWidget(0, 0, error);
	    }
	    // We have attributes from server
	} else {
	    // Go through all the attributes
	    Iterator<String> iterator = attributes.iterator();
	    while (iterator.hasNext()) {
		String elemento = iterator.next();
		// Check if attribute is already inserted
		Boolean siEstadentro = checkIfAttributeAlreadyIn(elemento);
		if (siEstadentro == false) {
		    addAttributeToPopupSelector(i, elemento, "");
		    i++;
		}
	    }
	    if (i == 0) {
		noAttributes = true;
		Label error = new Label(noAttribute);
		error.setStyleName("errorAttributesAttributePopupPanel");
	    }
	}
    }

    /**
     * Checks if an attribute is already placed in the panel
     * 
     * @param attribute
     *            Attribute name.
     * @return true if attribute is already placed and false if the attribute is not inside the panel
     */
    private boolean checkIfAttributeAlreadyIn(String attribute) {

	int rows = attributePanel.getRowCount();
	for (int i = 0; i < rows; i++) {
	    Label attributeName = (Label) attributePanel.getWidget(i, 0);
	    if (attributeName.getText().compareToIgnoreCase(attribute) == 0)
		return true;
	}
	return false;
    }

    /**
     * Adds a single attribute to attribute panel
     * 
     * @param row
     *            Row where the attribute goes
     * @param attribute
     *            Attribute name
     * @param value
     *            Attribute value, can be empty
     */
    private void addAttributeToPopupSelector(int row, final String attribute, final String value) {

	Label attributeName = new Label(attribute);
	attributeName.setStyleName("attributeIndividualPanelName");
	attributePanel.setWidget(row, 0, attributeName);

	TextBox attributeTextBox = new TextBox();
	attributeTextBox.setText(value);
	attributeTextBox.setName(attribute);
	attributeTextBox.setStyleName("attributeIndividualPanelTextBox");
	attributePanel.setWidget(row, 1, attributeTextBox);
    }

    /**
     * Send the changes in attributes to the server
     */
    private void updateAttributes() {
	if (noAttributes)
	    return;
	int rows = attributePanel.getRowCount();
	AttributeSenderStruct[] attributesToSend = new AttributeSenderStruct[rows];
	for (int i = 0; i < rows; i++) {
	    Label attributeName = (Label) attributePanel.getWidget(i, 0);
	    TextBox attributeValue = (TextBox) attributePanel.getWidget(i, 1);
	    AttributeSenderStruct newAttribute = new AttributeSenderStruct();
	    newAttribute.name = attributeName.getText();
	    newAttribute.value = attributeValue.getText();
	    attributesToSend[i] = newAttribute;
	}
	parentConnector.rpc.addAttributes(id, attributesToSend);
    }

    /**
     * Support function in javascript to extract the attributes of an element
     * 
     * @param elem
     *            The element to search the attributes
     * @return An Array with all te attributes
     */
    public static native JsArray<Node> getAttributes(Element elem) /*-{
								   return elem.attributes;
								   }-*/;

}
