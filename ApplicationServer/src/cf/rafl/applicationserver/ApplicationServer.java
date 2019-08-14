package cf.rafl.applicationserver;

import cf.rafl.applicationserver.core.Properties;
import cf.rafl.applicationserver.requesthandlers.API;
import cf.rafl.applicationserver.util.Crash;
import cf.rafl.http.core.HttpHandler;
import cf.rafl.http.server.HttpsServer;


public class ApplicationServer
{
    private HttpsServer server;

    public ApplicationServer()
    {
        try
        {
            Properties props = Properties.getInstance();
            HttpsServer.Builder builder = new HttpsServer.Builder();
            builder.setKeyStore(props.getProperty("keyStore"))
                    .setKeyStorePassword(props.getProperty("keyStorePassword"));
            server = builder.build();

            server.createContext("/", new HttpHandler.DefaultHandler());
            server.createContext("/api", new API());

        } catch (Properties.NoSuchPropertyException e)
        {
            System.err.println("The properties file isn't configured correctly!");
            e.printStackTrace();
            Crash.crash();
        }

    }

    public void start()
    {
        server.start();
    }

}
