package cf.rafl.http.server;


import cf.rafl.http.core.HttpMessage;
import cf.rafl.http.core.HttpRequest;

import java.io.DataOutputStream;
import java.io.IOException;

public class HttpExchange
{

    private HttpRequest request;
    private DataOutputStream tx;

    HttpExchange(HttpRequest request, DataOutputStream tx)
    {
        this.request = request;
        this.tx = tx;
    }

    public void send(HttpMessage message) throws IOException
    {
        tx.writeBytes(message.getMessage());
    }

    public HttpRequest getRequest()
    {
        return this.request;
    }

}
