package cf.rafl.applicationserver.core;


public class Constants
{
    private static Constants singleton = null;

    private long verifyTokenExpiry;
    private long sessionTokenExpiry;

    private Constants()
    {
        try
        {
            Properties props = Properties.getInstance();
            this.verifyTokenExpiry = Long.parseLong(props.getProperty("verifyTokenExpiry")) * 60 * 1000;
            this.sessionTokenExpiry = Long.parseLong(props.getProperty("sessionTokenExpiry")) * 60 * 1000;

        } catch (Properties.NoSuchPropertyException e)
        {
            System.err.println("The properties file is not correctly configured!\n Terminating...");
            throw new RuntimeException(e);
        }
    }

    private static Constants getInstance()
    {
        if (singleton == null)
            singleton = new Constants();
        return singleton;
    }

    public static long verifyTokenExpiry()
    {
        return getInstance().verifyTokenExpiry;
    }

    public static long sessionTokenExpiry()
    {
        return getInstance().sessionTokenExpiry;
    }
}
