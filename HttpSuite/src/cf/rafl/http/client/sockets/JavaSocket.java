package cf.rafl.http.client.sockets;

import cf.rafl.http.client.Socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JavaSocket implements Socket
{
    private final java.net.Socket socket;
    private final String host;
    private final int port;

    public JavaSocket(String host, int port) throws IOException
    {
        this.host = host;
        this.port = port;

        this.socket = new java.net.Socket(this.host, this.port);
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        return socket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException
    {
        return socket.getOutputStream();
    }

    @Override
    public void close() throws IOException
    {
        socket.close();
    }
}
