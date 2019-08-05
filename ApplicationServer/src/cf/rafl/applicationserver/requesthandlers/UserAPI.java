package cf.rafl.applicationserver.requesthandlers;


import cf.rafl.http.core.HttpHandler;
import cf.rafl.http.core.HttpRequest;
import cf.rafl.http.core.HttpResponse;

import java.io.IOException;

public class UserAPI extends HttpHandler
{
    @Override
    protected void handlePOST(HttpRequest httpRequest) throws IOException
    {

        String token = httpRequest.getField("Cookie");
        if(token == null)
        {

            return;
        }


//        if()


    }

    @Override
    protected void handleGET(HttpRequest httpRequest) throws IOException
    {
        handleUNKNOWN(httpRequest);
    }

    @Override
    protected void handleUNKNOWN(HttpRequest httpRequest) throws IOException
    {
        exchange.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.BadRequest)
                        .setContent("Method not allowed")
                        .build()
        );
    }
}
