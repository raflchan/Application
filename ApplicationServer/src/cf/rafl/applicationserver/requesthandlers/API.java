package cf.rafl.applicationserver.requesthandlers;


import cf.rafl.applicationserver.core.APICalls;
import cf.rafl.applicationserver.core.exceptions.*;
import cf.rafl.applicationserver.core.APIHandlerInterface;
import cf.rafl.applicationserver.core.structs.LoginCredentials;
import cf.rafl.applicationserver.util.Responses;
import cf.rafl.applicationserver.util.Settings;
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

public class API extends HttpHandler implements APIHandlerInterface
{
    private static Logger logger = Logger.getLogger(API.class.getName());

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
            Method method = APICalls.class.getMethod(type, APIHandlerInterface.class);
            method.invoke(new APICalls(), this);
            // TODO: 14.08.2019 make this better logging 
            logger.log(Level.INFO, "Invoked: " + method.getName());

        } catch (NoSuchMethodException e)
        {
            Responses.invalidType(exchange);

        } catch (IllegalAccessException e)
        {
            Responses.internalServerError(exchange, e);
            logger.log(Level.WARNING, "", e);

        } catch (InvocationTargetException e)
        {
            Class exception = e.getCause().getClass();

            if(exception.equals(WrongMethodException.class))
                Responses.wrongMethod(exchange);
            else if(exception.equals(InvalidSessionTokenException.class))
                Responses.invalidSessionToken(exchange);
            else if(exception.equals(InvalidVerificationTokenException.class))
                Responses.invalidVerificationToken(exchange);
            else if(exception.equals(SQLException.class))
                Responses.internalServerError(exchange,e );
            else if(exception.equals(BadFormatException.class))
                Responses.badFormat(exchange);
            else if(exception.equals(InvalidCredentialsException.class))
                Responses.invalidCredentials(exchange);
            else if(exception.equals(IOException.class))
                Responses.internalServerError(exchange, e);
            else if(exception.equals(UsernameTakenException.class))
                Responses.usernameTaken(exchange);
            else
            {
                Responses.internalServerError(exchange, e);
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

    public void verifySessionToken() throws InvalidSessionTokenException, SQLException, BadFormatException
    {
        if (!UtilDBRequest.validSessionToken(getSessionToken()))
            throw new InvalidSessionTokenException();
    }

    public String getSessionToken() throws BadFormatException
    {
        String token = httpRequest.getField("session-token");

        if (token == null)
            throw new BadFormatException();
        return token;
    }

    public String getUsername() throws SQLException, BadFormatException
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

    public void verifyVerificationToken() throws InvalidVerificationTokenException, BadFormatException, SQLException
    {
        String token = httpRequest.getField("verification-token");

        if(token == null)
            throw new BadFormatException();
        if(!UtilDBRequest.validVerificationToken(token))
            throw new InvalidVerificationTokenException();
    }

    @Override
    public LoginCredentials getLogin() throws BadFormatException
    {
        try
        {
            Settings settings = new Settings(exchange.getRequest().content);


            return new LoginCredentials(
                    settings.getSetting("username"),
                    settings.getSetting("password"),
                    exchange.getRemoteAddress()
            );
        } catch (Settings.BadContentException | Settings.NoSuchSettingException e)
        {
            throw new BadFormatException();
        }
    }


}
