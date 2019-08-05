package cf.rafl.applicationserver.requesthandlers;

import cf.rafl.applicationserver.util.UtilDBRequest;
import cf.rafl.http.core.HttpHandler;
import cf.rafl.http.core.HttpRequest;
import cf.rafl.http.core.HttpResponse;

import java.io.IOException;
import java.sql.SQLException;

public class VerifyUserToken extends HttpHandler
{
    @Override
    protected void handlePOST(HttpRequest httpRequest) throws IOException
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
