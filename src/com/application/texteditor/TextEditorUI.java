package com.application.texteditor;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.application.components.Broadcaster;
import com.application.components.Broadcaster.BroadcastListener;
import com.application.components.Jwt;
import com.application.components.LogFormatter;
import com.application.components.XMLDBManager;
import com.application.language.Labels;
import com.application.views.EditionView;
import com.application.views.FileSelectionView;
import com.application.views.NotAutorizedView;
import com.application.views.ServiceNotAvailableView;

import static com.application.components.Constants.FILESELECTIONVIEW;
import static com.application.components.Constants.EDITIONVIEW;
import static com.application.components.Constants.NOTATORIZEDVIEW;
import static com.application.components.Constants.DEMOSESSION;
import static com.application.components.Constants.EXISTUSER;
import static com.application.components.Constants.EXISTPASS;
import static com.application.components.Constants.SECRET;
import static com.application.components.Constants.TEIDATABASEFOLDER;
import static com.application.components.Constants.LOGGERFILE;

import static com.application.components.Constants.getXMLDBManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class of the server.
 * <p>
 * New instance is generated when a client asks for the web page.
 * </p>
 * 
 * @author Miguel Urízar Salinas
 *
 */
@Push
@Theme("texteditortheme")
@JavaScript("vaadin://js/javascript.js")
public class TextEditorUI extends UI implements BroadcastListener {

    private static final long serialVersionUID = 1L;

    private int UIStatus;
    private CustomComponent view = null;
    private String fileId;
    private String environment = DEMOSESSION;
    private String user = EXISTUSER;
    private String pass = EXISTPASS;
    private String collection = TEIDATABASEFOLDER;

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = true, ui = TextEditorUI.class, widgetset = "com.application.widgets.TexteditorWidgetset")
    public static class Servlet extends VaadinServlet {
	private static final long serialVersionUID = 5437747138355481373L;
    }

    /**
     * Initiates all the resources when a new client asks for a page.
     */
    @Override
    protected void init(VaadinRequest request) {

	/** Check the session */
	String name = request.getParameter("session");
	if (name == null) {
	    logMessage(Level.SEVERE, "Error, no session defined.", false);
	    updateView(new NotAutorizedView(), NOTATORIZEDVIEW, null);
	    return;
	} else {
	    if (!name.equals(DEMOSESSION)) {
		Jwt credentials = new Jwt();
		boolean a = credentials.parseJWT(name, SECRET);
		if (a == false) {
		    updateView(new NotAutorizedView(), NOTATORIZEDVIEW, null);
		    return;
		}
		collection = credentials.collection;
		environment = TEIDATABASEFOLDER;
		user = credentials.user;
		pass = credentials.pass;
	    }
	}
	// Register for a persistent session a XMLDBManager if does not exist
	if (getXMLDBManager(user) == null) {
	    try {
		VaadinServlet.getCurrent().getServletContext().setAttribute("XMLdataBaseManager" + user, new XMLDBManager(collection, user, pass));
	    } catch (Exception e) {
		logMessage(Level.SEVERE, "Error. Could not create XMDBManager. "+e.getMessage(), false);
		updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
		return;
	    }
	}
	try {
	    getXMLDBManager(user).createEnv(environment);
	} catch (Exception e) {
	    logMessage(Level.SEVERE, "Error. Could not create environment " + environment + ". ", false);
	    e.printStackTrace();
	    updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
	    return;
	}
	// Title of the web page
	getPage().setTitle("TEIScribe");
	// Create login view
	updateView(new FileSelectionView(), FILESELECTIONVIEW, null);
    }

    /**
     * Removes the thread from the BroadCaster informs and closes to the login screen
     */
    @Override
    public void detach() {
	super.detach();
	if (UIStatus == FILESELECTIONVIEW) {
	    Broadcaster.unregisterFileSelectionView(this);
	}
	if (UIStatus == EDITIONVIEW) {
	    Broadcaster.unregisterEditionView(fileId, this);
	}
    }

    /**
     * Message sent when a project is changed in the DataBase.
     * <p>
     * Is only sent when the user is in the fileSelectionView
     * </p>
     */
    public void changedProject(final String message, final String env) {
	access(new Runnable() {
	    @Override
	    public void run() {
		if (environment.equals(env)) {
		    ((FileSelectionView) getCurrent().view).updateProjectsLayout();
		    Notification.show(Labels.getString("changedDB"), message, Type.TRAY_NOTIFICATION);
		}
	    }
	});
    }

    /**
     * Message sent when a file is changed in the DataBase.
     * <p>
     * Is only sent when the user is in the fileSelectionView
     * </p>
     */
    public void changedFile(final String message, final String env, final String projectId) {
	access(new Runnable() {
	    @Override
	    public void run() {
		if (environment.equals(env)) {
		    boolean updated = ((FileSelectionView) getCurrent().view).updatedFile(projectId);
		    if (updated) {
			Notification.show(Labels.getString("changedDB"), message, Type.TRAY_NOTIFICATION);
		    }
		}
	    }
	});
    }

    /**
     * Updates edition view
     * 
     * @param message
     *            Message to show in the UIs
     */
    public void updatedFile(final String message) {
	access(new Runnable() {
	    @Override
	    public void run() {
		((EditionView) getCurrent().view).updateView(message);
	    }
	});
    }

    /**
     * Something happened with the file, so the UI goes to the file selection view
     */
    public void kickFromEditionView(final String message) {
	access(new Runnable() {
	    @Override
	    public void run() {
		((EditionView) getCurrent().view).exitView(message);
	    }
	});
    }

    /**
     * Get the personal UI
     * 
     * @return Current UI
     */
    public static TextEditorUI getCurrent() {
	return (TextEditorUI) UI.getCurrent();
    }

    /**
     * Sets the UIStatus and also detaches or attaches the listener in the different broadcasters
     * 
     * @param uIStatus
     *            the uIStatus to set
     * @param id
     *            id of the file linked to the UI. -1 if we don't enter in EditionView
     */
    public void setUIStatus(int uIStatus, String id) {
	if (UIStatus == FILESELECTIONVIEW) {
	    // As leaving file selection view, remove listener from file selection view broadcaster
	    Broadcaster.unregisterFileSelectionView(this);
	}
	if (UIStatus == EDITIONVIEW) {
	    Broadcaster.unregisterEditionView(fileId, this);
	}
	UIStatus = uIStatus;
	if (UIStatus == FILESELECTIONVIEW) {
	    // As entering file selection view, add listener from file selection view broadcaster
	    Broadcaster.registerFileSelectionView(this);
	}
	fileId = id;
    }

    /**
     * Updates the data that is shown in the UI
     * 
     * @param newView
     *            Component to attach to the UI
     * @param type
     *            Type of component attached. Used for updating the broadcaster
     * @param id
     *            id of the file linked to the UI. -1 if we don't enter in EditionView
     */
    public void updateView(CustomComponent newView, int type, String id) {
	view = newView;
	setUIStatus(type, id);
	this.setContent(view);
    }

    /**
     * Getter for environment name
     * 
     * @return environment name
     */
    public String getEnvironment() {
	return environment;
    }

    /**
     * Getter for user name
     * 
     * @return user name
     */
    public String getUser() {
	return user;
    }

    /**
     * Getter for collection name
     * 
     * @return collection name
     */
    public String getCollection() {
	return collection;
    }

    /**
     * Replaces invalid characters of a string
     * 
     * @param input
     *            String to delete the characters
     * @return the string with the characters replaced and deleted
     */
    public String clearString(String input) {
	input = input.trim();
	input = input.replace('á', 'a').replace('à', 'a').replace('ä', 'a').replace('â', 'a').replace('ª', 'a').replace('Á', 'A').replace('À', 'A')
		.replace('Â', 'A').replace('Ä', 'A');
	input = input.replace('é', 'e').replace('è', 'e').replace('ë', 'e').replace('ê', 'e').replace('É', 'E').replace('È', 'E').replace('Ê', 'E').replace('Ë',
		'E');
	input = input.replace('í', 'i').replace('ì', 'i').replace('ï', 'i').replace('î', 'i').replace('Í', 'I').replace('Ì', 'I').replace('Î', 'I').replace('Ï',
		'I');
	input = input.replace('ó', 'o').replace('ò', 'o').replace('ö', 'o').replace('ô', 'o').replace('Ó', 'O').replace('Ò', 'O').replace('Ô', 'O').replace('Ö',
		'O');
	input = input.replace('ú', 'u').replace('ù', 'u').replace('ü', 'u').replace('û', 'u').replace('Ú', 'U').replace('Ù', 'U').replace('Û', 'U').replace('Ü',
		'U');
	input = input.replace('ç', 'c').replace('Ç', 'C').replace('ñ', 'n').replace('Ñ', 'N');
	input = input.replaceAll("[¨|~|#|@|!|\\\\|\\||·|$|%|&|/|(|)|?|'|¡|¿|\\[|^|\\]|+|}|{|¨|´|>|<|;|,|:|\\s]", "");
	return input;
    }

    /**
     * Creates an output message in the log file creating the log and after closing it and adding the date when was thrown.
     * 
     * @param level
     *            Level of the message
     * @param message
     *            text to show
     * @param inside
     *            True if we are inside the project and false if the error is grabbing credentials
     */
    public void logMessage(Level level, String message, Boolean inside) {
	Logger logger = Logger.getLogger("TeiScribe");
	FileHandler fh = null;
	try {
	    // This block configure the logger with handler and formatter
	    fh = new FileHandler(LOGGERFILE, true);
	    logger.addHandler(fh);
	    fh.setFormatter(new LogFormatter());
	    logger = Logger.getLogger("TeiScribe");
	    String outputMessage = "";
	    if (inside == true) {
		outputMessage = outputMessage + "[" + collection + "] ";
	    }
	    outputMessage = outputMessage + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss,SSS").format(new Date()) + " " + level.getName() + " " + message;
	    logger.log(level, outputMessage);
	    logger.removeHandler(fh);
	    fh.close();
	} catch (SecurityException ex) {
	    ex.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
    }

}