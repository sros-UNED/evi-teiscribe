package com.application.widgets.client.TextEditorElements;

import java.util.ArrayList;
import java.util.Iterator;

import com.application.widgets.client.TextEditor.TextEditorConnector;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.application.widgets.client.TextEditorElements.ClientHumanizedMessage.MESSAGE_TYPE;

/**
 * Popup menu to select a label
 * 
 * @author Miguel Urízar Salinas
 *
 */
public class LabelPopupMenu extends PopupPanel {

    /** List where all the labels go */
    MenuBar menuBar = new MenuBar(true);
    /** To send the data to the server*/
    private final TextEditorConnector parentConnector;
    /** String defining there is no label. Is necessary because the language is only server side*/
    String noLabel;
    /** Values to restruct popup window*/
    int width,height;

    /**
     * Constructor of the popup menu to select the label
     * @param connector TextEditorConnector to send the response
     * @param noLabel String defining there is no label. Is necessary because the language is only server side
     */
    public LabelPopupMenu(TextEditorConnector connector, String noLabel) {
	super(true);
	this.setStyleName("popupLabelTextEditor");
	parentConnector = connector;
	menuBar.setStyleName("labelMenuBar");
	menuBar.setWidth("100%");
	this.setWidget(menuBar);
	this.noLabel = noLabel;
    }

    /**
     * Populates and shows the popup menu
     * @param labels List of labels to show
     */
    public void createLabelsPopupMenu(ArrayList<String> labels) {
	menuBar.clearItems();
	if (labels == null) {
	    ClientHumanizedMessage.showMessageAndFadeAfterMouseMove(noLabel, MESSAGE_TYPE.INFO);
	    return;
	}
	int numberOfLabels = addAllSubLabelsToPopupMenu(menuBar, labels);
	// Extract the real height
	if (numberOfLabels > 8) numberOfLabels = 8;
	int finalHeigth = numberOfLabels*21 + 4;
	// Show the popup where it must.
	if (height < (Window.getClientHeight() - finalHeigth)) {
	    finalHeigth = height;
	} else {
	    finalHeigth = height - finalHeigth;
	}
	super.setPopupPosition(width, finalHeigth);
	this.show();
    }

    /**
     * Adds the labels to the menu bar
     * @param menuBar Where to add the labels
     * @param labels list of labels to add
     * @return Number of labels added
     */
    private int addAllSubLabelsToPopupMenu(MenuBar menuBar, ArrayList<String> labels) {
    int i = 0;
	Iterator<String> iterator = labels.iterator();
	while (iterator.hasNext()) {
	    String elemento = iterator.next();
	    addLabelPopupSelector(menuBar, elemento);
	    i++;
	}
	return i;
    }

    /**
     * Function that generates a menuItem with its command to act as desired
     * 
     * @param menuBar
     *            MenuBar where the item will be added.
     * @param label
     *            String with the name the item will show.
     */
    private void addLabelPopupSelector(MenuBar menuBar, final String label) {

	/**
	 * Command that generates the new label and changes the structure
	 */
	MenuItem item = new MenuItem(label, true, new Command() {
	    @Override
	    public void execute() {
		parentConnector.popupLabelMenu.hide();
		parentConnector.createLabel(label);
	    }
	});
	item.addStyleName("popupItemTextEditor");
	menuBar.addItem(item);
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
    this.width = width;
    this.height = heigth;
    }

}
