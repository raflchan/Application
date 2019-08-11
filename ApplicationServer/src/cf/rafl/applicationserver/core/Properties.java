package cf.rafl.applicationserver.core;

import cf.rafl.applicationserver.util.Crash;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Properties
{

    private static Properties singleton = null;

    private java.util.Properties props;
    public static String fileName = "cfg/properties.properties";

    private Properties()
    {
        try (InputStream input = new FileInputStream(fileName))
        {
            props = new java.util.Properties();
            props.load(input);

        } catch (FileNotFoundException e)
        {
            System.err.println("Couldn't find Properties file (" + fileName + ")");
            Crash.crash();
        } catch (IOException e)
        {
            System.err.println("Couldn't open Properties file");
            e.printStackTrace();
            Crash.crash();
        }
    }

    public static Properties getInstance()
    {
        if(singleton == null)
            singleton = new Properties();

        return singleton;
    }

    public String getProperty(String s) throws NoSuchPropertyException
    {
        String prop = props.getProperty(s);
        if(prop == null)
            throw new NoSuchPropertyException();
        return props.getProperty(s);
    }

    public static class NoSuchPropertyException extends Exception
    {

    }

}
