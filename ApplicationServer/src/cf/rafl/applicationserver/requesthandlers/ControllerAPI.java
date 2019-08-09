package cf.rafl.applicationserver.requesthandlers;

import cf.rafl.applicationserver.util.Responses;
import cf.rafl.applicationserver.util.UtilDBRequest;
import cf.rafl.http.core.HttpHandler;
import cf.rafl.http.core.HttpRequest;
import cf.rafl.http.core.HttpResponse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerAPI extends HttpHandler
{
    private static Logger logger = Logger.getLogger(ControllerAPI.class.getName());

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

        try
        {
            Method method = this.getClass().getDeclaredMethod(type);
            method.invoke(this);

        } catch (NoSuchMethodException e)
        {
            Responses.invalidType(exchange);
            return;

        } catch (IllegalAccessException | InvocationTargetException e)
        {
            Responses.internalServerError(exchange);
            logger.log(Level.WARNING, "", e);
            return;
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


}
