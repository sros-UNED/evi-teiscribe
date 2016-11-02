package com.application.widgets.client.TextEditor;

/**
 * State of the text editor
 * @author Miguel Urízar Salinas
 *
 */
public class TextEditorState extends com.vaadin.shared.AbstractComponentState {
	private static final long serialVersionUID = 1390417763101845319L;
	
	public String noSavePopupAttribute, savePopupAttribute, errorAtributesPopupAttribute, noAtributesPopupAttribute;
	public String deleteLabelPopupAttribute;
	public String noLabelsPopupLabels;
	public String badSelectionForLabelPopup;
	public String lostCursor;

	
}