package cf.rafl.http.server;

import cf.rafl.http.core.HttpHandler;
import cf.rafl.http.core.HttpRequest;
import cf.rafl.http.core.HttpResponse;
import com.sun.media.sound.InvalidFormatException;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class HttpConnection
{
    private Socket socket;
    private Map<String, HttpHandler> origContexts;
    private Map<String, HttpHandler> contexts;
    private ConnectionThread connectionThread;


    public HttpConnection(Socket socket, Map<String, HttpHandler> origContexts)
    {
        this.socket = socket;
        this.origContexts = origContexts;

        connectionThread = new ConnectionThread();
    }

    private Map<String, HttpHandler> cloneContexts(Map<String, HttpHandler> contexts)
    {
        Map<String, HttpHandler> map = new HashMap<>();
        for (String field : contexts.keySet())
            map.put(field, contexts.get(field).clone());
        return map;
    }


    public void start()
    {
        connectionThread.start();
    }

    private class ConnectionThread extends Thread
    {

        String remoteAddress;

        private InputStream inputStream;
        private OutputStream outputStream;

        private BufferedReader rx;
        private DataOutputStream tx;



        Map<String, String> headerFields = new HashMap<>();

        private ConnectionThread()
        {
        }

        @Override
        public void run()
        {
            this.setName("ConnectionThread");
            contexts = cloneContexts(origContexts);

//            System.out.println("new connection from: " + socket.getRemoteSocketAddress().toString());
            try
            {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

                rx = new BufferedReader(new InputStreamReader(inputStream));
                tx = new DataOutputStream(outputStream);

                HttpRequest request = new HttpRequestHelper().getHttpRequest();

                getCorrespondingHandler(request.path).handle(
                        new HttpExchange(request,
                                tx,
                                socket.getInetAddress().getHostAddress()
                        )
                );

                close();

            } catch (IOException e)
            {
                // TODO: 14.07.2019 exception handling
                throw new RuntimeException(e);
            }

        }

        private HttpHandler getCorrespondingHandler(String path)
        {
            // TODO: 16.07.2019 make this actually work

            Set<String> paths = contexts.keySet();
            String bestMatch = null;
            int matching = 0;
            for (String s : paths)
            {
                //  perfect match
                if(s.equals(path))
                    return contexts.get(s);

                int cur = getMatching(s, path);
                if(cur > matching)
                {
                    matching = cur;
                    bestMatch = s;
                }
            }

            return bestMatch == null ? new HttpHandler.DefaultHandler() : contexts.get(bestMatch);
        }

        private int getMatching(String s, String path)
        {
            int length = s.length() > path.length() ? path.length() : s.length();

            int i;

            for(i = 0; i < length; i++)
            {
                if(s.charAt(i) != path.charAt(i))
                    break;
            }

            return i;
        }


        private void close()
        {
            try
            {
                inputStream.close();
                outputStream.close();
                socket.close();

            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        private class HttpRequestHelper
        {
            private HttpRequest.Method requestMethod;
            private String httpVersion, requestedPath;
            private String content;

            private HttpRequest getHttpRequest()
            {
                try
                {
                    processRemoteAddress();
                    processRequestLine();
                    processRequestFields();
                    processRequestBody();

                    return new HttpRequest.Builder(remoteAddress)
                            .setMethod(requestMethod)
                            .setHttpVersion(httpVersion)
                            .setRequestPath(requestedPath)
                            .setFieldContentMap(headerFields)
                            .setContent(content)
                            .build();

                } catch (InvalidRequestFormatException e)
                {
                    invalidRequestFormatResponse();
                    throw new RuntimeException(e);
                } catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }

            // TODO: 16.07.2019 add a readchar task that times out if no input is coming or throws exception if connection dropped

            private void processRemoteAddress()
            {
                remoteAddress = socket.getRemoteSocketAddress().toString();
            }

            private void processRequestLine() throws IOException, InvalidRequestFormatException
            {
                String request = rx.readLine();
                String[] requestSplit = request.split(" ");
                if(requestSplit.length != 3)
                    throw new InvalidRequestFormatException();


                requestMethod = HttpRequest.parseMethod(requestSplit[0]);
                requestedPath = requestSplit[1];
                httpVersion = requestSplit[2];

            }

            private void processRequestFields() throws IOException
            {
                String line;

                while(!(line = rx.readLine()).isEmpty())
                {
                    String field, content;
                    String[] split = line.split(": ");
                    if (split.length < 2)
                        throw new InvalidFormatException();

                    field = split[0].toLowerCase();
                    content = line.substring(field.length() + 2);

                    headerFields.put(field, content);
                }
            }

            private void processRequestBody() throws IOException, InvalidRequestFormatException
            {
                content = "";
                if(requestMethod == HttpRequest.Method.POST)
                {
                    String sLength = headerFields.get("content-length");
                    if (sLength == null)
                        throw new InvalidRequestFormatException();
                    else
                    {
                        // TODO: 14.07.2019 this can crash the application
                        long length = Long.parseLong(sLength);
                        StringBuilder builder = new StringBuilder();
                        for(long i = 0; i < length; i++)
                            builder.append((char) rx.read());
                        content = builder.toString();
                    }

                }
            }

            private void invalidRequestFormatResponse()
            {
                try
                {
                    tx.writeBytes(new HttpResponse.Builder(HttpResponse.StatusCode.BadRequest)
                            .setContent("Error in request")
                            .build()
                            .getMessage());

                } catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

    }


    public class InvalidRequestFormatException extends Exception
    {

    }


}
