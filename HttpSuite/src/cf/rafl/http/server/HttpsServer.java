package cf.rafl.http.server;

import cf.rafl.http.core.HttpHandler;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpsServer
{

    /* TODO: 17.07.2019
    *   implement max header size
    *   implement timeouts
    * */


    private Thread serverThread;
    private int port;
    private SSLServerSocket listenSocket;
    private Map<String, HttpHandler> contexts = new ConcurrentHashMap<>();

//    public static void main(String[] args) throws Exception
//    {
//        HttpsServer server = new HttpsServer.Builder()
//                .setKeyStore("res/keystores/rafl.cf.jks")
//                .setKeyStorePassword("nopassword")
//                .build();
//
//        server.start();
//        System.out.println("Server running...");
//    }

    private HttpsServer(int port, String keyStore, String keyStorePassword)
    {
        this.port = port;
        System.setProperty("javax.net.ssl.keyStore", keyStore);
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
    }

    public void start()
    {
        serverThread = new ServerThread();
        serverThread.start();
    }

    public void createContext(String contextPath, HttpHandler handler)
    {
        if(contextPath == null || handler == null)
            throw new IllegalArgumentException();

        contexts.put(contextPath, handler);
    }

    private class ServerThread extends Thread
    {
        @Override
        public void run()
        {
            this.setName("ServerThread");
            try
            {
                listenSocket = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(port);

                while (true)
                    new HttpConnection(listenSocket.accept(), contexts).start();

            } catch (IOException e)
            {
                // TODO: 14.07.2019 exception handling
                e.printStackTrace();

            }
        }

    }

    public static class Builder
    {
        private int port = 443;
        private String keyStore = null, keyStorePassword = null;


        public Builder()
        {
        }

        public Builder setPort(int port)
        {
            this.port = port;
            return this;
        }

        public Builder setKeyStore(String keyStore)
        {
            this.keyStore = keyStore;
            return this;
        }

        public Builder setKeyStorePassword(String keyStorePassword)
        {
            this.keyStorePassword = keyStorePassword;
            return this;
        }

        public HttpsServer build()
        {
            return new HttpsServer(port, keyStore, keyStorePassword);
        }
    }

}
