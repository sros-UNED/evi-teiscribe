package com.application.widgets.client.TextEditorElements;

import java.io.Serializable;

/**
 * Simple structure to send attributes to the server
 * @author Miguel Urízar Salinas
 *
 */
public class AttributeSenderStruct  implements Serializable {
    private static final long serialVersionUID = 6872403207671452213L;
    public String name; 
    public String value;
}
