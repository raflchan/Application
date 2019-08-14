package cf.rafl.applicationserver.util;

import cf.rafl.applicationserver.core.Constants;
import cf.rafl.applicationserver.core.Database;
import cf.rafl.applicationserver.core.structs.LoginCredentials;
import cf.rafl.applicationserver.core.structs.Microcontroller;

import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

// TODO: 10.08.2019 if db dead no response
// TODO: 10.08.2019 dead db crashes server, bad!
// TODO: 14.08.2019 sanitize input

public class UtilDBRequest
{

    @SuppressWarnings("SpellCheckingInspection")
    private static char[] tokenChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    private static Database db = Database.getInstance();



    public static boolean userExists(String username) throws SQLException
    {
        PreparedStatement statement =
                db.preparedStatement("SELECT users.Username FROM users WHERE users.Username = ?");
        statement.setString(1, username);
        ResultSet res = statement.executeQuery();
        return res.next();
    }

    public static long getCreationDate(String username) throws SQLException
    {
        PreparedStatement statement =
                db.preparedStatement("SELECT users.Created FROM users WHERE users.Username = ?");
        statement.setString(1, username);
        ResultSet res = statement.executeQuery();
        if (res.next())
            return res.getTimestamp(1).getTime();

        throw new SQLException();
    }

    public static String getPasswordHash(String username) throws SQLException
    {
        PreparedStatement statement =
                db.preparedStatement("SELECT users.Password FROM users WHERE users.Username = ?");
        statement.setString(1, username);
        ResultSet res = statement.executeQuery();
        if (res.next())
            return res.getString(1);

        throw new SQLException();
    }

    public static String getUserFromSessionToken(String sessionToken) throws SQLException
    {
        PreparedStatement statement =
                db.preparedStatement("SELECT session_tokens.FK_User FROM session_tokens WHERE session_tokens.Token = ?");
        statement.setString(1, sessionToken);
        ResultSet res = statement.executeQuery();
        if(res.next())
            return res.getString(1);

        throw new SQLException();
    }

    public static String createSessionToken(LoginCredentials login) throws SQLException
    {
        String sessionToken;
        do
        {
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[128];
            random.nextBytes(bytes);
            sessionToken = Base64.getEncoder().encodeToString(bytes);

        } while (validSessionToken(sessionToken));

        PreparedStatement statement =
                db.preparedStatement("INSERT INTO session_tokens (Token, FK_User, Created, Expires, Address) VALUES (?, ?, ?, ?, ?)");
        statement.setString(1, sessionToken);
        statement.setString(2, login.username);
        statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        statement.setTimestamp(4, new Timestamp(System.currentTimeMillis() + Constants.sessionTokenExpiry()));
        statement.setString(5, login.ip);

        if(statement.executeUpdate() == 1)
            return sessionToken;
        throw new SQLException();
    }

    public static void putUser(LoginCredentials login, Timestamp created) throws SQLException
    {
        PreparedStatement statement =
                db.preparedStatement("INSERT INTO users (Username, Password, Created) VALUES (?, ?, ?)");
        statement.setString(1, login.username);
        statement.setString(2, login.password);
        statement.setTimestamp(3, created);

        if(statement.executeUpdate() != 1)
            throw new SQLException();
    }

    public static boolean validVerificationToken(String verificationToken) throws SQLException
    {
        PreparedStatement statement =
                db.preparedStatement("DELETE FROM verification_tokens WHERE Token = ? AND Expires <= CURRENT_TIMESTAMP()");
        statement.setString(1, verificationToken);
        statement.executeUpdate();

        statement = db.preparedStatement("SELECT 1 FROM verification_tokens WHERE verification_tokens.Token = ?");
        statement.setString(1, verificationToken);
        return statement.executeQuery().next();
    }

    public static boolean validSessionToken(String sessionToken) throws SQLException
    {
        PreparedStatement statement =
                db.preparedStatement("DELETE FROM session_tokens WHERE Token = ? AND Expires <= CURRENT_TIMESTAMP()");
        statement.setString(1, sessionToken);
        statement.executeUpdate();

        statement = db.preparedStatement("SELECT 1 FROM session_tokens WHERE session_tokens.Token = ?");
        statement.setString(1, sessionToken);
        return statement.executeQuery().next();
    }

    public static String createVerificationToken(String username) throws SQLException
    {

        char[] cToken = new char[32];
        Random rand = new SecureRandom();
        for(int i = 0; i < 32; i++)
            cToken[i] = tokenChars[rand.nextInt(tokenChars.length)];
        String token = new String(cToken);

        PreparedStatement statement =
                db.preparedStatement("INSERT INTO verification_tokens (Token, FK_User, Created, Expires) VALUES (?, ?, ?, ?)");
        statement.setString(1, token);
        statement.setString(2, username);
        statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        statement.setTimestamp(4, new Timestamp(System.currentTimeMillis() + Constants.verifyTokenExpiry()));

        return statement.executeUpdate() == 1 ? token : null;

    }

    public static List<Microcontroller> getUsersMicrocontrollers(String username) throws SQLException
    {
        PreparedStatement statement =
                db.preparedStatement("SELECT ControllerID, Name, Description, Address FROM microcontrollers WHERE FK_User = ?");
        statement.setString(1, username);
        ResultSet result = statement.executeQuery();
        List<Microcontroller> controllers = new ArrayList<>();
        while(result.next())
        {
            String controllerID = result.getString(1);
            String name         = result.getString(2);
            String description  = result.getString(3);
            String address      = result.getString(4);
            controllers.add(new Microcontroller(controllerID, name, description, username, address));
        }

        return controllers;
    }

}
