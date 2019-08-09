package cf.rafl.applicationserver.requesthandlers;

import cf.rafl.applicationserver.core.security.Hasher;
import cf.rafl.applicationserver.core.security.LoginCredentials;
import cf.rafl.applicationserver.util.Responses;
import cf.rafl.applicationserver.util.Settings;
import cf.rafl.applicationserver.util.UtilDBRequest;
import cf.rafl.http.core.HttpHandler;
import cf.rafl.http.core.HttpRequest;
import cf.rafl.http.core.HttpResponse;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Auth extends HttpHandler
{

    Logger logger = Logger.getLogger(this.getClass().getName());


    public Auth()
    {
        super();
    }

    @Override
    protected void handlePOST(HttpRequest httpRequest) throws IOException
    {


        try
        {
            Settings settings = new Settings(httpRequest.getContent());

            LoginCredentials login = new LoginCredentials(
                    settings.getSetting("username"),
                    settings.getSetting("password"),
                    exchange.getRemoteAddress()
            );

            if (!UtilDBRequest.userExists(login.username))
            {
                invalidCredentials();
                return;
            }

            long creation = UtilDBRequest.getCreationDate(login.username);
            String hash = Hasher.generatePasswordHash(login, creation);
            String storedHash = UtilDBRequest.getPasswordHash(login.username);

            if(!storedHash.equals(hash))
            {
                invalidCredentials();
                return;
            }

            //  valid credentials

            String sessionToken = generateNewSessionToken(login);

            if (!UtilDBRequest.putSessionToken(login, sessionToken))
            {
                Responses.internalServerError(exchange);
                return;
            }

            exchange.send(
                    new HttpResponse.Builder(HttpResponse.StatusCode.OK)
                            .setContent(sessionToken)
                            .build()
            );

            clearExpiredSessionTokens(login.username);

        } catch (Settings.NoSuchSettingException | Settings.BadContentException e)
        {
            exchange.send(
                    new HttpResponse.Builder(HttpResponse.StatusCode.BadRequest)
                            .setContent("bad content format")
                            .build()
            );

        } catch (SQLException e)
        {
            logger.log(Level.WARNING, "", e);
            Responses.internalServerError(exchange);
        }
    }

    private void clearExpiredSessionTokens(String username)
    {
        // TODO: 27.07.2019
    }

    private String generateNewSessionToken(LoginCredentials login)
    {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[128];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
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


    private void invalidCredentials() throws IOException
    {
        exchange.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.Unauthorized)
                        .setContent("login failed due to incorrect username/password")
                        .build()
        );
    }
}
