package com.application.widgets;

import java.util.logging.Level;

import com.application.components.FileRegistration;
import com.application.language.Labels;
import com.application.texteditor.TextEditorUI;
import com.application.widgets.client.TextEditor.TextEditorClientRpc;
import com.application.widgets.client.TextEditor.TextEditorServerRpc;
import com.application.widgets.client.TextEditor.TextEditorState;
import com.application.widgets.client.TextEditorElements.AttributeSenderStruct;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

/**
 * Text Editor component.
 * <p>
 * Is a self-managed widget
 * </p>
 * 
 * @author Miguel Ur�zar Salinas
 *
 */
public class TextEditor extends com.vaadin.ui.AbstractComponent {
    private static final long serialVersionUID = -4562825754663097028L;

    /** The editor contains the whole TEI */
    public static final int TYPE_TEI = 0;

    /** The editor contains only the header */
    public static final int TYPE_HEADER = 1;

    /** The editor contains only the body */
    public static final int TYPE_TEXT = 2;

    /** The editor contains only the body */
    public static final int TYPE_XML = 3;

    /** Manager of the text file */
    private FileRegistration fileReg;

    /** Defines the type of text to show (header, body, all or xml) */
    public int typeOfText = 0;

    /**
     * Server rpc that is in charge of sending data from client to server
     */
    private TextEditorServerRpc rpc = new TextEditorServerRpc() {
	private static final long serialVersionUID = -6327578574297039944L;

	@Override
	public void getSubElements(String name) {
	    sendSublabels(name);
	}

	@Override
	public void getSubAttributes(String name, int id) {
	    sendAttributes(name, id);
	}

	@Override
	public void addAttributes(int id, AttributeSenderStruct[] attributesToSend) {
	    // Update attributes
	    if (fileReg.AddModifyAttributes(attributesToSend, id) != 0) {
		Notification.show(Labels.getString("problemUpdatingAttributes"), Type.ERROR_MESSAGE);
	    }
	}

	@Override
	public void ModifyBeginningText(String innerText, int id) {
	    // Modify beginning text
	    if (fileReg.ModifyBeginningText(innerText, id) != 0) {
		Notification.show(Labels.getString("problemModifyingText"), Type.ERROR_MESSAGE);
	    }
	}

	@Override
	public void ModifyEndingText(String innerText, int id) {
	    // Modify ending text
	    if (fileReg.ModifyEndingText(innerText, id) != 0) {
		Notification.show(Labels.getString("problemModifyingText"), Type.ERROR_MESSAGE);
	    }
	}

	@Override
	public void ModifyBeginningTextNoChanges(String innerText, int id) {
	    // Modify beginning text
	    if (fileReg.ModifyBeginningTextNoChanges(innerText, id) != 0) {
		Notification.show(Labels.getString("problemModifyingText"), Type.ERROR_MESSAGE);
	    }
	}

	@Override
	public void ModifyEndingTextNoChanges(String innerText, int id) {
	    // Modify ending text
	    if (fileReg.ModifyEndingTextNoChanges(innerText, id) != 0) {
		Notification.show(Labels.getString("problemModifyingText"), Type.ERROR_MESSAGE);
	    }
	}

	@Override
	public void removeLabel(int id) {
	    // Remove label
	    if (fileReg.removeLabel(id) != 0) {
		Notification.show(Labels.getString("problemRemovingLabel"), Type.ERROR_MESSAGE);
	    }
	}

	@Override
	public void changeLabel(int labelId, String newLabelName) {
	    // Change label
	    if (fileReg.changeLabel(labelId, newLabelName) != 0) {
		Notification.show(Labels.getString("problemChangingLabel"), Type.ERROR_MESSAGE);
	    }
	}

	@Override
	public void createLabel(int parentId, String newLabelName, int beginTextId, int beginTextOffset, int endTextId, int endTextOffset) {
	    // Create label
	    if (fileReg.createLabel(parentId, newLabelName, beginTextId, beginTextOffset, endTextId, endTextOffset) != 0) {
		Notification.show(Labels.getString("problemCreatingLabel"), Type.ERROR_MESSAGE);
	    }

	}

    };

    /**
     * Constructor for the textEditor
     * 
     * @param reg
     * @param typeText
     */
    public TextEditor(FileRegistration reg, int typeText) {

	fileReg = reg;
	this.setId("TextEditor");

	if ((typeText < 3) && (typeText > 0))
	    typeOfText = typeText;

	getState().noSavePopupAttribute = Labels.getString("noSavePopupAttribute");
	getState().savePopupAttribute = Labels.getString("savePopupAttribute");
	getState().errorAtributesPopupAttribute = Labels.getString("errorAtributesPopupAttribute");
	getState().noAtributesPopupAttribute = Labels.getString("noAtributesPopupAttribute");
	getState().noLabelsPopupLabels = Labels.getString("noLabelsPopupLabels");
	getState().deleteLabelPopupAttribute = Labels.getString("deleteLabelPopupAttribute");
	getState().badSelectionForLabelPopup = Labels.getString("badSelectionForLabelPopup");
	getState().lostCursor = Labels.getString("lostCursor");

	registerRpc(rpc);
	modifyText("", true);
    }

    /**
     * RPC function to send labels that can be child of the label identified by the name inserted
     * 
     * @param name
     *            Name of the label.
     */
    protected void sendSublabels(String name) {
	if (fileReg.manager != null) {
	    getRpcProxy(TextEditorClientRpc.class).createLabelsPopupMenu(fileReg.manager.ExtractElements(name));
	} else {
	    getRpcProxy(TextEditorClientRpc.class).createLabelsPopupMenu(null);
	}
    }

    /**
     * RPC function to send attributes that can be inside of the label identified by the name inserted
     * 
     * @param name
     *            Name of the label.
     */
    protected void sendAttributes(String name, int id) {
	if (fileReg.manager != null) {
	    try {
		getRpcProxy(TextEditorClientRpc.class).createAttributesPopupMenu(fileReg.manager.ExtractAttributeNames(name), id, name);
	    } catch (Exception e) {
		TextEditorUI.getCurrent().logMessage(Level.SEVERE, "Error creating attributes popup menu. " + e.getMessage() ,true);
		getRpcProxy(TextEditorClientRpc.class).createAttributesPopupMenu(null, id, name);
	    }
	} else {
	    getRpcProxy(TextEditorClientRpc.class).createAttributesPopupMenu(null, id, name);
	}
    }

    @Override
    public TextEditorState getState() {
	return (TextEditorState) super.getState();
    }

    /**
     * Method to get text updating the kind of view selected: TEI, Header or text.
     * <p>
     * Does not take in account bad formed TEI errors.
     * </p>
     * 
     * @param text
     *            String to show as notification, if empty, no notification is shown.
     * @param type
     *            Type of view selected. Can be TYPE_HEADER for only showing the Header, TYPE_TEXT for only showing text, or TYPE_TEI for showing whole file. If
     *            type of view is not selected, whole TEI file is shown.
     * 
     */
    public void modifyText(String text, int type) {
	switch (type) {
	case TYPE_HEADER:
	    getRpcProxy(TextEditorClientRpc.class).modifyTextHTML(fileReg.fileStruct.GenerateInnerTeiHeaderHTML());
	    break;
	case TYPE_TEXT:
	    getRpcProxy(TextEditorClientRpc.class).modifyTextHTML(fileReg.fileStruct.GenerateInnerBodyHTML());
	    break;
	case TYPE_XML:
	    getRpcProxy(TextEditorClientRpc.class).modifyTextHTML("<xmp>" + fileReg.fileStruct.GenerateInnerXML() + "</xmp>");
	    break;
	default:
	    getRpcProxy(TextEditorClientRpc.class).modifyTextHTML(fileReg.fileStruct.GenerateInnerHTML());
	    break;
	}
	typeOfText = type;
	if (text != "")
	    Notification.show(Labels.getString("TEIFileUpdated"), text, Type.TRAY_NOTIFICATION);
    }

    /**
     * Method to get text using the last view selected.
     * 
     * @param text
     *            String to show as notification, if empty, no notification is shown.
     * @param showError
     *            true to show error notification or false to not.
     */
    public void modifyText(String text, boolean showError) {
	int result = fileReg.fileStruct.validateXMLStructure(fileReg.manager);
	if (showError) {
	    switch (result) {
	    case -1:
		Notification.show(Labels.getString("someErrors"), Type.ERROR_MESSAGE);
		break;
	    case -2:
		Notification.show(Labels.getString("noSchema"), Type.ERROR_MESSAGE);
		break;
	    default:
		break;
	    }
	}
	// Modify text
	modifyText(text, typeOfText);
    }

    /**
     * Recovers the saved position of the cursor
     */
    public void setCursorPosition() {
	getRpcProxy(TextEditorClientRpc.class).setCursorPosition();
    }

    /**
     * Saves the position of the cursor
     */
    public void getCursorPosition() {
	getRpcProxy(TextEditorClientRpc.class).getCursorPosition();
    }
}
