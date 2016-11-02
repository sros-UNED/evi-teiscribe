package com.application.views;

import static com.application.components.Constants.FILESELECTIONVIEW;
import static com.application.components.Constants.MENUICON;
import static com.application.components.Constants.REDOICON;
import static com.application.components.Constants.UNDOICON;

import com.application.components.Broadcaster;
import com.application.components.FileRegistration;
import com.application.components.TextAreaLayout;
import com.application.language.Labels;
import com.application.texteditor.TextEditorUI;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Vaadin View for the edition of TEI files
 * 
 * @author Miguel Urízar Salinas
 *
 */
public class EditionView extends CustomComponent implements View {
    private static final long serialVersionUID = -1853286943450521502L;

    /** Main layout of the view */
    private VerticalLayout mainLayout;
    /** Tab where the different views of the file are shown */
    TextAreaLayout tabSheet;
    /** File Registration class*/
    FileRegistration reg;
    /** Names for the title screen */
    private String project;
    private String file;
    private Button undoButton;
    private Button redoButton;

    /**
     * Constructor that generates the whole view and stores the XML file and TEI struct.
     * 
     * @param project
     *            Data of the project
     * @param file
     *            Data of the file
     */
    public EditionView(String project, String file) {

	setSizeFull();
	// Attach file to global server
	reg = Broadcaster.registerEditionView(TextEditorUI.getCurrent().getEnvironment(), project, file, TextEditorUI.getCurrent());
	// Set the projects
	this.project = project;
	this.file = file;
	// Build main layout and link it
	buildMainLayout();
	
	setCompositionRoot(mainLayout);

    }

    /**
     * Creates the main layout
     * <p>
     * Creates and adds the bar layout and the edition layout
     * </p>
     * 
     * @return Vertical layout used as main layout
     */
    private VerticalLayout buildMainLayout() {
	// common part: create layout
	mainLayout = new VerticalLayout();
	mainLayout.setSizeFull();

	// barLayout
	mainLayout.addComponent(buildBarLayout());

	// editionLayout
	HorizontalLayout editionLayout = buildEditionLayout();
	mainLayout.addComponent(editionLayout);
	mainLayout.setExpandRatio(editionLayout, 1.0f);

	return mainLayout;
    }

    /**
     * Creates the bar layout.
     * <p>
     * Creates and adds a layout for the buttons and info of the view.
     * </p>
     * 
     * @return Vertical layout used as bar layout
     */
    private VerticalLayout buildBarLayout() {
	// common part: create external bar layout
	VerticalLayout barLayout = new VerticalLayout();
	barLayout.setPrimaryStyleName("text_editor_outer_menu_layout");

	HorizontalLayout innerBarLayout = new HorizontalLayout();
	innerBarLayout.setSizeUndefined();
	innerBarLayout.setPrimaryStyleName("text_editor_inner_menu_layout");
	barLayout.addComponent(innerBarLayout);

	// Add label
	Label title = new Label("\"" + TextEditorUI.getCurrent().getEnvironment() + "\" - \"" + project + "\" - \"" + file + "\"");
	title.setDescription(Labels.getString("CollectionProjectFileNameInfo"));
	title.setPrimaryStyleName("title_label");
	innerBarLayout.addComponent(title);

	// Add menu button
	addFileSelectionButton(innerBarLayout);
	addUndoButton(innerBarLayout);
	addRedoButton(innerBarLayout);
	return barLayout;
    }

    /**
     * Adds a button for going to file selection menu.
     * <p>
     * It creates a new {@link FileSelectionView}.
     * </p>
     * 
     * @param innerBarLayout
     *            Layout where to insert the button
     */
    private void addFileSelectionButton(HorizontalLayout innerBarLayout) {
	// Create button and add all the possible values
	Button buttonLogout = new Button();
	buttonLogout.setDescription(Labels.getString("GoToFileSelectionInfo"));
	buttonLogout.setIcon(new ThemeResource(MENUICON));
	buttonLogout.setPrimaryStyleName("text_editor_menu_button");
	innerBarLayout.addComponent(buttonLogout);

	// Make the button to logout
	buttonLogout.addClickListener(new Button.ClickListener() {
	    private static final long serialVersionUID = -1343278865464646439L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		// Detaches file so this UI is not uploaded anymore
		detachFile();
		// Go to new file selection view
		TextEditorUI.getCurrent().updateView(new FileSelectionView(),FILESELECTIONVIEW,null);
	    }
	});
    }
    
    /**
     * Adds a button for undoing data.
     * 
     * @param innerBarLayout
     *            Layout where to insert the button
     */
    private void addUndoButton(HorizontalLayout innerBarLayout) {
	// Create button and add all the possible values
	Button buttonUndo = new Button();
	undoButton = buttonUndo;
	boolean canUndo = reg.canUndo();
	buttonUndo.setEnabled(canUndo);
	if (canUndo) buttonUndo.setPrimaryStyleName("text_editor_menu_button");
	else buttonUndo.setPrimaryStyleName("text_editor_menu_button_disabled");
	buttonUndo.setDescription(Labels.getString("UndoInfo"));
	buttonUndo.setIcon(new ThemeResource(UNDOICON));
	innerBarLayout.addComponent(buttonUndo);

	// Make the button to undo
	buttonUndo.addClickListener(new Button.ClickListener() {
	    private static final long serialVersionUID = -1343278865464646439L;

	    @Override
	    public void buttonClick(ClickEvent event) {
	    	//Call undo
	    	reg.undo();
	    }
	});
    }
    
    /**
     * Adds a button for redoing data.
     * 
     * @param innerBarLayout
     *            Layout where to insert the button
     */
    private void addRedoButton(HorizontalLayout innerBarLayout) {
	// Create button and add all the possible values
	Button buttonRedo = new Button();
	redoButton = buttonRedo;
	boolean canRedo = reg.canRedo();
	buttonRedo.setEnabled(canRedo);
	if (canRedo) buttonRedo.setPrimaryStyleName("text_editor_menu_button");
	else buttonRedo.setPrimaryStyleName("text_editor_menu_button_disabled");
	buttonRedo.setEnabled(reg.canRedo());
	buttonRedo.setDescription(Labels.getString("RedoInfo"));
	buttonRedo.setIcon(new ThemeResource(REDOICON));
	innerBarLayout.addComponent(buttonRedo);

	// Make the button to redo
	buttonRedo.addClickListener(new Button.ClickListener() {
	    private static final long serialVersionUID = -1343278865464646439L;

	    @Override
	    public void buttonClick(ClickEvent event) {
	    	// Call redo
	    	reg.redo();
	    }
	});
    }
    

    /**
     * Creates the edition layout.
     * <p>
     * Includes a {@link TextAreaLayout} in an horizontal layout
     * </p>
     * 
     * @return HorizontalLayout with the {@link TextAreaLayout}.
     */
    private HorizontalLayout buildEditionLayout() {

	// common part: create layout
	HorizontalLayout editionLayout = new HorizontalLayout();
	editionLayout.setSizeFull();

	// Create tabsheet and add it
	tabSheet = new TextAreaLayout(reg);
	editionLayout.addComponent(tabSheet);

	return editionLayout;
    }

    /**
     * Unlinks the UI to the file updater structure
     */
    private void detachFile() {
	// Detach file from global server
	Broadcaster.unregisterEditionView(project + "/" + file,TextEditorUI.getCurrent());
    }

    /**
     * What to do when entering the edition view
     */
    @Override
    public void enter(ViewChangeEvent event) {
    }
    
    /**
     * Updates view
     */
    public void updateView(String message) {
    tabSheet.GetTextEditor().setCursorPosition();
	tabSheet.GetTextEditor().modifyText(message,false);
	tabSheet.GetTextEditor().getCursorPosition();
	// Update undo button
	boolean canUndo = reg.canUndo();
	undoButton.setEnabled(canUndo);
	if (canUndo) undoButton.setPrimaryStyleName("text_editor_menu_button");
	else undoButton.setPrimaryStyleName("text_editor_menu_button_disabled");
	// Update redo button
	boolean canRedo = reg.canRedo();
	redoButton.setEnabled(canRedo);
	if (canRedo) redoButton.setPrimaryStyleName("text_editor_menu_button");
	else redoButton.setPrimaryStyleName("text_editor_menu_button_disabled");
    }

    /**
     * Exits from edition view to file selection view without unregistering
     * @param message Message to show as error message
     */
    public void exitView(String message) {
	// Go to new file selection view
	TextEditorUI.getCurrent().updateView(new FileSelectionView(),FILESELECTIONVIEW,null);
	Notification.show(Labels.getString("error"), message,Type.ERROR_MESSAGE);
    }

}
