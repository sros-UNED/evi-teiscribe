package com.application.language;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Class to control the language of the system. A file with *.properties defines the language constants. The language is controlled automatically by the
 * browser.
 * 
 * @author Miguel Urízar Salinas
 *
 */
public class Labels {

    private static final String BUNDLE_NAME = "com.application.language.labels"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Creates new label struct
     */
    private Labels() {
    }

    /**
     * Function to get the string in the language defined by the browser
     * 
     * @param key
     *            Key name for the string that is asked
     * @return String defined by the key in the language defined by the browser
     */
    public static String getString(String key) {
	try {
	    return RESOURCE_BUNDLE.getString(key);
	} catch (MissingResourceException e) {
	    return '!' + key + '!';
	}
    }
}