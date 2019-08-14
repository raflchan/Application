package cf.rafl.applicationserver.util;

import cf.rafl.http.core.HttpResponse;
import cf.rafl.http.server.HttpExchange;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Responses
{
    private static Logger logger = Logger.getLogger(Responses.class.getName());

    public static void internalServerError(HttpExchange exchange, Exception e) throws IOException
    {
        if(e != null)
            logger.log(Level.WARNING, "", e);

        exchange.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.InternalServerError)
                        .setContent("internal server error")
                        .build()
        );
    }

    public static void invalidType(HttpExchange exchange) throws IOException
    {
        exchange.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.BadRequest)
                        .setContent("no such type")
                        .build()
        );
    }

    public static void invalidSessionToken(HttpExchange exchange) throws IOException
    {
        exchange.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.Unauthorized)
                        .setContent("invalid session token")
                        .build()
        );
    }

    public static void wrongMethod(HttpExchange exchange) throws IOException
    {
        exchange.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.BadRequest)
                        .setContent("method not allowed")
                        .build()
        );
    }

    public static void invalidVerificationToken(HttpExchange exchange) throws IOException
    {
        exchange.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.Unauthorized)
                        .setContent("invalid verification token")
                        .build()
        );
    }

    public static void badFormat(HttpExchange exchange) throws IOException
    {
        exchange.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.BadRequest)
                        .setContent("bad format")
                        .build()
        );
    }

    public static void invalidCredentials(HttpExchange exchange) throws IOException
    {
        exchange.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.Unauthorized)
                        .setContent("invalid username/password")
                        .build()
        );
    }

    public static void usernameTaken(HttpExchange exchange) throws IOException
    {
        exchange.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.UnprocessableEntity)
                        .setContent("username taken")
                        .build()
        );
    }
}
