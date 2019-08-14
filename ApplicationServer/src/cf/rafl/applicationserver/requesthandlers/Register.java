package cf.rafl.applicationserver.requesthandlers;

import cf.rafl.applicationserver.core.security.Hasher;
import cf.rafl.applicationserver.core.structs.LoginCredentials;
import cf.rafl.applicationserver.util.Responses;
import cf.rafl.applicationserver.util.Settings;
import cf.rafl.applicationserver.util.UtilDBRequest;
import cf.rafl.http.core.HttpHandler;
import cf.rafl.http.core.HttpRequest;
import cf.rafl.http.core.HttpResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Register extends HttpHandler
{
    private static Logger logger = Logger.getLogger(Register.class.getName());
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


            if (UtilDBRequest.userExists(login.username))
            {
                exchange.send(
                        new HttpResponse.Builder(HttpResponse.StatusCode.UnprocessableEntity)
                                .setContent("username taken")
                                .build()
                );
                return;
            }

            long created = (System.currentTimeMillis() / 1000) * 1000;
            String hashedPassword = Hasher.generatePasswordHash(login, created);
            login = new LoginCredentials(login.username, hashedPassword, login.ip);

            if (!UtilDBRequest. putUser(login, new Timestamp(created)))
            {

                Responses.internalServerError(exchange, null);
                return;
            }

            //  cant log into account that got created

            exchange.send(
                    new HttpResponse.Builder(HttpResponse.StatusCode.OK)
                            .setContent("Successfully created account!")
                            .build()
            );

        } catch (Settings.BadContentException | Settings.NoSuchSettingException e)
        {
            exchange.send(
                    new HttpResponse.Builder(HttpResponse.StatusCode.BadRequest)
                            .setContent("bad content format")
                            .build()
            );
        } catch (SQLException e)
        {
            logger.log(Level.WARNING, "", e);
            Responses.internalServerError(exchange, e);
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
        HttpResponse response = new HttpResponse.Builder(HttpResponse.StatusCode.BadRequest)
                .setContent("Method not allowed")
                .build();
        exchange.send(response);
    }

}
