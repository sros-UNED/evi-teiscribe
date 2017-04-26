package com.application.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.vaadin.server.VaadinServlet;

/**
 * Class that only contains constants
 * 
 * @author Miguel Ur�zar Salinas
 *
 */
public final class Constants {

	
	private static class YAMLConfig {
			
		
		/**
		 * This function returns the value for the key and subkey given as parameters. This reads a yaml file*/
		static String getParam(String key1, String key2) {
				
			Yaml yaml = new Yaml();
	    	
			String param = "";
			

			
			//System.out.println(key1 + " " + key2);
			
			
			try 
			{
				// Data in the YAML file are read as Object subclasses (String or Integer, in our case). 
				Map<String, Map<String, Object>> values = (Map<String, Map<String, Object>>) yaml
						.load(new FileInputStream(new File("/opt/software/evi/hardconfig/settings.yml")));
		
						
				// Get the values from the yaml file for the key and subkey.
				
				//System.out.println(key1 + key2 + " : " +values.get(key1).get(key2).toString());
				
				param = values.get(key1).get(key2).toString();
			}
			catch (FileNotFoundException e)
			{
				System.out.println(e);
				
			}
			
			
			//System.out.println(key1+ " " +key2 + " : " + param);
			
			return param; 
		}
			
		
	} 
	
	
	
    /** Error handling */
    public static final int ERROR = -1;
	//public static final int ERROR = Integer.parseInt(YAMLConfig.getParam("existdb", "error")); 
	
	
    public static final int TRUE = 0;
    //public static final int TRUE = Integer.parseInt(YAMLConfig.getParam("existdb", "true")); 
	
    public static final int FALSE = 1;
    // public static final int FALSE = Integer.parseInt(YAMLConfig.getParam("existdb", "false")); 
	
    /** State of the UI */
    // public static final int LOGINVIEW = 0;
    public static final int LOGINVIEW = Integer.parseInt(YAMLConfig.getParam("existdb", "loginview")); 
    
    
    // public static final int REGISTERVIEW = 1;
    public static final int REGISTERVIEW = Integer.parseInt(YAMLConfig.getParam("existdb", "registerview")); 
    
    
    // public static final int EDITIONVIEW = 2;
    public static final int EDITIONVIEW = Integer.parseInt(YAMLConfig.getParam("existdb", "editionview")); 
    
    
    // public static final int FILESELECTIONVIEW = 3;
    public static final int FILESELECTIONVIEW = Integer.parseInt(YAMLConfig.getParam("existdb", "fileselectionview")); 
    
    
    // public static final int NOTATORIZEDVIEW = 4;
    public static final int NOTATORIZEDVIEW = Integer.parseInt(YAMLConfig.getParam("existdb", "notatorizedview")); 
    
    
    /** For defining attributes in the HTML that are intrinsic of the label, we define a prefix */
    // public static final String LABELATTRIBUTEPREFIX = "labelattribute_";
    public static final String LABELATTRIBUTEPREFIX = YAMLConfig.getParam("existdb", "labelattributeprefix");
    
    /** Cool down of the timer to send changes in text editor to the server */
    // public static final int TIMERCOOLDOWN = 800;
    public static final int TIMERCOOLDOWN = Integer.parseInt(YAMLConfig.getParam("existdb", "timercooldown")); 
    

    /** WhiteSpace used for generating the beginning spaces of XML generated files */
    public static final String XMLWHITESPACE = " ";

    /** Max size of the file to upload */
    // public static final long MAXUPLOADSIZE = 1048576;
    
    public static final long MAXUPLOADSIZE = Integer.parseInt(YAMLConfig.getParam("existdb", "maxuploadsize")); 
    
    
    /** Link to the manual*/
    // public static final String MANUALURL = "http://linhd.uned.es/wp-content/uploads/2016/06/Manual-TEIScribe.pdf";
    public static final String MANUALURL = YAMLConfig.getParam("existdb", "manualurl");

    
    /** Path of upload XML icon */
    // public static final String UPLOADXMLICON = "img/upXML.ico";
    public static final String UPLOADXMLICON  = YAMLConfig.getParam("existdb", "uploadxmlicon");
    
    
    
    /** Path of download XML icon */
    // public static final String DOWNLOADXMLICON = "img/downXML.ico";
    public static final String DOWNLOADXMLICON  = YAMLConfig.getParam("existdb", "downloadxmlicon");
    
    
    
    /** Path of delete icon */
    // public static final String DELETEICON = "img/delete.ico";
    public static final String DELETEICON  = YAMLConfig.getParam("existdb", "deleteicon");
    
    
    
    /** Path of rename icon */
    // public static final String RENAMEICON = "img/changeName.ico";
    public static final String RENAMEICON  = YAMLConfig.getParam("existdb", "renameicon");
       
    
    /** Path of DTD change icon */
    // public static final String TEISTRUCTCHANGEICON = "img/DTDChange.ico";
    public static final String TEISTRUCTCHANGEICON   = YAMLConfig.getParam("existdb", "teistructchangeicon");
    
    
    
    /** Path of create Project icon */
    // public static final String CREATEPROJECTICON = "img/createProject.ico";
    public static final String CREATEPROJECTICON = YAMLConfig.getParam("existdb", "createprojecticon");
    
    
    /** Path of logout icon */
    // public static final String LOGOUTICON = "img/logout.ico";
    public static final String LOGOUTICON = YAMLConfig.getParam("existdb", "logouticon");
    
    
    /** Path of menu icon */
    // public static final String MENUICON = "img/menu.ico";
    public static final String MENUICON  = YAMLConfig.getParam("existdb", "menuicon");
    
    
    /** Path of new file icon */
    // public static final String NEWFILEICON = "img/newFile.ico";
    public static final String NEWFILEICON  = YAMLConfig.getParam("existdb", "newfileicon");
    
    
    
    /** Path of export file icon */
    // public static final String EXPORTFILEICON = "img/export.png";
    public static final String EXPORTFILEICON  = YAMLConfig.getParam("existdb", "exportfileicon");
    
    
    /** Redo icon */
    // public static final String REDOICON = "img/redo.png";
    public static final String REDOICON  = YAMLConfig.getParam("existdb", "redoicon");
    
    
    /** Undo icon */
    // public static final String UNDOICON = "img/undo.png";
    public static final String UNDOICON  = YAMLConfig.getParam("existdb", "undoicon");
    
    
    
    /** Manual icon */
    // public static final String MANUALICON = "img/manual.ico";
    public static final String MANUALICON  = YAMLConfig.getParam("existdb", "manualicon");
    
    
    /** Path of login TEIScribe image */
    // public static final String LOGINLOGOIMAGE = "img/loginMain.png";
    public static final String LOGINLOGOIMAGE  = YAMLConfig.getParam("existdb", "loginlogoimage");
    
    
    /** Path of register TEIScribe image */
    // public static final String REGISTERLOGOIMAGE = "img/registerMain.png";
    public static final String REGISTERLOGOIMAGE  = YAMLConfig.getParam("existdb", "registerlogoimage");
    
    
    
    /** User credentials*/
    // public static final String USERNAME = "teiscribe";
    public static final String USERNAME = YAMLConfig.getParam("existdb", "username");   
    
    
    // public static final String PASSWORD = "bieses";
    public static final String PASSWORD = YAMLConfig.getParam("existdb", "password");   
    
    
    /** Database credentials*/
    
    //Production
    // public static final String EXISTCOLLECTION = "xmldb:exist://10.195.9.22:8888/exist/xmlrpc/db/apps/";
    public static final String EXISTCOLLECTION = "xmldb:exist://"+
	    YAMLConfig.getParam("server", "name")+
	    ":8888/exist/xmlrpc"+
	    YAMLConfig.getParam("existdb", "collections_root");
    
    
    // public static final String URISQL = "jdbc:mysql://localhost:3306/evi";
    public static final String URISQL = YAMLConfig.getParam("existdb", "urisql");   
    
    //public static final String USERSQL = "evimysql";
    public static final String USERSQL = YAMLConfig.getParam("database", "user");   
    
    
    //public static final String PASSWORDSQL = "Hffk2!9k";
    public static final String PASSWORDSQL = YAMLConfig.getParam("database", "password");  
    
    // public static final String LOGGERFILE = "/var/log/tomcat7/teiscribe.log";
    public static final String LOGGERFILE = YAMLConfig.getParam("existdb", "loggerfile");  

    
    //Devel
    /*public static final String EXISTCOLLECTION = "xmldb:exist://evilinhd.com:8888/exist/xmlrpc/db/apps/";
    public static final String URISQL = "jdbc:mysql://91.146.100.83:3306/evi";
    public static final String USERSQL = "murizar";
    public static final String PASSWORDSQL = "M4n8Arza";
    public static final String LOGGERFILE = "C:\\LOG.TXT";*/
    
    /** EVILINHD*/
    //public static final String EXISTUSER = "admin";
    
    public static final String EXISTUSER = YAMLConfig.getParam("existdb", "useradm");
    
    
    //public static final String EXISTPASS = "4dm1n";
    // public static final String EXISTPASS = "5dm1nsEV";
    
    //public static final String EXISTPASS = "uned100*";
    public static final String EXISTPASS = YAMLConfig.getParam("existdb", "existpass");
    
    
    //public static final String DEMOSESSION = "prueba";
    public static final String DEMOSESSION = YAMLConfig.getParam("existdb", "demosession");


    /** Bieses*/
    /*public static final String EXISTCOLLECTION = "xmldb:exist://linhd.es:8888/exist/xmlrpc/db/Bieses/";
    public static final String EXISTUSER = "bieses";
    public static final String EXISTPASS = "lun3sb13s3s";
    public static final String DEMOSESSION = "bieses";*/
    
    
    /**Jwt*/
    // public static final String SECRET = "bTQzsaBsbEuAfTma6c7EkdLnP1X4F11oeK";
    public static final String SECRET = YAMLConfig.getParam("token", "skey");
    
    /** Database for teiscribe in existdb*/
    // public static final String TEIDATABASEFOLDER = "teiscribe";
    public static final String TEIDATABASEFOLDER = YAMLConfig.getParam("existdb", "teidatabasefolder");

    
    
    /** Generic TEI for each project*/
    // public static final String GENERICTEIPROJECT = "TEI";
    public static final String GENERICTEIPROJECT = YAMLConfig.getParam("existdb", "genericteiproject");
    
    // public static final String GENERICTEIFILE = "default-schema.dtd";
    public static final String GENERICTEIFILE = YAMLConfig.getParam("existdb", "genericteifile");
    
    // public static final String TEIPROJECTNAME = "schema.dtd";
    public static final String TEIPROJECTNAME = YAMLConfig.getParam("existdb", "teiprojectname");
    
    /** Generic folder for TeiSimple*/
    // public static final String GENERICTEISIMPLEPROJECT = "tei-simple";
    public static final String GENERICTEISIMPLEPROJECT = YAMLConfig.getParam("existdb", "genericteisimpleproject");

    // public static final String TEISIMPLEPROJECTNAME = "test";
    public static final String TEISIMPLEPROJECTNAME = YAMLConfig.getParam("existdb", "teisimpleprojectname");
    
    

    /** Static XML head for the TEI xml files */
    // public static final String XMLHEAD1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    public static final String XMLHEAD1 = YAMLConfig.getParam("existdb", "xmlhead1");

    
    
    /** Static XML head for the TEI DTD definition */
    // public static final String XMLHEAD2DTD1 = "<?xml-model href=\"";
    public static final String XMLHEAD2DTD1  = YAMLConfig.getParam("existdb", "xmlhead2dtd1");

    //xmlhead2dtd1: <?xml-model href=\"
    // public static final String XMLHEAD2DTD2 = "\" type=\"application/xml-dtd\"?>\n";
    public static final String XMLHEAD2DTD2  = YAMLConfig.getParam("existdb", "xmlhead2dtd2");

    
    /** Constants for DTDManager and TextStruct */
    // public static final int FILETYPE = 0;
    public static final int FILETYPE = Integer.parseInt(YAMLConfig.getParam("existdb", "filetype"));
    

    // public static final int STRINGTYPE = 1;
    public static final int STRINGTYPE = Integer.parseInt(YAMLConfig.getParam("existdb", "stringtype"));
    
    
    /** Size of the undo button*/
    // public static final int UNDOMAXSIZE = 10;
    public static final int UNDOMAXSIZE = Integer.parseInt(YAMLConfig.getParam("existdb", "undomaxsize"));
    
    
    /**
     * @return the XMLdataBaseManager
     */
    public static final XMLDBManager getXMLDBManager(String user) {
	return (XMLDBManager) VaadinServlet.getCurrent().getServletContext().getAttribute("XMLdataBaseManager"+user);
    }
    
    /**
     * This function reads the constants from the settings.yaml file. The constants that are read are the server name and port
     * Sample excerpt of settings.yaml file: 
     * 
     * server:
     * name: evi3.uned.es
     * port: 8080
     *  
     * @param args
     * @throws FileNotFoundException
     */
   /* public static  void main(String[] args) throws FileNotFoundException {
	
    	Yaml yaml = new Yaml();
    	
		
		Map<String, Map<String, String>> values = (Map<String, Map<String, String>>) yaml
				.load(new FileInputStream(new File("/home/ach/00_SINCRONIZAR_FERRARIUNED-VULCAN/Investigacion/Humanidades/00_CodigoEVI-github-2017-04-24/eviscripts/evi-scripts/hardconfig/settings.yml")));

				
		// Get the values from the yaml file for each key and subkey.
		
		String servername =  values.get("server").get("name");
		
		String collections_root =  values.get("existdb").get("collections_root");

		final String EXISTCOLLECTION = "xmldb:exist://"+servername+":8888/exist/xmlrpc/"+collections_root; 
		
		// System.out.println(EXISTCOLLECTION);
		
		 
		
		
		
    }*/
    

}