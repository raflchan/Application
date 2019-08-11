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
        handle(httpRequest);
    }

    @Override
    protected void handleGET(HttpRequest httpRequest) throws IOException
    {
        handle(httpRequest);
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

    private void handle(HttpRequest httpRequest) throws IOException
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
            Method method = API.class.getMethod(type, this.getClass());
            method.invoke(new API(), this);

        } catch (NoSuchMethodException e)
        {
            Responses.invalidType(exchange);

        } catch (IllegalAccessException e)
        {
            Responses.internalServerError(exchange);
            logger.log(Level.WARNING, "", e);

        } catch (InvocationTargetException e)
        {
            Class exception = e.getCause().getClass();

            if(exception.equals(WrongMethodException.class))
                Responses.wrongMethod(exchange);
            else if(exception.equals(InvalidSessionTokenException.class))
                Responses.invalidSessionToken(exchange);
            else if (exception.equals(SQLException.class))
                Responses.internalServerError(exchange);
            else
            {
                Responses.internalServerError(exchange);
                logger.log(Level.WARNING, "Unfiltered exception", e);
            }

        }
    }

    public void POST() throws WrongMethodException
    {
        if(httpRequest.method != HttpRequest.Method.POST)
            throw new WrongMethodException();
    }

    public void GET() throws WrongMethodException
    {
        if(httpRequest.method != HttpRequest.Method.GET)
            throw new WrongMethodException();
    }

    public void verifySessionToken() throws InvalidSessionTokenException, SQLException
    {
        if (!UtilDBRequest.validSessionToken(getSessionToken()))
            throw new InvalidSessionTokenException();
    }

    public String getSessionToken() throws InvalidSessionTokenException
    {
        String token = httpRequest.getField("cookie");

        if (token == null)
            throw new InvalidSessionTokenException();
        return token;
    }


    public String getUsername() throws SQLException, InvalidSessionTokenException
    {
        return UtilDBRequest.getUserFromSessionToken(getSessionToken());
    }

    public String getUserFromSessionToken(String sessionToken) throws SQLException
    {
        return UtilDBRequest.getUserFromSessionToken(sessionToken);
    }

    public void send(HttpResponse response) throws IOException
    {
        exchange.send(response);
    }

    class WrongMethodException extends Exception
    {

    }

    class InvalidSessionTokenException extends Exception
    {

    }
}
