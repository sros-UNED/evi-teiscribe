package com.application.components;

import com.application.language.Labels;
import com.application.widgets.TextEditor;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;

/**
 * Class that includes the text editor windows to allow updating files.
 * <p>
 * This tab sheet includes automatically three tabs using a single textStruct, this makes to update the values each time a tab is changed, but the info is not
 * replicated.
 * </p>
 * 
 * @author Miguel Urízar Salinas
 *
 */
public class TextAreaLayout extends TabSheet {
    private static final long serialVersionUID = -9030736473540684223L;

    /** Contains the edition widget */
    private TextEditor textEditor;

    /**
     * Constructor that creates a TextEditor using the DTD and XML file
     * @param reg FileRegistration to interact with the file
     * 
     */
    public TextAreaLayout(FileRegistration reg) {

	setSizeFull();

	// Create the textEditor
	SetTextEditor(new TextEditor(reg,TextEditor.TYPE_TEI));

	// Method to update the tab when selected
	addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
	    private static final long serialVersionUID = -521927836553290618L;

	    @Override
	    public void selectedTabChange(SelectedTabChangeEvent event) {
		// Update the selected tab
		updateTab((CssLayout) getSelectedTab());
	    }
	});

	// Add different tabs. Notice that when the first tab is added, it is
	// selected and the change event is fired, so if you want to catch that,
	// you need to add your listener before adding any tabs.
	addCustomTab(TextEditor.TYPE_TEI, "TEI", "TEITab");
	addCustomTab(TextEditor.TYPE_HEADER, "Header", "headerTab");
	addCustomTab(TextEditor.TYPE_TEXT, "Text", "textTab");
	//TODO punto donde editar con xml el texto.
	//addCustomTab(TextEditor.TYPE_XML, "XML", "XMLTab");
    }

    /**
     * Creates the layout of the tab.
     * <p>
     * Not necessary to fill it because for that there is the Tab Change Listener.
     * </p>
     * 
     * @param typeTei
     *            Defines which part of TEI file to show. Constants defined in {@link TextEditor}.
     * @param caption
     *            Used to name the tab and to know which tab is selected
     */
    private void addCustomTab(int typeTei, String caption, String description) {
	CssLayout tab = new CssLayout();

	tab.setStyleName("text_editor_outer_framework");
	tab.setId(caption);

	Tab tab1 = this.addTab(tab, caption);
	tab1.setDescription(Labels.getString(description));
    }

    /**
     * Method called from Tab Change Listener.
     * <p>
     * Creates the sheet shape content of the tab, updating the type of content to show from TextEditor class
     * </p>
     * 
     * @param tab
     *            Active tab to update
     */
    private void updateTab(CssLayout tab) {

	// Create a panel to scroll
	Panel innerPanel = new Panel();
	// Create internal layout (paper shape)
	CssLayout innerTextAreaLayout = new CssLayout();
	innerTextAreaLayout.setStyleName("text_editor_inner_framework");
	innerPanel.setContent(innerTextAreaLayout);
	// Update the components of the tab
	tab.removeAllComponents();
	tab.addComponent(innerPanel);
	// Check what values to show from the tab (header, text or tei)
	int type;
	switch (tab.getId()) {
	case "TEI":
	    type = TextEditor.TYPE_TEI;
	    break;
	case "Header":
	    type = TextEditor.TYPE_HEADER;
	    break;
	case "Text":
	    type = TextEditor.TYPE_TEXT;
	    break;
	case "XML":
	    type = TextEditor.TYPE_XML;
	    break;
	default:
	    type = TextEditor.TYPE_TEI;
	    break;
	}
	// Update the text to show and link it
	textEditor.modifyText("", type);
	innerTextAreaLayout.addComponent(textEditor);

    }

    /**
     * @return the textEditor
     */
    public TextEditor GetTextEditor() {
	return textEditor;
    }

    /**
     * @param textEditor
     *            the textEditor to set
     */
    public void SetTextEditor(TextEditor textEditor) {
	this.textEditor = textEditor;
    }

}