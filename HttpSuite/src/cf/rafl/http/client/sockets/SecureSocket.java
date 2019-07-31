package cf.rafl.http.client.sockets;

import cf.rafl.http.client.Socket;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SecureSocket implements Socket
{

    private final SSLSocket socket;

    private final String host;
    private final int port;

    public SecureSocket(String host, int port) throws IOException
    {
        this.host = host;
        this.port = port;

        this.socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);
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
