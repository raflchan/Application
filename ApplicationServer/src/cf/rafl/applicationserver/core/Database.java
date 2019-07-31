package cf.rafl.applicationserver.core;


import cf.rafl.applicationserver.util.Crash;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database
{

    Logger logger = Logger.getLogger(Database.class.getName());

    private static Database singleton = null;

    private Connection connection;

    private Database()
    {
        connect();
    }

    public static Database getInstance()
    {
        if (singleton == null)
            singleton = new Database();

        return singleton;
    }

    public PreparedStatement preparedStatement(String s) throws SQLException
    {
        validateConnection();
        return connection.prepareStatement(s);
    }

    private void connect()
    {
        try
        {
            String url, username, password;

            Properties properties = Properties.getInstance();
            url = properties.getProperty("databaseUrl");
            username = properties.getProperty("databaseUsername");
            password = properties.getProperty("databasePassword");

            connection = DriverManager.getConnection(url, username, password);
            logger.info("established connection to db");

        } catch (Properties.NoSuchPropertyException e)
        {
            logger.log(Level.SEVERE, "The properties file isn't configured properly!", e);
            e.printStackTrace();
            Crash.crash();

        } catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Couldn't establish connection with database!", e);
            Crash.crash();
        }
    }

    private void validateConnection()
    {
        try
        {
            if (!connection.isValid(1))
            {
                logger.info("connection no longer valid");
                connect();
            }
        } catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Issues trying to validate connection!", e);
            Crash.crash();
        }
    }

}
