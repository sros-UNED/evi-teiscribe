package com.application.components;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class to share the changes through the whole server and different clients
 * 
 * @author Miguel Urízar Salinas
 *
 */
public class Broadcaster implements Serializable {
    private static final long serialVersionUID = 2429071137890426074L;
    
    /** List to store UIs that are in file selection view */
    private static final List<BroadcastListener> fileSelectionViewListeners = new CopyOnWriteArrayList<BroadcastListener>();
    /** List of {@link FileRegistration} to store the users and their related files */
    private static final List<FileRegistration> editionViewListeners = new CopyOnWriteArrayList<FileRegistration>();

    /**
     * Registers the UI as in the file selection view to update it when necessary
     * 
     * @param listener
     *            Listener of the UI to register
     */
    public static void registerFileSelectionView(BroadcastListener listener) {
	fileSelectionViewListeners.add(listener);
    }

    /**
     * Unregisters the UI that goes out of the file selection view
     * 
     * @param listener
     *            Listener of the UI to unregister
     */
    public static void unregisterFileSelectionView(BroadcastListener listener) {
	fileSelectionViewListeners.remove(listener);
    }
    
    /**
     * Registers the UI as in the edition view to update it when necessary
     * @param env Name of the environment
     * @param project Name of the project 
     * @param file Name of the file
     * @param listener Listener of the UI.
     * @return Link to the fileRegistration linked to the file.
     */
    public static FileRegistration registerEditionView(String env, String project, String file, BroadcastListener listener) {
	// Check if file is already open
	for (FileRegistration reg : editionViewListeners) {
	    if (reg.fileId.equals(env + "/" + project + "/" + file)) {
		reg.listeners.add(listener);
		return reg;
	    }
	}
	FileRegistration reg = new FileRegistration(env, project, file, listener);
	editionViewListeners.add(reg);
	return reg;
    }

    /**
     * Unregisters the UI that goes out of the edition view.
     * 
     * @param projectFile
     *            Name of the UI identifier to unregister.
     * @param listener
     *            Listener to unregister.
     */
    public static void unregisterEditionView(String projectFile, BroadcastListener listener) {
	if (projectFile == null) {
	    return;
	}
	// Check if file is already open
	for (FileRegistration reg : editionViewListeners) {
	    if (reg.fileId.equals(projectFile)) {
		// Remove the listener
		reg.listeners.remove(listener);
		// If listeners empty, file not open, so delete it.
		if (reg.listeners.isEmpty()) {
		    editionViewListeners.remove(reg);
		}
	    }
	}
    }

    /**
     * When changed project, update all the UIs that are in the file selection view
     * 
     * @param message
     *            Message to send to the UIs
     */
    public static void changeProject(String envId, String message) {
	for (BroadcastListener listener : fileSelectionViewListeners) {
	    listener.changedProject(message, envId);
	}
    }

    /**
     * When changed a file, update all the UIs that are in the same project as the file in selection view
     * 
     * @param message
     *            Message to send to the UIs
     * @param envId
     *            Id of the environment to update only views with this active project
     * @param projectId
     *            Id of the project with this file active
     */
    public static void changeFile(String message, String envId, String projectId) {
	for (BroadcastListener listener : fileSelectionViewListeners) {
	    listener.changedFile(message, envId, projectId);
	}
    }

    
    /**
     * Kick all UIs that are using an specific file.
     * @param envId Name of the environment.
     * @param projectId Name of the project.
     * @param fileId Name of the file.
     * @param message Message to show the reason of kicking all UIs using this file.
     */
    public static void kickUIsOfFile(String envId, String projectId, String fileId, String message) {
	// Find the FileRegistration of the file
	for (FileRegistration reg : editionViewListeners) {
	    if (reg.fileId.equals(envId + "/" + projectId + "/" + fileId)) {
		// Remove all the listeners
		for (BroadcastListener listener : reg.listeners) {
		    listener.kickFromEditionView(message);
		}
		// Remove the file reg
		editionViewListeners.remove(reg);
	    }
	}
    }

    /**
     * Listener to send the messages and update when necessary to all the UIs
     * 
     * @author Miguel Urízar Salinas
     *
     */
    public interface BroadcastListener {

	/**
	 * Updates file selection view to match with new info of projects
	 * 
	 * @param message
	 *            Message to show in the UIs
	 */
	public void changedProject(String message, String envId);

	/**
	 * Updates file selection view to match with new info of files
	 * 
	 * @param message
	 *            Message to show in the UIs
	 * @param projectId
	 *            Id of the project for updating only if that project is active
	 */
	public void changedFile(String message, String envId, String projectId);

	/**
	 * Updates edition view
	 * 
	 * @param message
	 *            Message to show in the UIs
	 */
	public void updatedFile(String message);

	/**
	 * Something happened with the file, so the UI goes to the file selection view
	 */
	public void kickFromEditionView(String message);
    }

}