package com.application.widgets.client.TextEditor;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Widget for the text editor
 * 
 * @author Miguel Urízar Salinas
 *
 */
public class TextEditorWidget extends FocusPanel {

    /** Panel in HTML format to show the labels and text*/
    private HTMLPanel TEIText = new HTMLPanel("");
    /** Last selected element to remove the highlight on it*/
    private Element lastElement = null;

    /**
     * Text Editor widget constructor. Implements the text HTML and implements the mouse cursor change.
     */
    public TextEditorWidget() {

	setStyleName("text_editor");
	add(TEIText);
	TEIText.setStyleName("text_editor_inside");

	this.addMouseOutHandler(new MouseOutHandler() {

	    @Override
	    public void onMouseOut(MouseOutEvent event) {
		if (lastElement != null) {
		    String id = lastElement.getAttribute("rootElement");
		    // We are in the last element that must not have color
		    if ((id.isEmpty()) || (id.equalsIgnoreCase("0"))) {
			lastElement.getStyle().setBackgroundColor("rgba(255, 255, 255, 0.1)");
		    } else {
			lastElement.getStyle().setBackgroundColor("rgba(30, 30, 30, 0.1)");
		    }

		    lastElement = null;
		}
	    }
	});

	/*
	 * Add a handler to make the cursor change depending on selecting a label after the text or the text itself
	 */
	this.addMouseMoveHandler(new MouseMoveHandler() {

	    public void onMouseMove(MouseMoveEvent event) {
		if (lastElement != null) {
		    String id = lastElement.getAttribute("rootElement");
		    // We are in the last element that must not have color
		    if ((id.isEmpty()) || (id.equalsIgnoreCase("0"))) {
			lastElement.getStyle().setBackgroundColor("rgba(255, 255, 255, 0.1)");
		    } else {
			lastElement.getStyle().setBackgroundColor("rgba(30, 30, 30, 0.1)");
		    }
		}
		Element position = Element.as(event.getNativeEvent().getEventTarget());
		lastElement = position;

		if ((position.getAbsoluteTop() + position.getClientHeight() - 10 - event.getClientY()) < 1) {
		    position.getStyle().setCursor(Cursor.POINTER);
		    position.getStyle().setBackgroundColor("rgba(30, 200, 30, 0.1)");
		} else {
		    position.getStyle().setCursor(Cursor.TEXT);
		    String id2 = position.getAttribute("rootElement");
		    if ((id2.isEmpty()) || (id2.equalsIgnoreCase("0"))) {
			position.getStyle().setBackgroundColor("rgba(255, 255, 255, 0.1)");
		    } else {
			position.getStyle().setBackgroundColor("rgba(30, 30, 30, 0.1)");
		    }

		}
	    }
	});

    }

    /**
     * Extract index of the beginning of the selection https://developer.mozilla.org/en-US/docs/Web/API/range
     * 
     * @return the offset of the selection
     */
    native int getStartSelection() /*-{
				   var start = "";
				   if ( $wnd.getSelection() && $wnd.getSelection().rangeCount > 0) {
				   start = $wnd.getSelection().getRangeAt(0).startOffset ;
				   }
				   return start;
				   }-*/;

    /**
     * Extract index of the end of the selection
     * 
     * @return the offset of the selection
     */
    native int getEndSelection() /*-{
				 var start = "";
				 if ( $wnd.getSelection() && $wnd.getSelection().rangeCount > 0) {
				 start = $wnd.getSelection().getRangeAt(0).endOffset ;
				 }
				 return start;
				 }-*/;

    /**
     * Extract node of the beginning of the selection
     * 
     * @return The node where the text starts
     */
    native Node getStartNode() /*-{
			       var start = "";
			       if ( $wnd.getSelection() && $wnd.getSelection().rangeCount > 0) {
			       //tipo de nodo
			       start = $wnd.getSelection().getRangeAt(0).startContainer;
			       }
			       return start;
			       }-*/;

    /**
     * Extract node of the end of the selection
     * 
     * @return The node where the text ends
     */
    native Node getEndNode() /*-{
			     var start = "";
			     if ( $wnd.getSelection() && $wnd.getSelection().rangeCount > 0) {
			     //Valor del nodo, en texto es el texto de dentro
			     start = $wnd.getSelection().getRangeAt(0).endContainer;
			     }
			     return start;
			     }-*/;

    /**
     * Extract selected text
     * 
     * @return String with the selected text
     */
    native String getSelection() /*-{
				 var text = "";
				 if ( $wnd.getSelection() && $wnd.getSelection().rangeCount > 0) {
				 text = $wnd.getSelection().getRangeAt(0).toString();
				 } else if ($doc.selection && $doc.selection.type != "Control") {
				 text = $doc.selection.createRange().text;
				 }
				 return text;
				 }-*/;
    
    /**
     * Function that returns true for characters not allowed and false for characters allowed for the text editor modifiers
     * 
     * @param value
     *            Char value to check if is allowed or not
     * @return True if value is not allowed and false if value is allowed
     */
    public boolean valuesNotAllowed(int value) {
	// Eliminate values not needed
	if (value < 32)
	    return true;
	else
	    return false;
    }

    /**
     * Updates the text showing in the text editor
     * 
     * @param innerHTML
     *            HTML to show in the text editor. It has to have the desired format.
     */
    public void modifyTEIText(String innerHTML) {
    	remove(TEIText);
    	lastElement = null;
    	TEIText = new HTMLPanel(innerHTML);
    	add(TEIText);
    }

    native void setPosition(Node startNode, Node endNode, int startOffset, int endOffset) /*-{
    	if ($doc.selection && $doc.selection.createRange) {
    		var range = $doc.selection.createRange();
    		range.setStart(startNode, startOffset);
		range.setEnd(endNode, endOffset);
		range.select();
	} else if ($doc.createRange && $wnd.getSelection) {
		var range = $doc.createRange();
		range.setStart(startNode, startOffset);
		range.setEnd(endNode, endOffset);
    		var selection = $wnd.getSelection();
    		selection.removeAllRanges();
    		selection.addRange(range);
	}
    }-*/;

}