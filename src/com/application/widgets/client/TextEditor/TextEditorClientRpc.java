package com.application.widgets.client.TextEditor;

import java.util.ArrayList;

import com.vaadin.shared.communication.ClientRpc;

/**
 * Text editor client rpc.
 * <p>
 * Sends data from server (->) to client.
 * </p>
 * 
 * @author Miguel Urízar Salinas
 *
 */
public interface TextEditorClientRpc extends ClientRpc {
	public static final long serialVersionUID = -234598345981234L;

    /**
     * Alerts the client with the defined message
     * 
     * @param message
     *            Message to send to client
     */
    public void alert(String message);

    /**
     * Creates the pop up menu for selecting a label
     * 
     * @param labels
     *            List of the names of labels
     */
    public void createLabelsPopupMenu(ArrayList<String> labels);

    /**
     * Creates the pop up menu for modifying attributes and characteristics of the label
     * 
     * @param attributes
     *            List of attribute names defined in the TEI schema
     * @param id
     *            Id of the label
     * @param name
     *            Name of the label
     */
    public void createAttributesPopupMenu(ArrayList<String> attributes, int id, String name);

    /**
     * Modifies the inner HTML text in the text editor
     * 
     * @param innerHTML
     *            New HTML to show
     */
    public void modifyTextHTML(String innerHTML);
    
    /**
     * Sets the cursor position
     */
    public void setCursorPosition();
    
    /**
     * Gets or selects the cursor position
     */
    public void getCursorPosition();
}