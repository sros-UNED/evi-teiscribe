package com.application.widgets.client.TextEditorElements;

import java.io.Serializable;

/**
 * Simple structure to save cursor position
 * @author Miguel Urízar Salinas
 *
 */
public class CursorPosition implements Serializable {
	private static final long serialVersionUID = -3998096772149692323L;
	public String startingNodeId;
	public String startingNodeLabel;
	public int startingNodeChild;
	public String endingNodeId;
	public String endingNodeLabel;
	public int endingNodeChild;
	public int startingOffset;
	public int endingOffset;
}
