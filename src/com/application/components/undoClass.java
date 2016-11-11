package com.application.components;

import java.io.Serializable;
import java.util.ArrayList;

import com.application.components.TEITextStruct.TextStruct;

/**
 * 
 * Allows undo and redo functions saving copies of the textStruct in an array
 * 
 * @author Miguel Urízar Salinas
 *
 */
public class undoClass implements Serializable {
    private static final long serialVersionUID = -3914386068544432459L;

    /** List to save the values */
    ArrayList<TextStruct> linkedList;
    /** Maxium size of the list */
    int maxSize = 0;
    /** Actual position of the linked list */
    int listPosition = 0;

    /**
     * Constructor for undoClass
     * @param size Times to allow redo and undo
     * @param struct textStruct to take as actual
     */
    public undoClass(int size, TextStruct struct) {
	maxSize = size;
	linkedList = new ArrayList<TextStruct>();
	linkedList.add(listPosition, struct);
    }
    
    /**
     * Adds new modification to the class
     * @param addStruct TextStruct to add
     * @return struct saved
     */
    public TextStruct add(TextStruct addStruct) {
	// See if max reached
	if (listPosition == (maxSize - 1)) {
	    linkedList.remove(0);
	} else {
	    listPosition++;
	}
	linkedList.add(listPosition, addStruct);
	if (listPosition < (linkedList.size() - 1)) {
	    for (int i = linkedList.size() - 1; i > listPosition; i--) {
		linkedList.remove(i);
	    }
	}
	return addStruct;
    }
    
    /**
     * Returns last saved struct
     * @return Last saved struct
     */
    public TextStruct undo() {
	if (listPosition > 0) {
	    listPosition--;
	}
	return linkedList.get(listPosition);
    }
    
    /**
     * Returns last undone struct
     * @return Last undone struct
     */
    public TextStruct redo() {
	if (listPosition < (linkedList.size() - 1)) {
	    listPosition++;
	}
	return linkedList.get(listPosition);
    }

    /**
     * Returns if it is possible to make undo
     * @return True if is possible to undo and false if not
     */
    public boolean canUndo() {
	if (listPosition > 0) {
	    return true;
	}
	return false;
    }

    /**
     * Returns if it is possible to make redo
     * @return True if is possible to redo and false if not
     */
    public boolean canRedo() {
	if (listPosition < (linkedList.size() - 1)) {
	    return true;
	}
	return false;
    }

}
