package com.application.components;

import static com.application.components.Constants.getXMLDBManager;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.application.components.Broadcaster.BroadcastListener;
import com.application.components.Manager.DTDManager;
import com.application.components.Manager.FileManager;
import com.application.components.TEITextStruct.TextStruct;
import com.application.language.Labels;
import com.application.texteditor.TextEditorUI;
import com.application.views.ServiceNotAvailableView;
import com.application.widgets.client.TextEditorElements.AttributeSenderStruct;

import static com.application.components.Constants.NOTATORIZEDVIEW;
import static com.application.components.Constants.TEIPROJECTNAME;
import static com.application.components.Constants.STRINGTYPE;
import static com.application.components.Constants.UNDOMAXSIZE;

/**
 * Class to store the data needed for the edition view
 * 
 * @author Miguel Ur�zar Salinas
 *
 */
public class FileRegistration implements Serializable {
    private static final long serialVersionUID = -422519211517143137L;
    /** Id of the file */
    String fileId;
    /** Name of the environment*/
    String env;
    /** Name of the project*/
    String project;
    /** Name of the file*/
    String file;
    /** File manager to define the schema */
    public FileManager manager;
    /** File struct to work with */
    public TextStruct fileStruct;
    /** List to undo and redo*/
    private undoClass undoList;
    /** UIs linked to the file */
    List<BroadcastListener> listeners;

    /**
     * Constructor for FileRegistration that also creates the listener list
     * @param env Environment of the file
     * @param project Project inside the environment
     * @param file Name of the file
     * @param listener Listener of the UI that is opening the file
     */
    public FileRegistration(String env, String project, String file, BroadcastListener listener) {
	fileId = env + "/" + project + "/" + file;
	this.env = env;
	this.project = project;
	this.file = file;
	listeners = new CopyOnWriteArrayList<BroadcastListener>();
	listeners.add(listener);
	// Load the TEI schema
	try {
	    manager = new DTDManager(new String(getXMLDBManager(TextEditorUI.getCurrent().getUser()).getBinaryFile(env,project, TEIPROJECTNAME), StandardCharsets.UTF_8),STRINGTYPE);
	} catch (Exception e) {
	    //TODO add message
	    // If not possible to open TEI structure, no structure is used
	    manager = null;
	}
	// Load TEI file
	try {
	    fileStruct = new TextStruct(  new String(getXMLDBManager(TextEditorUI.getCurrent().getUser()).getXMLFile(env,project, file), StandardCharsets.UTF_8),STRINGTYPE);
	} catch (Exception e) {
	    //TODO add message
	    // If not possible to open TEI file, a generic one is used
	    if (fileStruct == null) {
		System.out.println("No file exported");
		fileStruct = new TextStruct();
	    }
	}
	undoList = new undoClass(UNDOMAXSIZE,fileStruct);
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
    TextStruct newFileStruct = org.apache.commons.lang3.SerializationUtils.clone(fileStruct);
    fileStruct = newFileStruct;
	if (newFileStruct.AddModifyAttributes(attributesToSend, id) != 0)
	    return -1;
	saveFileAndpdateUIs(Labels.getString("attributesUpdated"));
	undoList.add(fileStruct);
	return 0;
    }

    /**
     * Changes the beginning text of the label defined by id.
     * 
     * @param innerText
     *            Text to update
     * @param id
     *            Identification number of the node to change the text
     * @return 0 if success or -1 if node does not exist
     */
    public int ModifyBeginningText(String innerText, int id) {
    TextStruct newFileStruct = org.apache.commons.lang3.SerializationUtils.clone(fileStruct);
    fileStruct = newFileStruct;
	if (fileStruct.ModifyBeginningText(innerText, id) != 0)
	    return -1;
	saveFileAndpdateUIs(Labels.getString("textModified"));
	undoList.add(fileStruct);
	newFileStruct = null;
	return 0;
    }
    
    /**
     * Changes the beginning text of the label defined by id without changing the UIs.
     * 
     * @param innerText
     *            Text to update
     * @param id
     *            Identification number of the node to change the text
     * @return 0 if success or -1 if node does not exist
     */
    public int ModifyBeginningTextNoChanges(String innerText, int id) {
    TextStruct newFileStruct = org.apache.commons.lang3.SerializationUtils.clone(fileStruct);
    fileStruct = newFileStruct;
	if (fileStruct.ModifyBeginningText(innerText, id) != 0)
	    return -1;
	newFileStruct = null;
	return 0;
    }

    /**
     * Changes the ending text of the label defined by id.
     * 
     * @param innerText
     *            Text to update
     * @param id
     *            Identification number of the node to change the text
     * @return 0 if success or -1 if node does not exist
     */
    public int ModifyEndingText(String innerText, int id) {
    TextStruct newFileStruct = org.apache.commons.lang3.SerializationUtils.clone(fileStruct);
    fileStruct = newFileStruct;
	if (fileStruct.ModifyEndingText(innerText, id) != 0)
	    return -1;
	saveFileAndpdateUIs(Labels.getString("textModified"));
	undoList.add(fileStruct);
	newFileStruct = null;
	return 0;
    }
    
    /**
     * Changes the ending text of the label defined by id without changing the UIs.
     * 
     * @param innerText
     *            Text to update
     * @param id
     *            Identification number of the node to change the text
     * @return 0 if success or -1 if node does not exist
     */
    public int ModifyEndingTextNoChanges(String innerText, int id) {
    TextStruct newFileStruct = org.apache.commons.lang3.SerializationUtils.clone(fileStruct);
       fileStruct = newFileStruct;
	if (fileStruct.ModifyEndingText(innerText, id) != 0)
	    return -1;
	newFileStruct = null;
	return 0;
    }

    /**
     * Removes the label with selected id.
     * 
     * @param id
     *            Identification number for the label to remove
     * @return 0 if success or -1 if node does not exist
     */
    public int removeLabel(int id) {
    TextStruct newFileStruct = org.apache.commons.lang3.SerializationUtils.clone(fileStruct);
    fileStruct = newFileStruct;
	if (fileStruct.removeLabel(id) != 0)
	    return -1;
	saveFileAndpdateUIs(Labels.getString("labelRemoved"));
	undoList.add(fileStruct);
	return 0;
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
    TextStruct newFileStruct = org.apache.commons.lang3.SerializationUtils.clone(fileStruct);
    fileStruct = newFileStruct;
	if (fileStruct.changeLabel(labelId, newLabelName) != 0)
	    return -1;
	saveFileAndpdateUIs(Labels.getString("labelChanged"));
	undoList.add(fileStruct);
	return 0;
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
    TextStruct newFileStruct = org.apache.commons.lang3.SerializationUtils.clone(fileStruct);
    fileStruct = newFileStruct;
	if (fileStruct.createLabel(parentId, newLabelName, beginTextId, beginTextOffset, endTextId, endTextOffset) != 0)
	    return -1;
	saveFileAndpdateUIs(Labels.getString("labelCreated"));
	undoList.add(fileStruct);
	return 0;
    }

    /**
     * Redo the next change
     */
    public void redo() {
    	fileStruct = undoList.redo();
    	saveFileAndpdateUIs(Labels.getString("redoFile"));
    }
    /**
     * Undo the last change
     */
    public void undo() {
    	fileStruct = undoList.undo();
    	saveFileAndpdateUIs(Labels.getString("undoFile"));
    }
    
    /**
     * Saves file and updates the UIs that have the file opened.
     * 
     * @param message
     *            Message to send to the UIs that have the file opened
     */
    private void saveFileAndpdateUIs(String message) {
	// Save file
	saveFile();
	// Update UIs
	for (BroadcastListener listener : listeners) {
	    listener.updatedFile(message);
	}
    }
    
    /**
     * Saves file
     */
    private void saveFile() {
	// Save file
	try {
		getXMLDBManager(TextEditorUI.getCurrent().getUser()).addXMLFile(env,project,file,fileStruct.GenerateInnerXML().getBytes(StandardCharsets.UTF_8));
	} catch (Exception e) {
	    //TODO add message
		TextEditorUI.getCurrent().updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
	}
    }

    //TODO add javadoc
    public boolean canUndo() {
    	return undoList.canUndo();
    }

    //TODO add javadoc
    public boolean canRedo() {
    	return undoList.canRedo();
    }

}