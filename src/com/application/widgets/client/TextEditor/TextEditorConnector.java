package com.application.widgets.client.TextEditor;

import java.util.ArrayList;

import static com.application.components.Constants.TIMERCOOLDOWN;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;
import com.application.widgets.TextEditor;
import com.application.widgets.client.TextEditor.TextEditorClientRpc;
import com.application.widgets.client.TextEditor.TextEditorServerRpc;
import com.application.widgets.client.TextEditor.TextEditorState;
import com.application.widgets.client.TextEditor.TextEditorWidget;
import com.application.widgets.client.TextEditorElements.AttributePopupMenu;
import com.application.widgets.client.TextEditorElements.CursorPosition;
import com.application.widgets.client.TextEditorElements.ClientHumanizedMessage;
import com.application.widgets.client.TextEditorElements.LabelPopupMenu;
import com.application.widgets.client.TextEditorElements.ClientHumanizedMessage.MESSAGE_TYPE;
import com.vaadin.client.communication.RpcProxy;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.vaadin.client.communication.StateChangeEvent;

@Connect(TextEditor.class)
public class TextEditorConnector extends AbstractComponentConnector {
    private static final long serialVersionUID = 2319297555940481059L;

    /** Rpc to communicate client -> server */
    public TextEditorServerRpc rpc = RpcProxy.create(TextEditorServerRpc.class, this);
    /** Used to not send every keystroke, but in blocks */
    Timer keyPressTimer = null;
    /** Popup menu to create labels */
    public LabelPopupMenu popupLabelMenu = null;
    /** Popup menu to edit or delete labels */
    public AttributePopupMenu popupAttributeMenu = null;
    /** Variables to update create and delete labels */
    private int beginTextId, endTextId, beginTextOffset, endTextOffset, addLabelId;
    /** Defines if we are changing a label name or creating new one */
    public boolean changeLabel;
    /** Variable to save the node where delete starts */
    private Element startingModifiedText = null;
    /** Variable to save the id of the node where delete starts */
    private int startingModifiedTextId;
    /** To know if is beginning or ending text */
    private boolean startingModifiedTextIsBeginning = false;
    /** Defines the cursor */
    private CursorPosition cursorPosition = new CursorPosition();

    /**
     * Build a new TextEditor connector.
     * <p>
     * Creates the client rpc and adds handlers for the context menu with right click and for the key press (down up and press)
     * </p>
     */
    public TextEditorConnector() {

	/* Create the client rpc */
	registerRpc(TextEditorClientRpc.class, new TextEditorClientRpc() {
	    private static final long serialVersionUID = 3269309481621053786L;

	    /* Function or generic alerts */
	    public void alert(String message) {
		Window.alert(message);
	    }

	    /* Create popup menu for creating labels */
	    @Override
	    public void createLabelsPopupMenu(ArrayList<String> labels) {
		popupLabelMenu.createLabelsPopupMenu(labels);
	    }

	    /* Create he popup menu for editing or removing a label */
	    @Override
	    public void createAttributesPopupMenu(ArrayList<String> attributes, int id, String name) {
		popupAttributeMenu.createAttributesPopupMenu(attributes, id, name);
	    }

	    /* Update the text of the text editor */
	    @Override
	    public void modifyTextHTML(String innerHTML) {
		getWidget().modifyTEIText(innerHTML);
	    }

	    /* Set the cursor position*/
		@Override
		public void setCursorPosition() {
			Element startingNode = Element.as(getWidget().getStartNode());
			Element startingNodeParent = Element.as(startingNode.getParentElement());
			cursorPosition.startingNodeId = startingNodeParent.getId();
			cursorPosition.startingNodeLabel = startingNodeParent.getAttribute("label");
			int nodes = startingNodeParent.getChildCount();
			// Extract the child number
			for (int i=0;i<nodes;i++) {
				if (startingNodeParent.getChild(i).equals(startingNode)) {
					cursorPosition.startingNodeChild = i;
					i = nodes;
				}
			}
			
			Element endingNode = Element.as(getWidget().getEndNode());
			Element endingNodeParent = Element.as(endingNode.getParentElement());
			cursorPosition.endingNodeId = endingNodeParent.getId();
			cursorPosition.endingNodeLabel = endingNodeParent.getAttribute("label");
			nodes = endingNodeParent.getChildCount();
			// Extract the child number
			for (int i=0;i<nodes;i++) {
				if (endingNodeParent.getChild(i).equals(endingNode)) {
					cursorPosition.endingNodeChild = i;
					i = nodes;
				}
			}
			cursorPosition.startingOffset = getWidget().getStartSelection();
			cursorPosition.endingOffset = getWidget().getEndSelection();
		}

		@Override
		public void getCursorPosition() {
			//Check the starting node cursor position
			Element startingNode = DOM.getElementById(cursorPosition.startingNodeId);
			if (!startingNode.getAttribute("label").equals(cursorPosition.startingNodeLabel))
			{
				startingNode = DOM.getElementById("1");
				getWidget().setPosition(startingNode,startingNode,1,1);
				Window.alert(getState().lostCursor);
				return;
			}
			//Check the ending node cursor position
			Element endingNode = DOM.getElementById(cursorPosition.endingNodeId);
			if (!endingNode.getAttribute("label").equals(cursorPosition.endingNodeLabel))
			{
				getWidget().setPosition(startingNode,startingNode,cursorPosition.startingOffset,cursorPosition.startingOffset);
				Window.alert(getState().lostCursor);
				return;
			}
			getWidget().setPosition(startingNode.getChild(cursorPosition.startingNodeChild),
					DOM.getElementById(cursorPosition.endingNodeId).getChild(cursorPosition.endingNodeChild), 
					cursorPosition.startingOffset, cursorPosition.endingOffset);
			//Window.alert(cursorPosition.startingNodeLabel + " id " + cursorPosition.startingNodeId + " child " + cursorPosition.startingNodeChild + " offset " + cursorPosition.startingOffset + "\n\r" + 
			//		cursorPosition.endingNodeLabel + " id " + cursorPosition.endingNodeId + " child " + cursorPosition.endingNodeChild + " offset " + cursorPosition.endingOffset);
		}
	});

	/*
	 * When clicking left mouse button, if over a label, edition menu appears
	 */
	getWidget().addMouseUpHandler(new MouseUpHandler() {
	    public void onMouseUp(MouseUpEvent event) {
		int button = event.getNativeEvent().getButton();
		// If we pushed right button
		if (button == NativeEvent.BUTTON_RIGHT) {

		    // Button left
		} else if (button == NativeEvent.BUTTON_LEFT) {
		    MouseLeftClicked(event);
		}
	    }
	});

	/* When clicking right button, open create label menu */
	getWidget().sinkEvents(Event.ONCONTEXTMENU);
	getWidget().addHandler(new ContextMenuHandler() {
	    @Override
	    public void onContextMenu(ContextMenuEvent event) {
		createContextMenu(event);
	    }
	}, ContextMenuEvent.getType());

	/* Handler to not write the values not allowed and update values */
	getWidget().addKeyPressHandler(new KeyPressHandler() {
	    public void onKeyPress(KeyPressEvent event) {
		int keyCode = event.getCharCode();
		// Supress values not allowed
		if (getWidget().valuesNotAllowed(keyCode)) {
		    event.preventDefault();
		    event.stopPropagation();
		    // We have values that modify
		} else {
		    startTimer();
		}
	    }
	});

	/*
	 * Handler for when hitting backspace and going to other label, update both. We need to save the id of the label in case we delete whole label (now
	 * exists but afterwards would be impossible to access)
	 */
	getWidget().addKeyDownHandler(new KeyDownHandler() {

	    @Override
	    public void onKeyDown(KeyDownEvent event) {
		int keyPressed = event.getNativeKeyCode();
		if ((keyPressed == KeyCodes.KEY_BACKSPACE) || (keyPressed == KeyCodes.KEY_DELETE)) {
		    // Save the id for future changes
		    startingModifiedText = Element.as(getWidget().getStartNode());
		    Element previous = startingModifiedText.getPreviousSiblingElement();
		    // If there is not a previous node is the beginning of the
		    // text
		    if (previous == null) {
			startingModifiedTextIsBeginning = true;
			startingModifiedTextId = Integer.parseInt(startingModifiedText.getParentElement().getId());
			// If there is a previous node this text goes after it
		    } else {
			startingModifiedTextIsBeginning = false;
			startingModifiedTextId = Integer.parseInt(previous.getId());
		    }
		}
		// Catch cut and paste
		if (event.isControlKeyDown() && ((keyPressed == KeyCodes.KEY_V) || (keyPressed == KeyCodes.KEY_X))) {
		    startTimer();
		}
	    }

	});

	// To catch ctrl + v and delete, backspace
	getWidget().addKeyUpHandler(new KeyUpHandler() {
	    @Override
	    public void onKeyUp(KeyUpEvent event) {
		int keyPressed = event.getNativeKeyCode();
		// Catch backspace an delete
		if ((keyPressed == KeyCodes.KEY_BACKSPACE) || (keyPressed == KeyCodes.KEY_DELETE)) {
		    startTimer();
		}
	    }
	});
    }

    /**
     * Function that creates the contextMenu when right clicked some text.
     * <p>
     * It is not created when the selection is not allowed
     * </p>
     * 
     * @param event
     *            Event generated from the right click
     */
    public void createContextMenu(ContextMenuEvent event) {
	event.preventDefault();
	event.stopPropagation();
	// Check if the text corresponds the same node
	Element startNode = Element.as(getWidget().getStartNode());
	Element endNode = Element.as(getWidget().getEndNode());

	// Extract the id of the parent node to know in which node we start
	addLabelId = Integer.parseInt(startNode.getParentElement().getId());

	// The selection starts from the same node, if not there is an error for
	// xml format
	if (startNode.getParentElement().getId() == endNode.getParentElement().getId()) {
	    // We are not chaging a label
	    changeLabel = false;
	    // Extract the id to know the position of the beginning of selection
	    Element previousStartNode = startNode.getPreviousSiblingElement();
	    if (previousStartNode == null) {
		beginTextId = -1;
	    } else {
		beginTextId = Integer.parseInt(previousStartNode.getId());
	    }

	    // Extract the id to know the position of the end of selection
	    Element previousEndNode = endNode.getPreviousSiblingElement();
	    if (previousEndNode == null) {
		endTextId = -1;
	    } else {
		endTextId = Integer.parseInt(previousEndNode.getId());
	    }

	    // Extract the offsets and remove the space at the beginning
	    beginTextOffset = getWidget().getStartSelection();
	    if (beginTextOffset > 0) beginTextOffset--;
	    endTextOffset = getWidget().getEndSelection();
	    if (endTextOffset > 0) endTextOffset--;

	} else {
	    ClientHumanizedMessage.showMessageAndFadeAfterMouseMove(getState().badSelectionForLabelPopup, MESSAGE_TYPE.WARNING);
	    return;
	}
	createLabelPopupMenu(startNode.getParentElement().getAttribute("label"), event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
    }

    /**
     * Creates a labelPopup menu and then fills it with sub labels allowed inside that label
     * 
     * @param label
     *            Name of the label to search for the child labels
     * @param x
     *            Width of the window
     * @param y
     *            Height of the window
     */
    public void createLabelPopupMenu(String label, int x, int y) {
	if (popupLabelMenu == null) {
	    popupLabelMenu = new LabelPopupMenu(this, getState().noLabelsPopupLabels);
	}
	popupLabelMenu.setPopupVariablePositon(y, x);
	rpc.getSubElements(label);
    }

    /**
     * Creates an attributePopup menu.
     * <p>
     * This menu allows to change the label, delete it or change Attributes
     * </p>
     * 
     * @param event
     *            The left click event needed
     */
    public void askForAttributes(MouseUpEvent event) {
	event.preventDefault();
	event.stopPropagation();
	Element position = Element.as(event.getNativeEvent().getEventTarget());
	// If the selected element is not a label element we return
	if (position.getId() == "")
	    return;

	String label = position.getAttribute("label");
	int id = Integer.parseInt(position.getAttribute("id"));
	if (popupAttributeMenu == null) {
	    popupAttributeMenu = new AttributePopupMenu(this, getState().noSavePopupAttribute, getState().savePopupAttribute,
		    getState().errorAtributesPopupAttribute, getState().noAtributesPopupAttribute, getState().deleteLabelPopupAttribute);
	}

	popupAttributeMenu.setPopupVariablePositon(event.getClientY(), event.getClientX());
	rpc.getSubAttributes(label, id);
    }

    /**
     * Function that, if over a label, asks to the server for the attributes
     * 
     * @param event
     *            Event generated when left clicked
     */
    public void MouseLeftClicked(MouseUpEvent event) {
	Element position = Element.as(event.getNativeEvent().getEventTarget());

	position.getAbsoluteTop();
	position.getAbsoluteBottom();
	event.getClientX();
	if ((position.getAbsoluteTop() + position.getClientHeight() - 10 - event.getClientY()) < 1) {
	    askForAttributes(event);
	}
    }

    @Override
    protected Widget createWidget() {
	return GWT.create(TextEditorWidget.class);
    }

    @Override
    public TextEditorWidget getWidget() {
	return (TextEditorWidget) super.getWidget();
    }

    @Override
    public TextEditorState getState() {
	return (TextEditorState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
	super.onStateChanged(stateChangeEvent);

    }

    /**
     * Creates or changes a label
     * 
     * @param label
     *            Label to make the changes
     */
    public void createLabel(String label) {

	// Change a label
	if (changeLabel == true) {
	    rpc.changeLabel(addLabelId, label);
	    // Create a label
	} else {
	    // Check if the spaces are forced or real:

	    rpc.createLabel(addLabelId, label, beginTextId, beginTextOffset, endTextId, endTextOffset);
	}
    }

    /**
     * Starts restarts or deletes the timer updating to the server changes.
     */
    private void startTimer() {
	if (keyPressTimer == null) {
	    keyPressTimer = new Timer() {
		@Override
		public void run() {

		    Element modifiedText = Element.as(getWidget().getStartNode());
		    // Check if we started with a backspace
		    if (startingModifiedText != null) {
		    	updateDeleteText();
		    	startingModifiedText = null;
		    }
		    // Update the element where the selector is
		    updateText(modifiedText);


		    // Cancel the timer
		    keyPressTimer.cancel();
		    keyPressTimer = null;
		}
	    };
	    keyPressTimer.schedule(TIMERCOOLDOWN);
	}
	// Timer already set so restart time
	else {
	    keyPressTimer.schedule(TIMERCOOLDOWN);
	}
    }

    /**
     * Setter for the addLabelId
     * 
     * @param id
     *            Id to set
     */
    public void setAddLabelId(int id) {
	addLabelId = id;
    }

    /**
     * Updates in the server the text defined in modifedText Element
     * 
     * @param modifiedText
     *            Element that contains the text to update
     */
    private void updateText(Element modifiedText) {
	Element previous = modifiedText.getPreviousSiblingElement();
	// If there is not a previous node is the beginning of the text
	if (previous == null) {
	    rpc.ModifyBeginningText(modifiedText.getInnerText().replace((char) 0xA0, (char) 0x20), Integer.parseInt(modifiedText.getParentElement().getId()));
	    // If there is a previous node this text goes after it
	} else {
	    rpc.ModifyEndingText(modifiedText.getInnerText().replace((char) 0xA0, (char) 0x20), Integer.parseInt(previous.getId()));
	}
    }

    /**
     * Updates when deleting text.
     * <p>
     * If during deletion we invade other label, update both labels even if text is empty.
     * </p>
     */
    private void updateDeleteText() {
	String text = "";
	// It has a parent element so is not afterText
	if (startingModifiedText.getParentElement() != null) {
	    // If its parent is null is an empty beggining text
	    if (startingModifiedText.getParentElement().getParentElement() == null) {
		text = "";
		// Is not an empty label
	    } else {
		text = startingModifiedText.getInnerText().replace((char) 0xA0, (char) 0x20);
	    }
	}
	if (startingModifiedTextIsBeginning) {
	    rpc.ModifyBeginningTextNoChanges(text, startingModifiedTextId);
	} else {
	    rpc.ModifyEndingTextNoChanges(text, startingModifiedTextId);
	}
    }

    /**
     * Delete label with selected id
     * 
     * @param id
     *            Id of the label to remove
     */
    public void deleteLabel(int id) {
	rpc.removeLabel(id);
    }
}
