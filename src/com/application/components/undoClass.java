package com.application.components;

import java.io.Serializable;
import java.util.ArrayList;

import com.application.components.TEITextStruct.TextStruct;

public class undoClass implements Serializable {
	private static final long serialVersionUID = -3914386068544432459L;
	
	/** List to save the values*/
	ArrayList<TextStruct> linkedList;
	/** Maxium size of the list*/
	int maxSize = 0;
    /** Actual position of the linked list*/
    int listPosition = 0;
	
	public undoClass(int size,TextStruct struct) {
		maxSize = size;
		linkedList = new ArrayList<TextStruct>();
		linkedList.add(listPosition,struct);
	}
	
	public TextStruct add(TextStruct addStruct) {
		//See if max reached
		if (listPosition == (maxSize - 1)) {
			linkedList.remove(0);
		} else {
			listPosition++;
		}
		linkedList.add(listPosition,addStruct);
		//TODO see if are redone structures and delete them
		if (listPosition < (linkedList.size() - 1) ) {
			for (int i=linkedList.size()-1;i>listPosition;i--) {
				linkedList.remove(i);
			}
		}
		return addStruct;
	}
	
	public TextStruct undo() {
		if (listPosition > 0) {
			listPosition--;
		}
		return linkedList.get(listPosition);
	}
	
	public TextStruct redo() {
		if (listPosition < (linkedList.size() - 1) ) {
			listPosition++;
		}
		return linkedList.get(listPosition);
	}
	
	public boolean canUndo() {
		if (listPosition > 0) {
			return true;
		}
		return false;
	}
	
	public boolean canRedo() {
		if (listPosition < ( linkedList.size() - 1 )) {
			return true;
		}
		return false;
	}
	
}
