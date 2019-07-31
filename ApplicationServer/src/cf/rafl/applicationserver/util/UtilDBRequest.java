package cf.rafl.applicationserver.util;

import cf.rafl.applicationserver.core.Database;
import cf.rafl.applicationserver.core.security.LoginCredentials;

import java.sql.*;
import java.util.Date;

public class UtilDBRequest
{

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
                db.preparedStatement("INSERT INTO session_tokens (Token, FK_User, Created, Address) VALUES (?, ?, ?, ?)");
        statement.setString(1, sessionToken);
        statement.setString(2, login.username);
        statement.setTimestamp(3, new Timestamp(new Date().getTime()));
        statement.setString(4, login.ip);

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

}
