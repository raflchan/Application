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

public class UserAPI extends HttpHandler
{
    private static Logger logger = Logger.getLogger(UserAPI.class.getName());

    private HttpRequest httpRequest;

    @Override
    protected void handlePOST(HttpRequest httpRequest) throws IOException
    {

        this.httpRequest = httpRequest;
        String type = httpRequest.getField("type");
        String token = httpRequest.getField("cookie");
        if(type == null || token == null)
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
            if (!UtilDBRequest.validSessionToken(token))
            {
                Responses.invalidSessionToken(exchange);
                return;
            }

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
        } catch (SQLException e)
        {
            Responses.internalServerError(exchange);
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

    private void getVerifyToken()
    {
        // TODO: 09.08.2019  
        String token =
        if()
    }


}
