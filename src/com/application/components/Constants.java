package com.application.components;

import com.vaadin.server.VaadinServlet;

/**
 * Class that only contains constants
 * 
 * @author Miguel Urízar Salinas
 *
 */
public final class Constants {

    /** Error handling */
    public static final int ERROR = -1;
    public static final int TRUE = 0;
    public static final int FALSE = 1;

    /** State of the UI */
    public static final int LOGINVIEW = 0;
    public static final int REGISTERVIEW = 1;
    public static final int EDITIONVIEW = 2;
    public static final int FILESELECTIONVIEW = 3;
    public static final int NOTATORIZEDVIEW = 4;

    /** For defining attributes in the HTML that are intrinsic of the label, we define a prefix */
    public static final String LABELATTRIBUTEPREFIX = "labelattribute_";
    /** Cool down of the timer to send changes in text editor to the server */
    public static final int TIMERCOOLDOWN = 800;

    /** WhiteSpace used for generating the beginning spaces of XML generated files */
    public static final String XMLWHITESPACE = " ";

    /** Max size of the file to upload */
    public static final long MAXUPLOADSIZE = 1048576;
    
    /** Link to the manual*/
    public static final String MANUALURL = "http://linhd.uned.es/wp-content/uploads/2016/06/Manual-TEIScribe.pdf";
    
    /** Path of upload XML icon */
    public static final String UPLOADXMLICON = "img/upXML.ico";
    /** Path of download XML icon */
    public static final String DOWNLOADXMLICON = "img/downXML.ico";
    /** Path of delete icon */
    public static final String DELETEICON = "img/delete.ico";
    /** Path of rename icon */
    public static final String RENAMEICON = "img/changeName.ico";
    /** Path of DTD change icon */
    public static final String TEISTRUCTCHANGEICON = "img/DTDChange.ico";
    /** Path of create Project icon */
    public static final String CREATEPROJECTICON = "img/createProject.ico";
    /** Path of logout icon */
    public static final String LOGOUTICON = "img/logout.ico";
    /** Path of menu icon */
    public static final String MENUICON = "img/menu.ico";
    /** Path of new file icon */
    public static final String NEWFILEICON = "img/newFile.ico";
    /** Path of export file icon */
    public static final String EXPORTFILEICON = "img/export.png";
    /** Redo icon */
    public static final String REDOICON = "img/redo.png";
    /** Undo icon */
    public static final String UNDOICON = "img/undo.png";
    /** Manual icon */
    public static final String MANUALICON = "img/manual.ico";
    
    /** Path of login TEIScribe image */
    public static final String LOGINLOGOIMAGE = "img/loginMain.png";
    /** Path of register TEIScribe image */
    public static final String REGISTERLOGOIMAGE = "img/registerMain.png";
    
    /** User credentials*/
    public static final String USERNAME = "teiscribe";
    public static final String PASSWORD = "bieses";
    
    
    /** Database credentials*/
    
    //Production
    /*public static final String EXISTCOLLECTION = "xmldb:exist://localhost:8888/exist/xmlrpc/db/apps/";
    public static final String URISQL = "jdbc:mysql://localhost:3306/evi";
    public static final String USERSQL = "evimysql";
    public static final String PASSWORDSQL = "Hffk2!9k";*/
    
    //Devel
    public static final String EXISTCOLLECTION = "xmldb:exist://evilinhd.com:8888/exist/xmlrpc/db/apps/";
    public static final String URISQL = "jdbc:mysql://91.146.100.83:3306/evi";
    public static final String USERSQL = "murizar";
    public static final String PASSWORDSQL = "M4n8Arza";
    
    /** EVILINHD*/
    public static final String EXISTUSER = "admin";
    //public static final String EXISTPASS = "4dm1n";
    public static final String EXISTPASS = "5dm1nsEV";
    public static final String DEMOSESSION = "prueba";

    /** Bieses*/
    /*public static final String EXISTCOLLECTION = "xmldb:exist://linhd.es:8888/exist/xmlrpc/db/Bieses/";
    public static final String EXISTUSER = "bieses";
    public static final String EXISTPASS = "lun3sb13s3s";
    public static final String DEMOSESSION = "bieses";*/
    
    
    /**Jwt*/
    public static final String SECRET = "bTQzsaBsbEuAfTma6c7EkdLnP1X4F11oeK";
    
    /** Database for teiscribe in existdb*/
    public static final String TEIDATABASEFOLDER = "teiscribe";
    
    /** Generic TEI for each project*/
    public static final String GENERICTEIPROJECT = "TEI";
    public static final String GENERICTEIFILE = "default-schema.dtd";
    public static final String TEIPROJECTNAME = "schema.dtd";
    
    /** Generic folder for TeiSimple*/
    public static final String GENERICTEISIMPLEPROJECT = "tei-simple";
    public static final String TEISIMPLEPROJECTNAME = "test";

    /** Static XML head for the TEI xml files */
    public static final String XMLHEAD1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    
    /** Static XML head for the TEI DTD definition */
    public static final String XMLHEAD2DTD1 = "<?xml-model href=\"";
    public static final String XMLHEAD2DTD2 = "\" type=\"application/xml-dtd\"?>\n";
    
    /** Constants for DTDManager and TextStruct */
    public static final int FILETYPE = 0;
    public static final int STRINGTYPE = 1;
    
    /** Size of the undo button*/
    public static final int UNDOMAXSIZE = 10;
    
    /**
     * @return the XMLdataBaseManager
     */
    public static final XMLDBManager getXMLDBManager(String user) {
	return (XMLDBManager) VaadinServlet.getCurrent().getServletContext().getAttribute("XMLdataBaseManager"+user);
    }

}