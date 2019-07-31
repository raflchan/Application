package cf.rafl.http.core;

import cf.rafl.http.server.HttpExchange;

import java.io.IOException;

public abstract class HttpHandler implements Cloneable
{
    protected HttpExchange exchange;

    public void handle(HttpExchange exchange) throws IOException
    {
        this.exchange = exchange;
        HttpRequest request = exchange.getRequest();

//        System.out.println(request.toString());

        switch (request.method)
        {
            case GET:
                handleGET(request);
                break;
            case POST:
                handlePOST(request);
                break;
            default:
                handleUNKNOWN(request);
                break;
        }

    }

    /**
     * This method handles GET requests
     * @param request The handled {@link HttpRequest}
     */
    protected abstract void handleGET(HttpRequest request) throws IOException;

    /**
     * This method handles POST requests
     * @param request The handled {@link HttpRequest}
     */
    protected abstract void handlePOST(HttpRequest request) throws IOException;

    /**
     * This method handles UNKNOWN requests
     * @param request The handled {@link HttpRequest}
     */
    protected abstract void handleUNKNOWN(HttpRequest request) throws IOException;

    /**
     * @param response The to be sent {@link HttpResponse}
     */
    protected void respond(HttpResponse response) throws IOException
    {
        exchange.send(response);
    }

    public HttpHandler clone()
    {
        try
        {
            return (HttpHandler) super.clone();

        } catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static class DefaultHandler extends HttpHandler
    {

        @Override
        protected void handleGET(HttpRequest request) throws IOException
        {
            handleUNKNOWN(request);
        }

        @Override
        protected void handlePOST(HttpRequest request) throws IOException
        {
            handleUNKNOWN(request);
        }

        @Override
        protected void handleUNKNOWN(HttpRequest request) throws IOException
        {
            exchange.send(new HttpResponse.Builder(HttpResponse.StatusCode.NotFound).setContent("Not Found").build());
        }
    }
}
