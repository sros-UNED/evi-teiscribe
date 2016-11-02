package com.application.components;

import static com.application.components.Constants.FALSE;
import static com.application.components.Constants.TRUE;
import static com.application.components.Constants.ERROR;
import static com.application.components.Constants.EXISTCOLLECTION;
import static com.application.components.Constants.GENERICTEIPROJECT;
import static com.application.components.Constants.TEIPROJECTNAME;
import static com.application.components.Constants.GENERICTEISIMPLEPROJECT;
import static com.application.components.Constants.TEISIMPLEPROJECTNAME;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.application.language.Labels;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;
import org.xmldb.api.*;
import org.exist.xmldb.CollectionManagementServiceImpl;
import org.exist.xmldb.EXistResource;
import org.exist.xmldb.XmldbURI;
import org.exist.xquery.util.URIUtils;

/**
 * Class for connecting to exist database
 * 
 * @author Miguel Urízar Salinas
 *
 */
public class XMLDBManager implements Serializable {
    private static final long serialVersionUID = -183644918926700546L;
    
    /** Database driver */
    String driver = "org.exist.xmldb.DatabaseImpl";
    /** Load driver*/
    Class<?> cl = null;
    /** instance to database */
    Database database = null;
    /** Access to collection*/
    Collection col = null;
    String collection_name;
    XPathQueryService service = null;
    String userDB = null;
    String passDB = null;

    /** Connector for he database */
    static Connection DBConnection;
    /** Class for the sentence to send to the DB */
    static PreparedStatement preparedStatement;
    /** Class for storing the answers from the DB */
    static ResultSet resultSet;
    /** Lock to deny simultaneous accesses to database */
    static public Lock lock = new ReentrantLock();
    /**
     * Constructor for the exist database that registers the DB driver
     * @throws XMLDBException 
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public XMLDBManager(String collection,String user, String pass) throws XMLDBException, ClassNotFoundException, InstantiationException, IllegalAccessException {
    	userDB = user;
    	passDB = pass;
    	Class cl = Class.forName(driver);
	    database = (Database) cl.newInstance();
	    database.setProperty("create-database", "true");
	    DatabaseManager.registerDatabase(database);
	    col = DatabaseManager.getCollection(EXISTCOLLECTION, user, pass);
	    service =(XPathQueryService) col.getService("XPathQueryService", "1.0");
	    service.setProperty("pretty", "true");
	    service.setProperty("encoding", "ISO-8859-1");
	    col = col.getChildCollection(collection);
	    collection_name = collection;
    }
    
    /**
     * Creates folder for the project in ExistDB database.
     * @param env Name of the project to create the folder.
     * @throws XMLDBException 
     */
    public void createEnv(String env) throws XMLDBException {
    	// Go to EVI Collection
    	Collection envCol;
		envCol = col.getChildCollection(env);
		// Create collection if does not exist
		if (envCol == null) {
			CollectionManagementService mgt = (CollectionManagementService) col.getService("CollectionManagementService", "1.0");
			mgt.createCollection(env);
		}
    }
    
    /**
     * Gets all the collection in the database.
     * <p>
     * Returns an empty array if there are no collection at the moment.
     * </p>
     * 
     * @param env Name of the project. 
     * @return Array of the present collections or null if an error occurred.
     * @throws XMLDBException 
     */
    public ArrayList<String> getProjects(String env) throws XMLDBException {
    	ArrayList<String> projects = new ArrayList<String>();
	    String[] names = col.getChildCollection(env).listChildCollections();
	    // Create array with collections
	    for (String name : names) {
	    	name = URIUtils.urlDecodeUtf8(name);
	    	if (!name.equals("TEI"))
	    		projects.add(name);
	    }
	    return projects;
    }

    /**
     * Gets all the TEI structures in the database.
     * <p>
     * Returns an empty array if there are no TEI structures at the moment.
     * </p>
     * @param env Name of the project. 
     * 
     * @return Array of the present TEI structures or null if an error occurred.
     * @throws XMLDBException 
     */
    public ArrayList<String> getTEIStructs(String env) throws XMLDBException {
    	ArrayList<String> structs = new ArrayList<String>();

	    Collection teiFolder = col.getChildCollection(env).getChildCollection(GENERICTEIPROJECT);
	    if (teiFolder == null)
	    {
		CollectionManagementService mgt = (CollectionManagementService) col.getChildCollection(env).getService("CollectionManagementService", "1.0");
		teiFolder = mgt.createCollection(GENERICTEIPROJECT);
		if (teiFolder == null) {
		    return null;
		}
	    }
	    String[] names = teiFolder.listResources();
	    // Create array with tei Files
	    for (String name : names) {
	    	name = URIUtils.urlDecodeUtf8(name);
	    	structs.add(name);
	    }
	    return structs;
    }

    /**
     * Gets all the files from a collection in the database.
     * <p>
     * Returns an empty array if there are no files at the moment.
     * </p>
     * 
     * @param env Name of the project.
     * @param project Name of the collection inside the project.
     * 
     * @return Array of the present files or null if an error occurred.
     * @throws XMLDBException 
     */
    public ArrayList<String> getFilesFromProject(String env, String project) throws XMLDBException {
    	ArrayList<String> structs = new ArrayList<String>();
	    String[] names = col.getChildCollection(env).getChildCollection(project).listResources();
	    // Create array with projects
	    for (String name : names) {
	    	name = URIUtils.urlDecodeUtf8(name);
	    	// Don't show the schema file
	    	if (!name.equals(TEIPROJECTNAME))
	    		structs.add(name);
	    	}
	    return structs;
    }

    /**
     * Adds a collection to the DataBase.
     * <p>
     * Creates the folder as the projectName and adds the structure for the project. If a collection with that name already exists,
     * no collection is created and FALSE is returned.
     * </p>
     * 
     * @param projectName
     *            Name of the collection to check if exists
     * @param env Name of the project.
     * @return {@link Constants#TRUE} if is introduced correctly, {@link Constants#FALSE} if project exists or {@link Constants#ERROR} if there was an error in
     *         DB.
     * @throws XMLDBException 
     */
    public int addProject(String projectName, String env) throws XMLDBException {
    	// Check if collection exists
    	if (checkIfProjectExists(env, projectName) == TRUE) return FALSE;
    	CollectionManagementService mgt;
	    mgt = (CollectionManagementService) col.getChildCollection(env).getService("CollectionManagementService", "1.0");
	    Collection newProject = mgt.createCollection(projectName);
	    if (newProject == null) return FALSE;
	    Broadcaster.changeProject(env, Labels.getString("addedProject"));
	    return TRUE;
    }
    
    /**
     * Checks if a TEI schema exists in the collection.
     * 
     * @param project
     *            Name of the collection to check if file exists
     * @param env Name of the project.
     * 
     * @return {@link Constants#TRUE} if is introduced correctly or {@link Constants#ERROR} if there was an error in DB.
     * @throws XMLDBException 
     */
    public int checkTEIStruct(String env, String project) throws XMLDBException {
    	return checkIfFileExists(env, project,TEIPROJECTNAME);
    }
    

    /**
     * Adds a XML file to the DataBase.
     * 
     * @param project
     *            Folder of the collection where the file belongs to
     * @param fileName
     *            Name of the file to introduce
     * @param data
     *            Data to save
     * @param env Name of the project.
     * 
     * @return {@link Constants#TRUE} if is introduced correctly, {@link Constants#FALSE} if file name already exists or {@link Constants#ERROR} if there was an
     *         error in DB.
     * @throws XMLDBException 
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     */
    public int addXMLFile(String env, String project, String fileName, byte[] data) throws XMLDBException, ParserConfigurationException, SAXException, IOException {
    	return addXMLFile(env, project, fileName,data, col);
    }
    
    /**
     * Adds a XML file to the DataBase.
     * 
     * @param project
     *            Folder of the collection where the file belongs to
     * @param fileName
     *            Name of the file to introduce
     * @param data
     *            Data to save
     * @param env Name of the project.
     * @param col_dest Collection to destination file
     * 
     * @return {@link Constants#TRUE} if is introduced correctly, {@link Constants#FALSE} if file name already exists or {@link Constants#ERROR} if there was an
     *         error in DB.
     * @throws XMLDBException 
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     */
    public int addXMLFile(String env, String project, String fileName, byte[] data, Collection col_dest) throws XMLDBException, ParserConfigurationException, SAXException, IOException {
    	XMLResource res = null;
	    // Go to collection
	    Collection projectCol = col_dest.getChildCollection(env).getChildCollection(project);
	    // Parse data as DOM object
	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    dbFactory.setIgnoringComments(true);
	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    ByteArrayInputStream is =  new ByteArrayInputStream(data);
	    Document doc = dBuilder.parse(is);
	    // Normalize the file
	    doc.getDocumentElement().normalize();
	    // Get root element
	    Node root = doc.getDocumentElement();
	    res = (XMLResource)projectCol.createResource(fileName, "XMLResource");
	    res.setContentAsDOM(root);
	    projectCol.storeResource(res);
	    if (res != null) {
	    	try { ((EXistResource)res).freeResources(); } catch(Exception xe) {return ERROR;}
	    }
	    Broadcaster.changeFile(Labels.getString("addedFile"), env, project);
	    return TRUE;
    }
    
    /**
     * Adds a Binary file to the DataBase.
     * 
     * @param env Name of the project.
     * @param project
     *            Folder of the collection where the file belongs to
     * @param fileName
     *            Name of the file to introduce
     * @param data
     *            Data to save
     * @return {@link Constants#TRUE} if is introduced correctly, {@link Constants#FALSE} if file name already exists or {@link Constants#ERROR} if there was an
     *         error in DB.
     * @throws XMLDBException 
     * @throws UnsupportedEncodingException 
     */
    public int addBinaryFile(String env, String project, String fileName, String data) throws XMLDBException, UnsupportedEncodingException {
    	BinaryResource res = null;
	    // Go to collection
	    Collection projectCol = col.getChildCollection(env).getChildCollection(project);
	    res = (BinaryResource)projectCol.createResource(fileName, "BinaryResource");
	    res.setContent(data.getBytes("UTF-8"));
	    projectCol.storeResource(res);
	    if (res != null) {
		try { ((EXistResource)res).freeResources(); } catch(Exception xe) {return ERROR;}
	    }
	    return TRUE;
    }

    /**
     * Deletes the collection with its files.
     * 
     * @param env Name of the project.
     * @param project Name of the project defined inside the environment.
     *            Folder of the collection to delete
     * @return {@link Constants#TRUE} if is deleted correctly, {@link Constants#FALSE} if was impossible to get project path or {@link Constants#ERROR} if there
     *         was an error in DB.
     * @throws XMLDBException 
     */
    public int deleteProject(String env,String project) throws XMLDBException {
    	CollectionManagementService mgt;
	    mgt = (CollectionManagementService) col.getChildCollection(env).getService("CollectionManagementService", "1.0");
	    mgt.removeCollection(project);
	    Broadcaster.changeProject(env,Labels.getString("deletedProject"));
	    return TRUE;
    }

    /**
     * Deletes the file and removes from DB. Updates the UIs
     * 
     * @param env Name of the project.
     * @param project
     *            Folder of the collection where the file belongs to.
     * @param fileName
     *            Id of the file to delete
     * @return {@link Constants#TRUE} if is deleted correctly, {@link Constants#FALSE} if was impossible to get file path or {@link Constants#ERROR} if there
     *         was an error in DB.
     * @throws XMLDBException 
     */
    public int deleteFile(String env, String project, String fileName) throws XMLDBException {
	    Collection dir = col.getChildCollection(env).getChildCollection(project);
	    dir.removeResource(dir.getResource(fileName));
	    Broadcaster.changeFile(Labels.getString("deletedFile"), env, project);
	    Broadcaster.kickUIsOfFile(env, project, fileName, Labels.getString("deletedFile"));
	    return TRUE;
    }

    /**
     * Updates the collection with the new TEI schema
     * 
     * @param env Name of the project.
     * @param project
     *            Folder of the collection where the file belongs to.
     * @param schema
     *            Name of the schema to link with the collection
     * @return {@link Constants#TRUE} if is updated correctly or {@link Constants#ERROR} if there was an error in DB.
     * @throws XMLDBException 
     */
    public int updateProjectTEISchema(String env, String project, String schema) throws XMLDBException {
    	BinaryResource res = null,from = null;
	
	    from = (BinaryResource) col.getChildCollection(env).getChildCollection(GENERICTEIPROJECT).getResource(schema);
	    byte[] data = (byte[]) from.getContent();
	    Collection projectCol = col.getChildCollection(env).getChildCollection(project);
	    res = (BinaryResource)projectCol.createResource(TEIPROJECTNAME, "BinaryResource");
	    res.setContent(data);
	    projectCol.storeResource(res);
	    if (res != null) {
		try { ((EXistResource)res).freeResources(); } catch(Exception xe) {return ERROR;}
	    }
	    if (from != null) {
		try { ((EXistResource)from).freeResources(); } catch(Exception xe) {return ERROR;}
	    }
	    return TRUE;
    }

    /**
     * Renames a project in the database with defined id
     * 
     * @param env Name of the project.
     * @param newName
     *            New name for the collection.
     * @param oldName
     *            Old name of the collection.
     * @return {@link Constants#TRUE} if is changed correctly, {@link Constants#FALSE} if a collection with that name exists or {@link Constants#ERROR} if there
     *         was an error in DB.
     * @throws XMLDBException 
     */
    public int updateProjectName(String env, String newName, String oldName) throws XMLDBException {
    	// Check if collection exists
    	if (checkIfProjectExists(env, newName) == TRUE) return FALSE;
    	CollectionManagementServiceImpl mgt;
	    mgt = (CollectionManagementServiceImpl) col.getChildCollection(env).getService("CollectionManagementService", "1.0");
	    mgt.move(XmldbURI.create(oldName), XmldbURI.create(EXISTCOLLECTION+ collection_name + "/" + env),XmldbURI.create(newName));
	    Broadcaster.changeProject(env,Labels.getString("changeNameProject"));
	    return TRUE;
    }

    /**
     * Renames a file in the database with defined id
     * <p>
     * It does not affect the path name.
     * </p>
     * 
     * @param env Name of the project.
     * @param project
     *            Folder of the collection where the file belongs to.
     * @param newName
     *            New name for the file
     * @param oldName
     *            Old name of the file
     * @return {@link Constants#TRUE} if is changed correctly, {@link Constants#FALSE} if a file with that name exists or {@link Constants#ERROR} if there was
     *         an error in DB.
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     * @throws XMLDBException 
     */
    public int updateFileName(String env, String newName, String oldName, String project) throws XMLDBException, ParserConfigurationException, SAXException, IOException {
	// Check if name exists
	if (checkIfFileExists(env, project,newName) == TRUE) return FALSE;
	byte[] data = getXMLFile(env, project, oldName);
	if (data == null) return ERROR;
	int response = addXMLFile(env,project,newName,data);
	if (response == TRUE) {
	    // Remove resource
	    return deleteFile(env,project,oldName);
	} else {
	    return response;
	}
    }
    
    /**
     * Renames a file in the database with defined id
     * <p>
     * It does not affect the path name.
     * </p>
     * 
     * @param env Name of the project.
     * @param project
     *            Folder of the collection where the file belongs to.
     * @param oldName
     *            Old name of the file
     * @return {@link Constants#TRUE} if is changed correctly, {@link Constants#FALSE} if a file with that name exists or {@link Constants#ERROR} if there was
     *         an error in DB.
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     * @throws XMLDBException 
     */
    public int exportTeiSimpleFileName(String env, String oldName, String project, String new_name) throws XMLDBException, ParserConfigurationException, SAXException, IOException {
	// Check if name exists
	byte[] data = getXMLFile(env, project, oldName);
	if (data == null) return ERROR;
	Collection col_dest = DatabaseManager.getCollection(EXISTCOLLECTION, userDB, passDB);
	int response = addXMLFile(GENERICTEISIMPLEPROJECT,TEISIMPLEPROJECTNAME,new_name,data,col_dest);
    return response;
    }
    
    /**
     * Returns the inputStream for the project.
     * @param env Name of the project.
     * @param project Name of the collection.
     * @param file Name of the file.
     * @return Byte[] with the data of the file.
     * @throws XMLDBException 
     */
    public byte[] getXMLFile(String env, String project, String file) throws XMLDBException {
	    XMLResource res = (XMLResource) col.getChildCollection(env).getChildCollection(project).getResource(file);
	    String data = (String)res.getContent();
	    return data.getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * Returns the inputStream for the file.
     * @param env Name of the project.
     * @param project Name of the collection.
     * @param file Name of the file.
     * @return Byte[] with the data of the file.
     * @throws XMLDBException 
     */
    public byte[] getBinaryFile(String env, String project, String file) throws XMLDBException {
	    BinaryResource res = (BinaryResource) col.getChildCollection(env).getChildCollection(project).getResource(file);
	    return (byte[])res.getContent();
    }

    /**
     * Returns the size of the file.
     * @param env Name of the project.
     * @param project Name of the collection.
     * @param file Name of the file.
     * @return int with the size.
     * @throws XMLDBException 
     */
    public int getSize(String env, String project, String file) throws XMLDBException {
	    EXistResource res = (EXistResource) col.getChildCollection(env).getChildCollection(project).getResource(file);
	    return (int) res.getContentLength();
    }

    /**
     * Returns the last modification of the file.
     * @param env Name of the project.
     * @param project Name of the collection.
     * @param file Name of the file.
     * @return Date with the last modified date.
     * @throws XMLDBException 
     */
    public Date getLastModified(String env, String project, String file) throws XMLDBException {
	    EXistResource res = (EXistResource) col.getChildCollection(env).getChildCollection(project).getResource(file);
	    return res.getLastModificationTime();
    }
    
    /**
     * Checks if a file exists in the collection.
     * 
     * @param env Name of the project.
     * @param project
     *            Name of the collection to check if file exists
     * @param filename
     * 		  Name of the file to check if exists
     * @return {@link Constants#TRUE} if exists, {@link Constants#FALSE} if does no exist or {@link Constants#ERROR} if there was an error in DB.
     * @throws XMLDBException 
     */
    public int checkIfFileExists(String env, String project, String filename) throws XMLDBException {
    	String[] names;
	    names = col.getChildCollection(env).getChildCollection(project).listResources();
	    // Create array with projects
	    for (String name : names) {
	    	if (name.equalsIgnoreCase(filename)) {
	    		return TRUE;
	    	}
	    }
	    return FALSE;
    }
    
    /**
     * Checks if a collection exists.
     * 
     * @param env Name of the project.
     * @param project
     *            Name of the collection to check if exists
     * @return {@link Constants#TRUE} if exists, {@link Constants#FALSE} if does no exist or {@link Constants#ERROR} if there was an error in DB.
     * @throws XMLDBException 
     */
    public int checkIfProjectExists(String env, String project) throws XMLDBException {
	    if (col.getChildCollection(env).getChildCollection(project) == null) return FALSE;
	    return TRUE;
    }

}