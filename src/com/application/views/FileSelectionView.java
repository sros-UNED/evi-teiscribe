package com.application.views;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.vaadin.dialogs.ConfirmDialog;
import org.xmldb.api.base.XMLDBException;

import com.application.components.FileUploaded;
import com.application.components.FileUploader;
import com.application.components.Manager.DTDManager;
import com.application.components.Manager.FileManager;
import com.application.components.TEITextStruct.TextStruct;
import com.application.language.Labels;
import com.application.texteditor.TextEditorUI;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import static com.application.components.Constants.DOWNLOADXMLICON;
import static com.application.components.Constants.EDITIONVIEW;
import static com.application.components.Constants.NOTATORIZEDVIEW;
import static com.application.components.Constants.STRINGTYPE;
import static com.application.components.Constants.TEIPROJECTNAME;
import static com.application.components.Constants.UPLOADXMLICON;
import static com.application.components.Constants.DELETEICON;
import static com.application.components.Constants.RENAMEICON;
import static com.application.components.Constants.NEWFILEICON;
import static com.application.components.Constants.EXPORTFILEICON;
import static com.application.components.Constants.TEISTRUCTCHANGEICON;
import static com.application.components.Constants.CREATEPROJECTICON;
import static com.application.components.Constants.GENERICTEIPROJECT;
import static com.application.components.Constants.getXMLDBManager;
import static com.application.components.Constants.FALSE;
import static com.application.components.Constants.TRUE;
import static com.application.components.Constants.ERROR;
import static com.application.components.Constants.FILETYPE;
import static com.application.components.Constants.GENERICTEIFILE;
import static com.application.components.Constants.MANUALICON;
import static com.application.components.Constants.MANUALURL;


/**
 * View to select, download and modify files, TEI schemas and projects.
 * 
 * @author Miguel Urízar Salinas
 *
 */
public class FileSelectionView extends CustomComponent {
    private static final long serialVersionUID = -6179843137218755733L;
    
    /** Name of the project */
    String activeProject = null;
    /** Class for uploading XML file */
    FileUploader fileUploader;
    /** name of the environment*/
    String env = null;
    /** Selection panel needed for updating the folders when inside */
    HorizontalSplitPanel fileSelectionPanel;

    /** Button that has selected the project */
    Button lastClickedButton = null;

    /**
     * Custom button class to also store the data of the project linked to it
     * 
     * @author Miguel Urízar Salinas
     *
     */
    private class ProjectButton extends Button {
	private static final long serialVersionUID = 2294314136789636046L;

	/** Project structure to have the values */
	public String project;

	/**
	 * Constructor that also creates the click listener and updates the active project in the view
	 * 
	 * @param proj
	 */
	public ProjectButton(String proj) {
	    super();
	    project = proj;
	    setCaption(proj);
	    setWidth("100%");
	    setHeight("-1px");

	    // Add listener
	    addClickListener(new Button.ClickListener() {
		private static final long serialVersionUID = -5290130939769863038L;

		@Override
		public void buttonClick(ClickEvent event) {
		    // Change selected project name
		    activeProject = project;
		    // Update files in file layout
		    try {
				fileSelectionPanel.setSecondComponent(buildFilesLayout());
		    } catch (Exception e) {
				TextEditorUI.getCurrent().updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
				return;
			}
		    // Light up the button changing the values of css and fade out the other.
		    if (lastClickedButton != null)
			lastClickedButton.removeStyleName("selected_project_button");
		    lastClickedButton = event.getButton();
		    event.getButton().addStyleName("selected_project_button");
		}
	    });
	}
    }

    /**
     * Special layer with action buttons that stores the file path
     * 
     * @author Miguel Urízar Salinas
     *
     */
    private class ActionButtons extends HorizontalLayout {
	private static final long serialVersionUID = 7998834371912103249L;

	// PathName to remember which file is linked to the class
	final String fileData;
	final String project_name;
	

	/**
	 * Constructor that adds buttons
	 * 
	 * @param file
	 *            Structure to store all the data of the file linked to these action buttons
	 */
	public ActionButtons(String file, String project) {
	    // Save path name of the file to work with
	    fileData = file;
	    project_name = project;
	    // Add rename button
	    addComponent(renameFileButton());
	    // Add download button
	    addComponent(downloadButton());
	    // Add delete button
	    addComponent(deleteButton());
	    // Add delete button
	    addComponent(exportTeiSimpleButton());
	}

	/**
	 * Creates a new button for renaming the file.
	 * <p>
	 * It opens a new tab for renaming it. Does not include the .xml type and sends a warning if the new fileName already exists
	 * </p>
	 * 
	 * @return Rename button with the listener for renaming the file
	 */
	private Button renameFileButton() {
	    // Create and configure button
	    Button renameFileButton = new Button();
	    renameFileButton.setDescription(Labels.getString("RenameFileInfo"));
	    renameFileButton.setIcon(new ThemeResource(RENAMEICON));
	    renameFileButton.setPrimaryStyleName("text_editor_menu_button");

	    // Button click listener
	    renameFileButton.addClickListener(new ClickListener() {
		private static final long serialVersionUID = -5184013335987557603L;

		public void buttonClick(ClickEvent event) {
		    // Create a new sub-window for changing name
		    final Window renameFileWindow = new Window(Labels.getString("changeFileName"));

		    // Create the field to change the name
		    final TextField renameField = new TextField(null, fileData);
		    renameField.setSizeUndefined();

		    // Create confirmButton
		    Button confirmButton = new Button(Labels.getString("Save"));
		    confirmButton.setDescription(Labels.getString("SaveInfo"));
		    confirmButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = -2240032160954135303L;

			@Override
			public void buttonClick(ClickEvent event) {
			    // Extract new Name
			    String newName = renameField.getValue();
			    // If name is empty return and print name cannot be empty
			    if (newName.isEmpty()) {
				Notification.show(Labels.getString("NoEmptyName"), Type.HUMANIZED_MESSAGE);
				return;
			    }
			    // Rename the file in the database
			    int response = ERROR;
				try {
					response = getXMLDBManager(TextEditorUI.getCurrent().getUser()).updateFileName(env,newName, fileData, activeProject);
				} catch (Exception e) {
					TextEditorUI.getCurrent().updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
					return;
				}
			    // Check the response
			    switch (response) {
			    case FALSE:
				Notification.show(Labels.getString("fileUpdateError"), Type.ERROR_MESSAGE);
				break;
			    case ERROR:
				Notification.show(Labels.getString("DBError"), Type.ERROR_MESSAGE);
				// Close window
				UI.getCurrent().removeWindow(renameFileWindow);
				break;
			    case TRUE:
				// Close window
				UI.getCurrent().removeWindow(renameFileWindow);
				break;
			    }
			}

		    });

		    // Set window variables
		    renameFileWindow.center();
		    renameFileWindow.setResizable(false);
		    renameFileWindow.setWidth("400px");
		    renameFileWindow.setModal(true);
		    renameFileWindow.setIcon(new ThemeResource(RENAMEICON));
		    // Create the window layout
		    HorizontalLayout windowLayout = new HorizontalLayout();
		    windowLayout.setWidth("100%");
		    windowLayout.setMargin(true);
		    // Add components to fill the layout
		    windowLayout.addComponent(renameField);
		    windowLayout.addComponent(confirmButton);
		    renameField.setWidth("100%");
		    windowLayout.setExpandRatio(renameField, 1);

		    renameFileWindow.setContent(windowLayout);
		    UI.getCurrent().addWindow(renameFileWindow);
		}
	    });
	    return renameFileButton;
	}

	/**
	 * Creates a new button for deleting the file.
	 * <p>
	 * It asks for confirmation before deleting.
	 * </p>
	 * 
	 * @return Delete button with the listener for deleting the file
	 */
	private Button deleteButton() {
	    // Create and configure button
	    Button deleteButton = new Button();
	    deleteButton.setDescription(Labels.getString("DeleteFileInfo"));
	    deleteButton.setIcon(new ThemeResource(DELETEICON));
	    deleteButton.setPrimaryStyleName("text_editor_menu_button");

	    // Button click listener
	    deleteButton.addClickListener(new ClickListener() {
		private static final long serialVersionUID = -5184013335987557603L;

		public void buttonClick(ClickEvent event) {
		    // Confirm we are deleting the file
		    ConfirmDialog.show(getUI(), Labels.getString("confirmPlz"), Labels.getString("delete?") + fileData + " ?", Labels.getString("yes"),
			    Labels.getString("no"), new ConfirmDialog.Listener() {
				private static final long serialVersionUID = 5743879032434725898L;

				public void onClose(ConfirmDialog dialog) {
				    if (dialog.isConfirmed()) {
					// Delete file from database
					int response = ERROR;
					try {
						response = getXMLDBManager(TextEditorUI.getCurrent().getUser()).deleteFile(env, activeProject, fileData);
					} catch (Exception e) {
						TextEditorUI.getCurrent().updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
						return;
					}
					// Check the response
					switch (response) {
					case FALSE:
					    Notification.show(Labels.getString("fileDeleteError"), Type.ERROR_MESSAGE);
					    break;
					case ERROR:
					    Notification.show(Labels.getString("DBError"), Type.ERROR_MESSAGE);
					    break;
					case TRUE:
					    break;
					}
					// Notification.show(Labels.getString("deletedFile") + fileName, Type.TRAY_NOTIFICATION);
				    }
				}
			    });
		}
	    });
	    return deleteButton;
	}
	
	/**
	 * Creates a new button for exporting file to teiSimple.
	 * <p>
	 * It asks for confirmation before exporting.
	 * </p>
	 * 
	 * @return Export button with the listener for exporting the file
	 */
	private Button exportTeiSimpleButton() {
	    // Create and configure button
	    Button exportTeiSimpleButton = new Button();
	    exportTeiSimpleButton.setDescription(Labels.getString("ExportFileInfo"));
	    exportTeiSimpleButton.setIcon(new ThemeResource(EXPORTFILEICON));
	    exportTeiSimpleButton.setPrimaryStyleName("text_editor_menu_button");

	    // Button click listener
	    exportTeiSimpleButton.addClickListener(new ClickListener() {
		private static final long serialVersionUID = 8219384411672278109L;
		public void buttonClick(ClickEvent event) {
			int response = ERROR;
			// Delete file from database
			try {
				//Check if file is correct
				FileManager manager = new DTDManager(new String(getXMLDBManager(TextEditorUI.getCurrent().getUser()).getBinaryFile(env,activeProject, TEIPROJECTNAME), StandardCharsets.UTF_8),STRINGTYPE);
				TextStruct fileStruct = new TextStruct(  new String(getXMLDBManager(TextEditorUI.getCurrent().getUser()).getXMLFile(env,activeProject, fileData), StandardCharsets.UTF_8),STRINGTYPE);
				int result = fileStruct.validateXMLStructure(manager);
				if (result != 0) {
					response = FALSE;
				} else {
				    String newName = TextEditorUI.getCurrent().clearString(TextEditorUI.getCurrent().getCollection()+"_"+project_name+"_"+fileData+".xml");
				    response = getXMLDBManager(TextEditorUI.getCurrent().getUser()).exportTeiSimpleFileName(env, fileData, activeProject, newName);
				    getUI().getPage().open("http://www.evilinhd.com:8888/exist/apps/tei-simple/test/"+newName+"?odd=myteisimple.odd", null);
				}
				
			} catch (Exception e) {
				System.out.println("Error");
				e.printStackTrace();
				TextEditorUI.getCurrent().updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
				return;
			}
			// Check the response
			switch (response) {
			case FALSE:
			    Notification.show(Labels.getString("someErrorsNotExporting"), Type.ERROR_MESSAGE);
			    break;
			case ERROR:
			    Notification.show(Labels.getString("DBError"), Type.ERROR_MESSAGE);
			    break;
			case TRUE:
			    break;
			}
		}});
	    return exportTeiSimpleButton;
	}
	
	

	/**
	 * Creates a new button for downloading the file.
	 * <p>
	 * Links the button with a FileDownloader
	 * </p>
	 * 
	 * @return Download button with the FileDownloader attached
	 */
	private Button downloadButton() {
	    // Create and configure button
	    Button downloadButton = new Button();
	    downloadButton.setDescription(Labels.getString("DownloadFileInfo"));
	    downloadButton.setIcon(new ThemeResource(DOWNLOADXMLICON));
	    downloadButton.setPrimaryStyleName("text_editor_menu_button");

	    // Generate the resource for downloading the xml
	    StreamResource sr = new StreamResource(new StreamResource.StreamSource() {
		private static final long serialVersionUID = 6817727287879802075L;

		// Download file
		public InputStream getStream() {
		    try {
				return new ByteArrayInputStream( getXMLDBManager(TextEditorUI.getCurrent().getUser()).getXMLFile(env, activeProject, fileData));
		    } catch (Exception e) {
				TextEditorUI.getCurrent().updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
				return null;
			}
		}
	    }, fileData);
	    // Link the stream resource with the button
	    FileDownloader fd = new FileDownloader(sr);
	    fd.extend(downloadButton);
	    return downloadButton;
	}

    }

    /**
     * Constructor of the view.
     */
    public FileSelectionView() {
	env = TextEditorUI.getCurrent().getEnvironment();
	createFileSelectionView();
    }

    /**
     * Creates the view.
     * <p>
     * Includes the barLayout and the panel for file selection
     * </p>
     */
    public void createFileSelectionView() {

	// Make the view full size
	setSizeFull();
	activeProject = null;
	// Add the main layout
	VerticalLayout mainLayout = new VerticalLayout();
	mainLayout.setSizeFull();
	// Add menu bar layout
	mainLayout.addComponent(buildBarLayout());
	// fileSelectionPanel
	HorizontalSplitPanel fileSelectionPanel;
	try {
		fileSelectionPanel = buildFileSelectionPanel();
	} catch (Exception e) {
		e.printStackTrace();
		TextEditorUI.getCurrent().updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
		return;
	}
	mainLayout.addComponent(fileSelectionPanel);
	mainLayout.setExpandRatio(fileSelectionPanel, 1.0f);
	// Generate global composition
	setCompositionRoot(mainLayout);
    }

    /**
     * Creates the panel for selecting the file.
     * <p>
     * On the left of the panel there is a layout with the projects as clickable buttons, and on the right panel a layout shows the files in the chosen project.
     * No default project is selected at the beginning
     * </p>
     * 
     * @return Panel that contains the projects an files
     * @throws XMLDBException 
     */
    private HorizontalSplitPanel buildFileSelectionPanel() throws XMLDBException {

	// common part: create layout
	fileSelectionPanel = new HorizontalSplitPanel();
	fileSelectionPanel.setSizeFull();
	// maximum and minimum size of each panel
	fileSelectionPanel.setSplitPosition(20, Unit.PERCENTAGE);
	fileSelectionPanel.setMaxSplitPosition(30, Unit.PERCENTAGE);
	fileSelectionPanel.setMinSplitPosition(20, Unit.PERCENTAGE);

	// projectsLayout
	fileSelectionPanel.addComponent(buildProjectsLayout());

	// filesLayout
	// fileSelectionPanel.addComponent(buildFilesLayout());

	return fileSelectionPanel;
    }

    /**
     * Updates the projectLayout without changing anything else and activates the project that was active.
     */
    public void updateProjectsLayout(){
	try {
		fileSelectionPanel.setFirstComponent(buildProjectsLayout());
	} catch (Exception e) {
		TextEditorUI.getCurrent().updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
		return;
	}
    }

    /**
     * Layout with the projects as buttons.
     * <p>
     * Also fills the file layout with a project or an empty one. When a project is clicked, file layout is changed.
     * </p>
     * 
     * @return Layout with the available projects
     * @throws XMLDBException 
     */
    public VerticalLayout buildProjectsLayout() throws XMLDBException {

	VerticalLayout projectsLayout = new VerticalLayout();
	projectsLayout.setSizeFull();

	// Create Title
	Label projectsLayoutLabel = new Label(Labels.getString("projects"));
	projectsLayoutLabel.setDescription(Labels.getString("CollectionNameInfo"));
	projectsLayoutLabel.setStyleName("project_title_label");
	projectsLayoutLabel.setSizeUndefined();
	// Create Header
	VerticalLayout projectTitleLayout = new VerticalLayout();
	projectTitleLayout.setStyleName("project_title_layout");

	// Add and align title
	projectTitleLayout.addComponent(projectsLayoutLabel);
	projectTitleLayout.setComponentAlignment(projectsLayoutLabel, Alignment.MIDDLE_CENTER);
	// Add Header
	projectsLayout.addComponent(projectTitleLayout);

	// Create buttons Panel to have automatic scroll
	Panel projectButtonsPanel = new Panel();
	projectButtonsPanel.setSizeFull();
	VerticalLayout projectButtonsLayout = new VerticalLayout();
	projectTitleLayout.setSizeUndefined();

	ArrayList<String> projects = null;
	projects = getXMLDBManager(TextEditorUI.getCurrent().getUser()).getProjects(env);
	// Check if we have an active project
	Boolean noActiveProject = true;
	// There was an error getting the projects
	if (projects == null) {
	    Notification.show(Labels.getString("DBError"), Type.ERROR_MESSAGE);
	} else {
	    // No projects exist
	    if (projects.size() == 0) {
		projectButtonsLayout.addComponent(new Label(Labels.getString("noProjects")));
	    } else {
		// Insert all the projects
		for (int i = 0; i < projects.size(); i++) {
		    // Create project button
		    ProjectButton projectButton = new ProjectButton(projects.get(i));
		    projectButton.setDescription(Labels.getString("ProjectClickToEnterInfo"));
		    projectButtonsLayout.addComponent(projectButton);
		    // If this button is the one of the active project we click it programmatically
		    if (activeProject != null) {
			if (activeProject.equals(projectButton.project)) {
			    projectButton.click();
			    noActiveProject = false;
			}
		    }
		}
	    }
	}
	projectButtonsPanel.setContent(projectButtonsLayout);
	projectsLayout.addComponent(projectButtonsPanel);
	projectsLayout.setExpandRatio(projectButtonsPanel, 1);
	// If no active project, we delete the active project and generate the files layout
	if (noActiveProject) {
	    activeProject = null;
	    fileSelectionPanel.setSecondComponent(buildFilesLayout());
	}
	return projectsLayout;
    }

    /**
     * Creates the files layout with a title to modify the project and a table with all the files in the project.
     * 
     * @return layout with the files of the selected project or a layout with a label asking to choose a project
     * @throws XMLDBException 
     */
    private VerticalLayout buildFilesLayout() throws XMLDBException {

	// Create and configure layout
	VerticalLayout filesLayout = new VerticalLayout();
	filesLayout.setWidth("100%");
	filesLayout.setHeight("-1px");
	// If no project is chosen only a label is displayed
	if (activeProject == null) {
	    filesLayout.addComponent(new Label(Labels.getString("selectProjectMessage")));
	    // If project is chosen we make the table and title
	} else {
	    // Add title to the files layout
	    filesLayout.addComponent(menuFileSelectionTitle());
	    // Add table to the files layout
	    Table fileTable = menuFileSelectionTable();
	    filesLayout.addComponent(fileTable);
	    filesLayout.setExpandRatio(fileTable, 1);
	}
	return filesLayout;
    }

    /**
     * Creates the title for the file layout with the buttons to modify the project.
     * 
     * @return Layout with a title with the project name and buttons to edit that layout.
     * @throws XMLDBException 
     */
    private HorizontalLayout menuFileSelectionTitle() throws XMLDBException {

	// Create Title
	Label projectsLayoutLabel = new Label(activeProject);
	projectsLayoutLabel.setDescription(Labels.getString("ActiveCollectionNameInfo"));
	projectsLayoutLabel.setStyleName("title_label");
	projectsLayoutLabel.setSizeUndefined();
	// Create Header
	HorizontalLayout projectTitleLayout = new HorizontalLayout();
	projectTitleLayout.setStyleName("project_title_layout");
	// projectTitleLayout.setHeight("50px");

	// Add and align title
	projectTitleLayout.addComponent(projectsLayoutLabel);
	projectTitleLayout.setComponentAlignment(projectsLayoutLabel, Alignment.MIDDLE_LEFT);

	// Add upload XML button
	Button uploadButton = uploadXMLButton();
	projectTitleLayout.addComponent(uploadButton);
	projectTitleLayout.setComponentAlignment(uploadButton, Alignment.MIDDLE_LEFT);

	// Add new XML button
	Button newFileButton = newXMLButton();
	projectTitleLayout.addComponent(newFileButton);
	projectTitleLayout.setComponentAlignment(newFileButton, Alignment.MIDDLE_LEFT);

	// Add rename project button
	Button renameButton = renameProjectButton();
	projectTitleLayout.addComponent(renameButton);
	projectTitleLayout.setComponentAlignment(renameButton, Alignment.MIDDLE_LEFT);

	// Add change TEI struct button
	Button changeTEIStructButton = changeTEIStructButton();
	projectTitleLayout.addComponent(changeTEIStructButton);
	projectTitleLayout.setComponentAlignment(changeTEIStructButton, Alignment.MIDDLE_LEFT);

	// Add download TEI struct button
	Button downloadTEIStructButton = downloadTEIStructButton();
	projectTitleLayout.addComponent(downloadTEIStructButton);
	projectTitleLayout.setComponentAlignment(downloadTEIStructButton, Alignment.MIDDLE_LEFT);
	

	// Add delete project button
	Button deleteProjectButton = deleteProjectButton();
	projectTitleLayout.addComponent(deleteProjectButton);
	projectTitleLayout.setComponentAlignment(deleteProjectButton, Alignment.MIDDLE_LEFT);

	return projectTitleLayout;

    }

    /**
     * Creates a table with a row for each file.
     * <p>
     * Each row has a name, date modified, size and action buttons. A column is hidden for knowing the path of each file. From css resizing rows is disabled.
     * When clicking a row, text editor is opened for that file.
     * </p>
     * 
     * @return Filled table for showing the files from selected project.
     * @throws XMLDBException 
     */
    private Table menuFileSelectionTable() throws XMLDBException {

	// Create and configure table
	Table table = new Table();
	table.setItemDescriptionGenerator(new ItemDescriptionGenerator() {
		private static final long serialVersionUID = -2094144754176362121L;

		@Override
		public String generateDescription(Component source, Object itemId, Object propertyId) {
			return Labels.getString("RowClickToEnterInfo");
		}
	});
	table.setWidth("100%");
	table.addStyleName("file_selection_table");

	// Define columns for the table
	table.addContainerProperty(Labels.getString("columnName"), String.class, null);
	table.addContainerProperty(Labels.getString("columnSize"), String.class, " - ");
	table.addContainerProperty(Labels.getString("columnDate"), String.class, " - ");
	table.addContainerProperty(Labels.getString("columnActions"), ActionButtons.class, " - ");

	// Name expands the most
	table.setColumnExpandRatio(Labels.getString("columnName"), 1);
	// Actions is fixed 130 pixels because of the buttons
	table.setColumnWidth(Labels.getString("columnActions"), 162);

	// All rows are loaded at the same time
	table.setPageLength(0);

	// Read the files from the database using projectName
	ArrayList<String> files = null;
	files = getXMLDBManager(TextEditorUI.getCurrent().getUser()).getFilesFromProject(env,activeProject);
	int tablePointer = 0;
	// Print all the files
	for (String file : files) {
	    // Add files to the table
		table.addItem(new Object[] { file, new DecimalFormat("##0.0").format(getXMLDBManager(TextEditorUI.getCurrent().getUser()).getSize(env,activeProject, file) / 1024) + " KB",
		new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(getXMLDBManager(TextEditorUI.getCurrent().getUser()).getLastModified(env,activeProject, file)), new ActionButtons(file, activeProject) }, tablePointer);
	    tablePointer++;
	}
	// Listener to open editor when a row is selected
	table.addItemClickListener(new ItemClickListener() {
	    private static final long serialVersionUID = -4035373045630033926L;

	    @Override
	    public void itemClick(ItemClickEvent event) {
		// Get the teiStruct
		try {
			if (getXMLDBManager(TextEditorUI.getCurrent().getUser()).checkTEIStruct(env,activeProject) == FALSE) {
			    Notification.show(Labels.getString("noTEILinked"), Type.ERROR_MESSAGE);
			} else {
			    // Extract the file direction from ActionButtons and ActiveProject
			    String file = ((ActionButtons) event.getItem().getItemProperty(Labels.getString("columnActions")).getValue()).fileData;
			    // Go to the edition view
			    TextEditorUI.getCurrent().updateView(new EditionView(activeProject, file), EDITIONVIEW, env + "/" + activeProject + "/" + file);
			}
		} catch (Exception e) {
			TextEditorUI.getCurrent().updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
			return;
		}
	    }
	});

	return table;
    }

    /**
     * Creates a bar for the top of the view displaying menu buttons.
     * 
     * @return Layout with the menu buttons
     */
    private VerticalLayout buildBarLayout() {
	// common part: create external bar layout
	VerticalLayout barLayout = new VerticalLayout();
	barLayout.setPrimaryStyleName("text_editor_outer_menu_layout");
	
	HorizontalLayout innerBarLayout = new HorizontalLayout();
	innerBarLayout.setSizeUndefined();
	innerBarLayout.setPrimaryStyleName("text_editor_inner_menu_layout");
	barLayout.addComponent(innerBarLayout);
	

	// Add create project button
	addCreateProjectButton(innerBarLayout);
	addReadManualButton(innerBarLayout);

	return barLayout;
    }
    
    /**
     * Adds a button for reading the maunal
     * 
     * @param innerBarLayout
     *            Layout where to insert the button
     */
    private void addReadManualButton(HorizontalLayout innerBarLayout) {
    	Button buttonReadManual = new Button();
    	buttonReadManual.setDescription(Labels.getString("ReadManualInfo"));
    	buttonReadManual.setIcon(new ThemeResource(MANUALICON));
    	buttonReadManual.setPrimaryStyleName("text_editor_read_manual_button");
    	BrowserWindowOpener opener = new BrowserWindowOpener(MANUALURL);
    	opener.setWindowName("_blank");
    	// Attach it to a button
    	opener.extend(buttonReadManual);
    	innerBarLayout.addComponent(buttonReadManual);
    }
    
    /**
     * Adds a button for creating a project
     * 
     * @param innerBarLayout
     *            Layout where to insert the button
     */
    private void addCreateProjectButton(HorizontalLayout innerBarLayout) {

	// Create button and configure it
	Button buttonCreateProject = new Button();
	buttonCreateProject.setDescription(Labels.getString("CreateProjectInfo"));
	buttonCreateProject.setIcon(new ThemeResource(CREATEPROJECTICON));
	buttonCreateProject.setPrimaryStyleName("text_editor_menu_button");

	// Button click listener
	buttonCreateProject.addClickListener(new ClickListener() {
		private static final long serialVersionUID = -8629878360771394304L;

		public void buttonClick(ClickEvent event) {

		// Create a new sub-window for defining name
		final Window renameProjectWindow = new Window(Labels.getString("createProjectName"));

		// Create the field to name the project
		final TextField renameField = new TextField(null, "");
		renameField.setSizeUndefined();

		// Create confirmButton
		Button confirmButton = new Button(Labels.getString("Create"));
		confirmButton.setDescription(Labels.getString("CreateProjectConfirmInfo"));
		confirmButton.addClickListener(new ClickListener() {
		    private static final long serialVersionUID = 6764773479640348180L;

		    @Override
		    public void buttonClick(ClickEvent event) {

			// Extract project Name
			String projectName = renameField.getValue();
			// Don't allow empty name
			if (projectName.isEmpty()) {
			    Notification.show(Labels.getString("NoEmptyName"), Type.HUMANIZED_MESSAGE);
			    return;
			}
			// Add project in the database
			int response = ERROR;
			try {
				response = getXMLDBManager(TextEditorUI.getCurrent().getUser()).addProject(projectName, env);
			} catch (Exception e) {
				TextEditorUI.getCurrent().updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
				return;
			}
			// Check the response
			switch (response) {
			case FALSE:
			    Notification.show(Labels.getString("projectExists") + projectName, Type.WARNING_MESSAGE);
			    UI.getCurrent().removeWindow(renameProjectWindow);
			    return;
			case ERROR:
			    Notification.show(Labels.getString("DBError"), Type.ERROR_MESSAGE);
			    UI.getCurrent().removeWindow(renameProjectWindow);
			    return;
			case TRUE:
			    break;
			}
			// Close window
			UI.getCurrent().removeWindow(renameProjectWindow);
			// Set TEI struct for project
			try {
				response = getXMLDBManager(TextEditorUI.getCurrent().getUser()).updateProjectTEISchema(env,projectName, GENERICTEIFILE);
			} catch (Exception e) {
				selectTEIStructure(projectName, true);
			}
		    }
		});

		// Set window variables
		renameProjectWindow.center();
		renameProjectWindow.setResizable(false);
		renameProjectWindow.setWidth("400px");
		renameProjectWindow.setModal(true);
		renameProjectWindow.setIcon(new ThemeResource(CREATEPROJECTICON));
		// Create the window layout
		HorizontalLayout windowLayout = new HorizontalLayout();
		windowLayout.setWidth("100%");
		windowLayout.setMargin(true);
		// Add components to fill the layout
		windowLayout.addComponent(renameField);
		windowLayout.addComponent(confirmButton);
		renameField.setWidth("100%");
		windowLayout.setExpandRatio(renameField, 1);
		renameProjectWindow.setContent(windowLayout);
		UI.getCurrent().addWindow(renameProjectWindow);
	    }
	});

	innerBarLayout.addComponent(buttonCreateProject);
    }

    /**
     * Adds a button for creating a file
     * 
     * @return Button with listener for creating file
     */
    private Button newXMLButton() {

	// Create button and configure it
	Button buttonCreateFile = new Button();
	buttonCreateFile.setDescription(Labels.getString("CreateFileInfo"));
	buttonCreateFile.setIcon(new ThemeResource(NEWFILEICON));
	buttonCreateFile.setPrimaryStyleName("text_editor_menu_button");

	// Button click listener
	buttonCreateFile.addClickListener(new ClickListener() {
		private static final long serialVersionUID = 1036219863936575973L;

		public void buttonClick(ClickEvent event) {

		// Create a new sub-window for defining name
		final Window newFileWindow = new Window(Labels.getString("createFileName"));

		// Create the field to name the project
		final TextField renameField = new TextField(null, "");
		renameField.setSizeUndefined();

		// Create confirmButton
		Button confirmButton = new Button(Labels.getString("Create"));
		confirmButton.setDescription(Labels.getString("CreateFileConfirmInfo"));
		confirmButton.addClickListener(new ClickListener() {
		    private static final long serialVersionUID = -5027260937177648253L;

		    @Override
		    public void buttonClick(ClickEvent event) {
			
			// Get the name and path of the file
			String fileName = renameField.getValue();
			// Don't allow empty name
			if (fileName.isEmpty()) {
			    Notification.show(Labels.getString("NoEmptyName"), Type.HUMANIZED_MESSAGE);
			    return;
			}
			//Check if file already exists
			try {
				if (getXMLDBManager(TextEditorUI.getCurrent().getUser()).checkIfFileExists(env,activeProject,fileName)==TRUE)
				{
				    Notification.show(Labels.getString("error"),Labels.getString("fileAlreadyExists1") + 
					    fileName + " " + Labels.getString("fileAlreadyExists2"), Type.ERROR_MESSAGE);
				    return;
				}
			} catch (Exception e) {
				TextEditorUI.getCurrent().updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
				return;
			}
			    
			// Generate the file
			TextStruct newFile = new TextStruct();

			// Save the file
			int response = ERROR;
			try {
				response = getXMLDBManager(TextEditorUI.getCurrent().getUser()).addXMLFile(env, activeProject, fileName, newFile.GenerateInnerXML().getBytes(StandardCharsets.UTF_8));
			} catch (Exception e) {
				TextEditorUI.getCurrent().updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
				return;
			}
			
			// Check if there was an error
			if (response == ERROR) {
			    Notification.show(Labels.getString("error"), Labels.getString("DBError"), Type.ERROR_MESSAGE);
			} else if (response == FALSE) {
			    Notification.show(Labels.getString("error"),
				    Labels.getString("fileAlreadyExists1") + fileName + " " + Labels.getString("fileAlreadyExists2"), Type.ERROR_MESSAGE);
			}
			// Close window
			UI.getCurrent().removeWindow(newFileWindow);
		    }
		});

		// Set window variables
		newFileWindow.center();
		newFileWindow.setResizable(false);
		newFileWindow.setWidth("400px");
		newFileWindow.setModal(true);
		newFileWindow.setIcon(new ThemeResource(NEWFILEICON));
		// Create the window layout
		HorizontalLayout windowLayout = new HorizontalLayout();
		windowLayout.setWidth("100%");
		windowLayout.setMargin(true);
		// Add components to fill the layout
		windowLayout.addComponent(renameField);
		windowLayout.addComponent(confirmButton);
		renameField.setWidth("100%");
		windowLayout.setExpandRatio(renameField, 1);
		newFileWindow.setContent(windowLayout);
		UI.getCurrent().addWindow(newFileWindow);
	    }
	});

	return buttonCreateFile;
    }

    /**
     * Creates a button to upload files to the selected project.
     * 
     * @return Button that uploads a file when created
     */
    private Button uploadXMLButton() {

	// Create button and configure it
	Button buttonUploadXML = new Button();
	buttonUploadXML.setDescription(Labels.getString("UploadXMLInfo"));
	buttonUploadXML.setIcon(new ThemeResource(UPLOADXMLICON));
	buttonUploadXML.setPrimaryStyleName("text_editor_menu_button");

	// Make the button to show the new window
	buttonUploadXML.addClickListener(new Button.ClickListener() {
	    private static final long serialVersionUID = -5224592588246806016L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		// Create the window and load file
		fileUploader = new FileUploader(Labels.getString("headerUploadXMLWindow") + activeProject, UPLOADXMLICON, new FileUploaded() {
		    @Override
		    // When upload is done save the file and add to the project
		    public int FileUploadedWork(File tempFile, String fileName) {

			//Check if file already exists
			try {
				if (getXMLDBManager(TextEditorUI.getCurrent().getUser()).checkIfFileExists(env,activeProject,fileName)==TRUE)
				{
				    Notification.show(Labels.getString("error"),Labels.getString("fileAlreadyExists1") + 
					    fileName + " " + Labels.getString("fileAlreadyExists2"), Type.ERROR_MESSAGE);
				    return ERROR;
				}
			} catch (Exception e) {
				TextEditorUI.getCurrent().updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
				return ERROR;
			}
			
			// Check if is an XML valid file
			try {
			    new TextStruct(tempFile.getCanonicalPath(),FILETYPE);
			} catch (Exception e) {
			    tempFile.delete();
			    Notification.show(Labels.getString("error"), Labels.getString("badXMLStructure"), Type.ERROR_MESSAGE);
			    fileUploader = null;
			    return ERROR;
			}

			// Add to database
			int response = ERROR;
			try {
			    response = getXMLDBManager(TextEditorUI.getCurrent().getUser()).addXMLFile(env, activeProject, fileName, Files.readAllBytes(tempFile.toPath()));
			} catch (Exception e) {	}
			tempFile.delete();
			fileUploader = null;
			// Check if there was an error
			if (response == ERROR) {
			    Notification.show(Labels.getString("error"), Labels.getString("DBError"), Type.ERROR_MESSAGE);
			} else if (response == FALSE) {
			    Notification.show(Labels.getString("error"),
				    Labels.getString("fileAlreadyExists1") + fileName + " " + Labels.getString("fileAlreadyExists2"), Type.ERROR_MESSAGE);
			}
			return response;
		    }

		});

	    }
	});
	return buttonUploadXML;
    }

    /**
     * Button to change TEI structure
     * 
     * @return A button to change TEI structure when clicking
     */
    private Button changeTEIStructButton() {

	// Create button and configure it
	Button buttonChangeTEIStruct = new Button();
	buttonChangeTEIStruct.setDescription(Labels.getString("ChangeTEIStructInfo"));
	buttonChangeTEIStruct.setIcon(new ThemeResource(TEISTRUCTCHANGEICON));
	buttonChangeTEIStruct.setPrimaryStyleName("text_editor_menu_button");

	buttonChangeTEIStruct.addClickListener(new ClickListener() {
	    private static final long serialVersionUID = 527259169216923940L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		selectTEIStructure(activeProject, false);
	    }

	});

	return buttonChangeTEIStruct;
    }

    /**
     * Button to download TEI structure
     * 
     * @return A button to download TEI structure when clicking
     * @throws XMLDBException 
     */
    private Button downloadTEIStructButton() throws XMLDBException {

	// Create button and configure it
	Button buttonDownloadTEIStruct = new Button();
	buttonDownloadTEIStruct.setDescription(Labels.getString("DownloadTEIStructInfo"));
	buttonDownloadTEIStruct.setIcon(new ThemeResource(DOWNLOADXMLICON));
	buttonDownloadTEIStruct.setPrimaryStyleName("text_editor_menu_button");
	// Get the teiStruct
	final int tei = getXMLDBManager(TextEditorUI.getCurrent().getUser()).checkTEIStruct(env,activeProject);
	// If no possible to grab the tei values a DB error appears
	if (tei == FALSE) {
	    Notification.show(Labels.getString("noTEILinked"), Type.WARNING_MESSAGE);
	    buttonDownloadTEIStruct.addClickListener(new ClickListener() {
		private static final long serialVersionUID = -1891140452110130575L;

		@Override
		public void buttonClick(ClickEvent event) {
		    Notification.show(Labels.getString("noTEILinked"), Type.ERROR_MESSAGE);
		}

	    });
	} else {
	    // Generate the resource for downloading the TEI
	    StreamResource sr = new StreamResource(new StreamResource.StreamSource() {
		private static final long serialVersionUID = -8203681650541575889L;

		public InputStream getStream() {
		    try {
				return new ByteArrayInputStream(getXMLDBManager(TextEditorUI.getCurrent().getUser()).getBinaryFile(env,activeProject, TEIPROJECTNAME));
		    } catch (Exception e) {
				TextEditorUI.getCurrent().updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
				return null;
			}
		}
	    }, TEIPROJECTNAME);
	    // Link the stream resource with the button
	    FileDownloader fd = new FileDownloader(sr);
	    fd.extend(buttonDownloadTEIStruct);
	}
	return buttonDownloadTEIStruct;
    }

    /**
     * Button to delete the project and all its linked files.
     * <p>
     * It first informs the user with a confirm dialog.
     * </p>
     * 
     * @return A button that deletes the project when clicking.
     * 
     */
    private Button deleteProjectButton() {

	// Create button and configure it
	Button buttonDeleteProject = new Button();
	buttonDeleteProject.setDescription(Labels.getString("DeleteProjectInfo"));
	buttonDeleteProject.setIcon(new ThemeResource(DELETEICON));
	buttonDeleteProject.setPrimaryStyleName("text_editor_menu_button");

	buttonDeleteProject.addClickListener(new ClickListener() {
	    private static final long serialVersionUID = 527259169216923940L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		// Tell everything is going to be deleted from project(files and probably TEI struct)
		ConfirmDialog.show(getUI(), Labels.getString("confirmPlz"),
			Labels.getString("deleteProject?") + activeProject + Labels.getString("deleteProject2?"), Labels.getString("yes"),
			Labels.getString("no"), new ConfirmDialog.Listener() {
			    private static final long serialVersionUID = -4702595100404123937L;

			    public void onClose(ConfirmDialog dialog) {
				if (dialog.isConfirmed()) {
				    // Remove the project
				    int response = ERROR;
					try {
						response = getXMLDBManager(TextEditorUI.getCurrent().getUser()).deleteProject(env,activeProject);
					} catch (Exception e) {
						TextEditorUI.getCurrent().updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
						return;
					}
				    // Check the response
				    switch (response) {
				    case FALSE:
					Notification.show(Labels.getString("projectDeleteError"), Type.ERROR_MESSAGE);
					break;
				    case ERROR:
					Notification.show(Labels.getString("DBError"), Type.ERROR_MESSAGE);
					break;
				    case TRUE:
					break;
				    }
				}
			    }
			});

	    }
	});
	return buttonDeleteProject;
    }

    /**
     * Creates a new button for renaming the project.
     * <p>
     * It opens a new tab for renaming it. A project must be selected and sends a warning if the new project name already exists.
     * </p>
     * 
     * @return Rename button with the listener for renaming the file
     */
    private Button renameProjectButton() {
	// Create and configure button
	Button renameprojectButton = new Button();
	renameprojectButton.setDescription(Labels.getString("RenameProjectInfo"));
	renameprojectButton.setIcon(new ThemeResource(RENAMEICON));
	renameprojectButton.setPrimaryStyleName("text_editor_menu_button");

	// Button click listener
	renameprojectButton.addClickListener(new ClickListener() {
	    private static final long serialVersionUID = 527259169216923940L;

	    public void buttonClick(ClickEvent event) {

		// Create a new sub-window for changing name
		final Window renameProjectWindow = new Window(Labels.getString("changeProjectName"));

		// Create the field to change the name
		final TextField renameField = new TextField(null, activeProject);
		renameField.setSizeUndefined();

		// Create confirmButton
		Button confirmButton = new Button(Labels.getString("Save"));
		confirmButton.setDescription(Labels.getString("RenameProjectConfirmInfo"));
		confirmButton.addClickListener(new ClickListener() {
		    private static final long serialVersionUID = -2240032160954135303L;

		    @Override
		    public void buttonClick(ClickEvent event) {
			// Extract new Name
			String newName = renameField.getValue();
			// Don't allow empty name
			if (newName.isEmpty()) {
			    Notification.show(Labels.getString("NoEmptyName"), Type.HUMANIZED_MESSAGE);
			    return;
			}
			// Change project name in database
			int response = ERROR;
			try {
				response = getXMLDBManager(TextEditorUI.getCurrent().getUser()).updateProjectName(env, newName, activeProject);
			} catch (Exception e) {
				TextEditorUI.getCurrent().updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
				return;
			}
			// Check the response
			switch (response) {
			case FALSE:
			    Notification.show(Labels.getString("projectExists") + newName, Type.WARNING_MESSAGE);
			    break;
			case ERROR:
			    Notification.show(Labels.getString("DBError"), Type.ERROR_MESSAGE);
			    break;
			case TRUE:
			    break;
			}
			UI.getCurrent().removeWindow(renameProjectWindow);
		    }
		});

		// Set window variables
		renameProjectWindow.center();
		renameProjectWindow.setResizable(false);
		renameProjectWindow.setWidth("400px");
		renameProjectWindow.setModal(true);
		renameProjectWindow.setIcon(new ThemeResource(RENAMEICON));
		// Create the window layout
		HorizontalLayout windowLayout = new HorizontalLayout();
		windowLayout.setWidth("100%");
		windowLayout.setMargin(true);
		// Add components to fill the layout
		windowLayout.addComponent(renameField);
		windowLayout.addComponent(confirmButton);
		renameField.setWidth("100%");
		windowLayout.setExpandRatio(renameField, 1);
		renameProjectWindow.setContent(windowLayout);
		UI.getCurrent().addWindow(renameProjectWindow);
	    }
	});

	return renameprojectButton;
    }

    /**
     * Modal window to select the TEI structure for the project
     * <p>
     * It allows also to update a new TEI schema.
     * </p>
     * 
     * @param selectedProject
     *            Project to update the TEI file
     * @param mandatory
     *            if true is mandatory to select a TEI schema
     */
    private void selectTEIStructure(final String selectedProject, final boolean mandatory) {
	// Create a new sub-window for changing TEI structure
	final Window changeTEIStructWindow = new Window(Labels.getString("changeProjectStruct"));

	// Set window variables
	changeTEIStructWindow.center();
	changeTEIStructWindow.setResizable(false);
	changeTEIStructWindow.setWidth("400px");
	changeTEIStructWindow.setModal(true);
	changeTEIStructWindow.setIcon(new ThemeResource(TEISTRUCTCHANGEICON));
	changeTEIStructWindow.setClosable(false);

	// Create the layer inside the window
	VerticalLayout changeTEIStructLayout = new VerticalLayout();
	changeTEIStructLayout.setWidth("100%");
	changeTEIStructLayout.setMargin(true);
	changeTEIStructWindow.setContent(changeTEIStructLayout);

	// Create a panel for possible scrolling
	Panel chooseTEIStructPanel = new Panel(Labels.getString("chooseProjectStruct"));
	chooseTEIStructPanel.setSizeFull();
	chooseTEIStructPanel.addStyleName("panel_inside_modal_window");
	changeTEIStructLayout.addComponent(chooseTEIStructPanel);

	// Create the option group, can be empty if no TEI structure is in the database
	final OptionGroup chooseTEIStruct = new OptionGroup();
	chooseTEIStruct.setNullSelectionAllowed(false);

	// Add all the projects from database
	ArrayList<String> structs = null;
	try {
		structs = getXMLDBManager(TextEditorUI.getCurrent().getUser()).getTEIStructs(env);
	} catch (Exception e) {
		TextEditorUI.getCurrent().updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
		return;
	}

	// There was an error getting the projects
	if (structs == null) {
	    Notification.show(Labels.getString("DBError"), Type.ERROR_MESSAGE);
	    return;
	} else {
	    // No TEI structure exists
	    if (structs.size() == 0) {
		chooseTEIStructPanel.setContent(new Label(Labels.getString("noTEIStructs")));
	    } else {
		// Create the list of TEI Struct where only one option is selected
		for (int i = 0; i < structs.size(); i++) {
		    String tei = structs.get(i);
		    chooseTEIStruct.addItem(tei);
		    chooseTEIStruct.setItemCaption(tei, tei);
		}
		chooseTEIStruct.addStyleName("optionGroup_TEI_structure");
		chooseTEIStructPanel.setContent(chooseTEIStruct);
	    }
	}

	// Create the buttons layout
	HorizontalLayout buttonsLayout = new HorizontalLayout();
	buttonsLayout.setWidth("100%");
	buttonsLayout.setMargin(false);
	changeTEIStructLayout.addComponent(buttonsLayout);

	// Add upload TEI structure Button
	Button uploadTEIStructButton = new Button(Labels.getString("Upload"));
	uploadTEIStructButton.setDescription(Labels.getString("UploadTEIInfo"));
	uploadTEIStructButton.addClickListener(new ClickListener() {
	    private static final long serialVersionUID = 4980172601419761049L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		// Create the window and load file
		fileUploader = new FileUploader(Labels.getString("headerUploadTEIWindow"), UPLOADXMLICON, new FileUploaded() {
		    @Override
		    // When upload is done save the file and add to the project
		    public int FileUploadedWork(File tempFile, String fileName) {

			// Check if is a TEI valid file
			try {
			    new DTDManager(tempFile.getCanonicalPath(),FILETYPE);
			} catch (Exception e) {
			    Notification.show(Labels.getString("error"), Labels.getString("UploadTEIStructError") + "\n" + e.getMessage(), Type.ERROR_MESSAGE);
			    e.printStackTrace();
			    tempFile.delete();
			    fileUploader = null;
			    return ERROR;
			}
			
			// Add to database
			int response = ERROR;
			try {
			    response = getXMLDBManager(TextEditorUI.getCurrent().getUser()).addBinaryFile(env,GENERICTEIPROJECT, fileName, new String(Files.readAllBytes(tempFile.toPath()), "UTF-8"));
			} catch (Exception e) {	}
			tempFile.delete();
			fileUploader = null;
			
			// Check if there was an error
			if (response == ERROR) {
			    Notification.show(Labels.getString("error"), Labels.getString("DBError"), Type.ERROR_MESSAGE);
			} else if (response == FALSE) {
			    Notification.show(Labels.getString("error"),
				    Labels.getString("fileAlreadyExists1") + fileName + " " + Labels.getString("fileAlreadyExists2"), Type.ERROR_MESSAGE);
			}
			UI.getCurrent().removeWindow(changeTEIStructWindow);
			selectTEIStructure(selectedProject, mandatory);
			return response;
		    }
		});
	    }
	});
	buttonsLayout.addComponent(uploadTEIStructButton);
	buttonsLayout.setComponentAlignment(uploadTEIStructButton, Alignment.BOTTOM_LEFT);

	if (!mandatory) {
	    // Add Cancel Button
	    Button cancelTEIStructButton = new Button(Labels.getString("Cancel"));
	    cancelTEIStructButton.setDescription(Labels.getString("UploadTEICancelInfo"));
	    cancelTEIStructButton.addClickListener(new ClickListener() {
		private static final long serialVersionUID = 3132731610598024402L;

		@Override
		public void buttonClick(ClickEvent event) {
		    UI.getCurrent().removeWindow(changeTEIStructWindow);
		}
	    });
	    buttonsLayout.addComponent(cancelTEIStructButton);
	    buttonsLayout.setComponentAlignment(cancelTEIStructButton, Alignment.BOTTOM_CENTER);
	}

	// Add confirm Button
	Button confirmButton = new Button(Labels.getString("Save"));
	confirmButton.setDescription(Labels.getString("UploadTEIConfirmInfo"));
	confirmButton.addClickListener(new ClickListener() {
	    private static final long serialVersionUID = 4684926003523699484L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		String tei = (String) chooseTEIStruct.getValue();
		if (tei == null) {
		    Notification.show(Labels.getString("noTEIStructSelected"), Type.WARNING_MESSAGE);
		    return;
		}
		// Update project with TEIStruct
		int response = ERROR;
		try {
			response = getXMLDBManager(TextEditorUI.getCurrent().getUser()).updateProjectTEISchema(env,selectedProject, tei);
		} catch (Exception e) {
			TextEditorUI.getCurrent().updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
			return;
		}
		// There was a problem with the database
		if (response == ERROR) {
		    Notification.show(Labels.getString("DBError"), Type.ERROR_MESSAGE);
		}
		UI.getCurrent().removeWindow(changeTEIStructWindow);
	    }
	});
	buttonsLayout.addComponent(confirmButton);
	buttonsLayout.setComponentAlignment(confirmButton, Alignment.BOTTOM_RIGHT);

	UI.getCurrent().addWindow(changeTEIStructWindow);
    }

    /**
     * If the file is inside the active project, the file selection layout is updated. If not, nothing is done
     * 
     * @param projectId
     *            Id of the project where the file belongs to.
     * @return true if content is updated and false if not.
     */
    public boolean updatedFile(String projectId) {
	if (activeProject != null) {
	    if (activeProject.equals(projectId)) {
		try {
			fileSelectionPanel.setSecondComponent(buildFilesLayout());
		} catch (Exception e) {
			TextEditorUI.getCurrent().updateView(new ServiceNotAvailableView(), NOTATORIZEDVIEW, null);
			return false;
		}
		return true;
	    }
	}
	return false;
    }

}
