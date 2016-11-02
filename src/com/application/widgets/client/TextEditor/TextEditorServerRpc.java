package com.application.widgets.client.TextEditor;

import com.application.widgets.client.TextEditorElements.AttributeSenderStruct;
import com.vaadin.shared.communication.ServerRpc;

/**
 * Text editor server rpc.
 * <p>
 * Sends data from client (->) to server.
 * </p>
 * 
 * @author Miguel Urízar Salinas
 *
 */
public interface TextEditorServerRpc extends ServerRpc {

    /**
     * Get the labels that can be inside the label defined with name value as specified in the TEI schema.
     * 
     * @param name
     *            Name of the label
     */
    public void getSubElements(String name);

    /**
     * Get attributes of a label as specified in the TEI schema.
     * 
     * @param name
     *            name of the label
     * @param id
     *            Id of the label defined in the TextStruct
     */
    public void getSubAttributes(String name, int id);

    /**
     * Adds attributes to the TextStruct
     * 
     * @param id
     *            Id of the label where to save the attributes
     * @param attributesToSend
     *            List of {@link AttributeSenderStruct} to save
     */
    public void addAttributes(int id, AttributeSenderStruct[] attributesToSend);

    /**
     * Modifies the text at te beginning of a label
     * 
     * @param innerText
     *            New text to update
     * @param id
     *            Id of the label to change the text
     */
    public void ModifyBeginningText(String innerText, int id);

    /**
     * Modifies the text at the beginning of a label without uploading
     * 
     * @param text
     *            New text to update
     * @param startingModifiedTextId
     *            Id of the label to change the text
     */
	public void ModifyBeginningTextNoChanges(String text, int startingModifiedTextId);
	
    /**
     * Modifies the text at the end of the label
     * 
     * @param innerText
     *            New text to update
     * @param id
     *            Id of the label to change the text
     */
    public void ModifyEndingText(String innerText, int id);

    /**
     * Modifies the text at the end of the label without uploading
     * 
     * @param innerText
     *            New text to update
     * @param id
     *            Id of the label to change the text
     */
    public void ModifyEndingTextNoChanges(String innerText, int id);
    
    /**
     * Removes a label
     * 
     * @param id
     *            Id of the label to remove
     */
    public void removeLabel(int id);

    /**
     * Changes a label
     * 
     * @param labelId
     *            Id of the label
     * @param newLabelName
     *            new name of the label
     */
    public void changeLabel(int labelId, String newLabelName);

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
    public void createLabel(int parentId, String newLabelName, int beginTextId, int beginTextOffset, int endTextId, int endTextOffset);
}
