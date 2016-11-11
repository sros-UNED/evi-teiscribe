package com.application.components;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Level;

import com.application.texteditor.TextEditorUI;
import com.auth0.jwt.JWTVerifier;
import org.json.JSONObject;
import static com.application.components.Constants.URISQL;
import static com.application.components.Constants.USERSQL;
import static com.application.components.Constants.PASSWORDSQL;

/***
 * Class to decode jwt tokens
 * 
 * @author Miguel Urízar Salinas
 */
public class Jwt implements Serializable {
    private static final long serialVersionUID = -4534649687271268164L;
    /** Database credentials */
    public String user = null;
    public String pass = null;
    public String collection = null;

    /**
     * Sample method to validate and read the JWT
     * 
     * @param jwt
     *            String containing the token
     * @param CLIENT_SECRET
     *            Secret to decript the token
     * @return true if the jwt is correctly formed and false if there was any error.
     */
    public boolean parseJWT(String jwt, String CLIENT_SECRET) {
	try {
	    byte[] secret = org.apache.commons.codec.binary.Base64.decodeBase64(CLIENT_SECRET);
	    Map<String, Object> decodedPayload = new JWTVerifier(secret).verify(jwt);

	    JSONObject obj = new JSONObject(decodedPayload.get("data").toString());
	    String pid = obj.getString("pId");
	    String pName = obj.getString("pName");

	    Class.forName("com.mysql.jdbc.Driver").newInstance();
	    // Connect
	    Connection DBConnection = DriverManager.getConnection(URISQL, USERSQL, PASSWORDSQL);
	    // Prepare statement
	    PreparedStatement preparedStatement = DBConnection
		    .prepareStatement("SELECT * FROM projects_existdb WHERE project_id='" + pid + "' AND user_existdb='" + pName + "'");
	    // Execute statement
	    ResultSet resultSet = preparedStatement.executeQuery();
	    if (!resultSet.next()) { // if rs.next() returns false then there are no rows.
		TextEditorUI.getCurrent().logMessage(Level.SEVERE,
			"Error, no rows matching query  \"SELECT * FROM projects_existdb WHERE project_id='" + pid + "' AND user_existdb='" + pName + "'\"",
			false);
		return false;
	    }
	    collection = resultSet.getString("collection_name");
	    user = resultSet.getString("user_existdb");
	    pass = resultSet.getString("passwd_existdb");
	} catch (Exception e) {
	    TextEditorUI.getCurrent().logMessage(Level.SEVERE, "Error parsing JWT \"" + jwt + "\". " + e.getMessage() + ". Invalid signature!", false);
	    e.printStackTrace();
	    return false;
	}
	return true;
    }
}
