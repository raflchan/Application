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
                        .setContent("Internal Server Error :(")
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
                        .setContent("Method not allowed")
                        .build()
        );
    }
}
