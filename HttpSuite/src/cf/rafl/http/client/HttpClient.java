package cf.rafl.http.client;

import cf.rafl.http.client.sockets.JavaSocket;
import cf.rafl.http.client.sockets.SecureSocket;
import cf.rafl.http.core.HttpResponse;

import java.io.*;


public class HttpClient
{
    /* TODO: 17.07.2019
     *   implement max header size
     *   implement timeouts
     * */

    private int port;
    private String host;
    private Socket clientSocket;
    private final Class socketClass;
    private boolean followRedirects;
    private BufferedReader rx;
    private DataOutputStream tx;


    // TODO: 16.07.2019 import trust store 
    
    private HttpClient(String host, int port, Class socketClass, String trustStore, String trustStorePassword)
    {
        this.port = port;
        this.host = host;
        this.socketClass = socketClass;
        this.followRedirects = false;

        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.keyStorePassword", trustStorePassword);
    }

    private HttpClient(String host, int port, Class socketClass)
    {
        this.port = port;
        this.host = host;
        this.socketClass = socketClass;
        this.followRedirects = false;
    }

//    public static void main(String[] args) throws Exception
//    {
//        HttpClient client = new Builder("localhost")
//                .useSSL()
//                .setTrustStore("res/truststores/truststore.jks", "nopassword")
//                .build();
//        long start = System.currentTimeMillis();
//        HttpResponse response = client.GET("/");
//        long stop = System.currentTimeMillis();
//        System.out.println(response.toString());
//        client.close();
//
//        System.out.println("Query took " + (stop - start) + "ms");
//    }

    public void followRedirects()
    {
        this.followRedirects = true;
    }

    public void followRedirects(boolean followRedirects)
    {
        this.followRedirects = followRedirects;
    }

    private boolean checkConnection()
    {
        // TODO: 10.07.2019 make this actually check the connection
        return false;
    }

    private void resetConnection() throws HttpClientException
    {
        try
        {
            clientSocket = (Socket) socketClass
                    .getClassLoader()
                    .loadClass(socketClass.getName())
                    .getDeclaredConstructor(String.class, int.class)
                    .newInstance(host, port);

            tx = new DataOutputStream(clientSocket.getOutputStream());
            rx = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e)
        {
            throw new HttpClientException(e);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void ensureConnection() throws HttpClientException
    {
        if (!checkConnection())
        {
            resetConnection();
        }
    }

    private HttpResponse send(String message) throws HttpClientException
    {
        ensureConnection();
        try
        {
            tx.writeBytes(message);
            tx.flush();
            return receive();

        } catch (IOException e)
        {
            throw new HttpClientException(e);
        }
    }

    private HttpResponse receive() throws HttpClientException
    {
        try
        {
            HttpResponse.Builder responseBuilder;
            responseBuilder = processHeader();
            processBody(responseBuilder);

            return responseBuilder.build();
        } catch (IOException e)
        {
            throw new HttpClientException(e);
        }
    }

    private HttpResponse.Builder processHeader() throws IOException
    {
        // TODO: 10.07.2019 header can be processed with rx.getline() 

        char c;
        StringBuilder line = new StringBuilder();
        int code;

        while((c = (char) rx.read()) != '\n')
        {
            line.append(c);
        }


        code = Integer.parseInt(line.toString().split(" ")[1]);

        HttpResponse.Builder responseBuilder = new HttpResponse.Builder(code);
        responseBuilder.setHttpVersion(line.toString().split(" ")[0]);

        line = new StringBuilder();


        while(true)
        {
            c = (char) rx.read();
            if (c == '\n' || c == '\r')
            {
                String sLine = line
                        .toString()
                        .replace("\r", "")
                        .replace("\n", "");

                if (sLine.isEmpty())
                    break;
                else
                {

                    String field = sLine.split(": ")[0];
                    String content = sLine.substring(field.length() + 2);

                    responseBuilder.addField(field.toLowerCase(), content);
                    line = new StringBuilder();
                }
            } else
                line.append(c);

        }
        return responseBuilder;
    }

    private HttpResponse.Builder processBody(HttpResponse.Builder responseBuilder) throws IOException
    {
        StringBuilder content = new StringBuilder();
        String contentLength;
        int cl = -1;


        if ((contentLength = responseBuilder.getField(HttpResponse.HeaderField.ContentLength.field)) != null)
            cl = Integer.parseInt(contentLength);

        if (cl >= 0)
        {
            for(int i = 0; i < cl; i++)
                content.append((char) rx.read());
        }
        else
        {
            int c;
            while((c = rx.read()) != -1)
                content.append((char) c);
        }

        return responseBuilder.setContent(content.toString());
    }

    public void close() throws HttpClientException
    {
        try
        {
            rx.close();
            tx.close();
            clientSocket.close();
        } catch (IOException e)
        {
            throw new HttpClientException(e);
        }
    }

    public HttpResponse GET(String subdomain) throws HttpClientException
    {

        String get = "GET " + subdomain + " HTTP/1.1\r\n" +
                "Host: " + this.host + ":" + port + "\r\n" +
                "User-Agent: raflhttp/1.0\r\n" +
                "\r\n";
        return send(get);
    }

    public HttpResponse POST(String subdomain, String content) throws HttpClientException
    {

        String get = "POST " + subdomain + " HTTP/1.1\r\n" +
                "Host: " + this.host + ":" + port + "\r\n" +
                "User-Agent: raflhttp/1.0\r\n" +
                "Content-Length: " + content.length() + "\r\n" +
                "\r\n" +
                content;
        return send(get);
    }

    public static class Builder
    {
        private int port = 80;
        private String host;
        private Class socketClass = JavaSocket.class;
        private String trustStore = null, trustStorePassword = null;

        public Builder(String host)
        {
            this.host = host;
        }

        public Builder useSSL()
        {
            this.socketClass = SecureSocket.class;
            this.port = 443;
            return this;
        }

        public Builder setPort(int port)
        {
            this.port = port;
            return this;
        }

        public Builder setTrustStore(String trustStore, String trustStorePassword)
        {
            this.trustStore = trustStore;
            this.trustStorePassword = trustStorePassword;
            return this;
        }

        public HttpClient build()
        {
            if(trustStore == null || trustStorePassword == null)
                return new HttpClient(host, port, socketClass);
            else
                return new HttpClient(host, port, socketClass, trustStore, trustStorePassword);
        }


    }

    public class HttpClientException extends Exception
    {
        private HttpClientException(Exception e)
        {
            e.printStackTrace();
        }
    }
}
