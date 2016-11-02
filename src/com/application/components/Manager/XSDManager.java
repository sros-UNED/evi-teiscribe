package com.application.components.Manager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.vaadin.server.VaadinService;
import com.vaadin.ui.NativeSelect;

/**
 * Generic class not implemented for using an xsd format instead of other. Will implement the FileManager interface, but in a future, now does not have it
 * 
 * @author Miguel Urízar Salinas
 *
 */
public class XSDManager implements Serializable {
    private static final long serialVersionUID = -906356545638758742L;

    static String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
    private static String path = basepath + "files/tei_all." + "xsd";

    public XSDManager() {
	readDocXerces();
    }

    public static NativeSelect addNames(NativeSelect label, String relativePath) {
	Document doc;
	doc = newDocXerces(relativePath);
	readDoc(doc);
	// Obtenemos la etiqueta raiz
	Element elementRaiz = doc.getDocumentElement();
	// Iteramos sobre sus hijos
	NodeList hijos = elementRaiz.getChildNodes();
	for (int i = 0; i < hijos.getLength(); i++) {
	    Node nodo = hijos.item(i);
	    if (nodo instanceof Element) {
		label.addItem(((Element) nodo).getAttribute("name"));
	    }
	}
	return label;
    }

    public static void readDoc(Document doc) {
	// Obtenemos la etiqueta raiz
	Element elementRaiz = doc.getDocumentElement();
	// Iteramos sobre sus hijos
	NodeList hijos = elementRaiz.getChildNodes();
	for (int i = 0; i < hijos.getLength(); i++) {
	    Node nodo = hijos.item(i);
	    if (nodo instanceof Element) {
	    }
	}
    }

    public static void readDocJaxp() {
	Document doc;
	try {
	    doc = newDocJaxp(path);
	    readDoc(doc);
	} catch (ParserConfigurationException | SAXException | IOException e) {
	}
    }

    public static void readDocXerces() {
	Document doc;
	doc = newDocXerces(path);
	readDoc(doc);

    }

    public static Document newDocJaxp(String path) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException {
	// Construimos nuestro DocumentBuilder
	DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	// Procesamos el fichero XML y obtenemos nuestro objeto Document
	Document doc = documentBuilder.parse(new InputSource(new FileInputStream(path)));

	return doc;
    }

    public static Document newDocXerces(String path) {
	Document doc = null;
	// Creamos el parseador
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db;
	try {
	    db = dbf.newDocumentBuilder();
	    doc = db.parse(new FileInputStream(path));
	} catch (Exception e) {
	}

	return doc;
    }

    /**
     * @return Path of the xsd file
     */
    public String getPath() {
	return path;
    }

    /**
     * @param path
     *            Path of the xsd file to set
     */
    public void setPath(String path) {
	XSDManager.path = path;
    }

}
