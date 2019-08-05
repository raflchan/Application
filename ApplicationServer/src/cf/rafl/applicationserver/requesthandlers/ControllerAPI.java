package cf.rafl.applicationserver.requesthandlers;

import cf.rafl.applicationserver.util.UtilDBRequest;
import cf.rafl.http.core.HttpHandler;
import cf.rafl.http.core.HttpRequest;
import cf.rafl.http.core.HttpResponse;

import java.io.IOException;
import java.sql.SQLException;

public class ControllerAPI extends HttpHandler
{

    HttpRequest httpRequest;

    @Override
    protected void handlePOST(HttpRequest httpRequest) throws IOException
    {
        this.httpRequest = httpRequest;
        String type = httpRequest.getField("type");
        if(type == null)
        {
            exchange.send(
                    new HttpResponse.Builder(HttpResponse.StatusCode.BadRequest)
                            .setContent("specify type field")
                            .build()
            );
            return;
        }

        //  reflection!

        if(type.equals("verifyUserToken"))
            verifyUserToken();
        else
            invalidType();



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

    private void verifyUserToken() throws IOException
    {
        String verificationToken = httpRequest.getContent();
        try
        {
            if(UtilDBRequest.verificationTokenExists(verificationToken))
            {
                exchange.send(
                        new HttpResponse.Builder(HttpResponse.StatusCode.OK)
                                .setContent("valid token")
                                .build()
                );
            }
            else
            {
                exchange.send(
                        new HttpResponse.Builder(HttpResponse.StatusCode.Unauthorized)
                                .setContent("no such token")
                                .build()
                );
            }

        } catch (SQLException e)
        {
            exchange.send(
                    new HttpResponse.Builder(HttpResponse.StatusCode.InternalServerError)
                            .setContent("Internal Server Error :(")
                            .build()
            );
        }
    }

    private void invalidType() throws IOException
    {
        exchange.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.BadRequest)
                .setContent("no such type")
                .build()
        );
    }
}
