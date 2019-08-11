package cf.rafl.applicationserver.util;

import cf.rafl.http.core.HttpResponse;
import cf.rafl.http.server.HttpExchange;

import java.io.IOException;

public class Responses
{
    public static void internalServerError(HttpExchange exchange) throws IOException
    {
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

    public void invalidCredentials(HttpExchange exchange) throws IOException
    {
        exchange.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.Unauthorized)
                        .setContent("login failed due to incorrect username/password")
                        .build()
        );
    }
}
