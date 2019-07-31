package cf.rafl.http.server;

import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

/**
 * An HTTP Server implementation with focus on easy and simple usage.
 * This utilizes the sun implementation of HttpServer (com.sun.net.httpserver.HttpServer).
 */
public class HttpServer
{
    private com.sun.net.httpserver.HttpServer server;
    final int port;

    /**
     * Creates an HttpServer with specified port
     * @param port The port the server will be running on
     * @param backlog The max amount of incoming TCP connections that can be queued
     * @throws IOException
     */
    public HttpServer(int port, int backlog) throws IOException
    {
        this.port = port;
        server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(this.port),backlog);
    }

    /**
     * Sets the Executor of the server.
     * @param executor The wanted Executor.
     */
    public void setExecutor(Executor executor)
    {
        server.setExecutor(executor);
    }

    /**
     * This will create a context that the server listens to and handles accordingly
     * @param contextPath The subdomain the handler will be attached to
     * @param handler The handler that handles the requests
     */
    public void createContext(String contextPath, HttpHandler handler)
    {
        server.createContext(contextPath, handler);
    }

    /**
     * Starts the server.
     */
    public void start()
    {
        server.start();
    }
}
