package cf.rafl.http.client;

import java.util.HashMap;
import java.util.Map;

public class DefaultPorts
{

    private static Map<String, Integer> map = null;

    public static int getPort(String prefix) throws NoDefaultPortException
    {
        if (map == null)
            createMap();

        Integer port = map.get(prefix);
        if(port == null)
            throw new NoDefaultPortException();

        return port;
    }

    private static void createMap()
    {
        map = new HashMap<>();

        for (Port port : Port.values())
            map.put(port.prefix, port.port);
    }


    private enum Port
    {

        HTTP("http", 80),
        HTTPS("https", 443),
        FTP("ftp", 21);

        String prefix;
        int port;

        Port(String prefix, int port)
        {
            this.prefix = prefix;
            this.port = port;
        }
    }

    public static class NoDefaultPortException extends Exception
    {

    }
}
