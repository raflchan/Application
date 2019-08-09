package cf.rafl.applicationserver.util;

import cf.rafl.applicationserver.core.Constants;
import cf.rafl.applicationserver.core.Database;
import cf.rafl.applicationserver.core.security.LoginCredentials;

import java.sql.*;
import java.util.Random;

// TODO: 07.08.2019 implement the expiries in here 

public class UtilDBRequest
{

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

    public static boolean putSessionToken(LoginCredentials login, String sessionToken) throws SQLException
    {
        PreparedStatement statement =
                db.preparedStatement("INSERT INTO session_tokens (Token, FK_User, Created, Expires, Address) VALUES (?, ?, ?, ?, ?)");
        statement.setString(1, sessionToken);
        statement.setString(2, login.username);
        statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        statement.setTimestamp(4, new Timestamp(System.currentTimeMillis() + Constants.sessionTokenExpiry()));
        statement.setString(5, login.ip);

        return statement.executeUpdate() == 1;
    }

    public static boolean putUser(LoginCredentials login, Timestamp created) throws SQLException
    {
        PreparedStatement statement =
                db.preparedStatement("INSERT INTO users (Username, Password, Created) VALUES (?, ?, ?)");
        statement.setString(1, login.username);
        statement.setString(2, login.password);
        statement.setTimestamp(3, created);


        return statement.executeUpdate() == 1;
    }

    public static boolean verificationTokenExists(String verificationToken) throws SQLException
    {
        PreparedStatement statement =
                db.preparedStatement("DELETE FROM verification_tokens WHERE Token = ? AND Expires <= CURTIME()");
        statement.setString(1, verificationToken);
        statement.executeUpdate();

        statement = db.preparedStatement("SELECT 1 FROM verification_tokens WHERE verification_tokens.Token = ?");
        statement.setString(1, verificationToken);
        return statement.executeQuery().next();
    }

    public static boolean validSessionToken(String sessionToken) throws SQLException
    {
        PreparedStatement statement =
                db.preparedStatement("DELETE FROM verification_tokens WHERE Token = ? AND Expires <= CURTIME()");
        statement.setString(1, sessionToken);
        statement.executeUpdate();

        statement = db.preparedStatement("SELECT 1 FROM session_tokens WHERE session_tokens.Token = ?");
        statement.setString(1, sessionToken);
        return statement.executeQuery().next();
    }

    public static String createVerificationToken(String username) throws SQLException
    {

        char[] cToken = new char[32];
        Random rand = new Random();
        for(int i = 0; i < 32; i++)
            cToken[i] = tokenChars[rand.nextInt(tokenChars.length)];
        String token = new String(cToken);

        PreparedStatement statement =
                db.preparedStatement("INSERT INTO verification_tokens (Token, FK_User, Created, Expires) = (?, ?, ?, ?)");
        statement.setString(1, token);
        statement.setString(2, username);
        statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        statement.setTimestamp(4, new Timestamp(System.currentTimeMillis() + Constants.verifyTokenExpiry()));

        return statement.executeUpdate() == 1 ? token : null;

    }

}
