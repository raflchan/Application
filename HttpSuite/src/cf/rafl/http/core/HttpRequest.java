package cf.rafl.http.core;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest extends HttpMessage
{
    public final Method method;
    public final String host;
    public final String path;



    public enum Method
    {
        GET("GET"),
        POST("POST"),
        UNKNOWN("UNKNOWN"),
        ;
        public String method;

        Method(String method)
        {
            this.method = method;
        }

    }
    private HttpRequest(String httpVersion,
                        Method method,
                        String requestPath,
                        String requestHost,
                        Map<String, String> headerFields,
                        String content)
    {
        super(httpVersion, headerFields, content);
        this.method = method;
        this.host = requestHost;
        this.path = requestPath;
    }

    public static Method parseMethod(String method)
    {
        Method mMethod = Method.UNKNOWN;

        for (Method method1 : Method.values())
        {
            if (method.equals(method1.method))
            {
                mMethod = method1;
                break;
            }
        }
        return mMethod;
    }

    @Override
    public String getMessage()
    {
        StringBuilder builder = new StringBuilder();

        builder.append(method.method).append(" ")
                .append(path).append(" ")
                .append(httpVersion).append("\n");

        builder.append("Host: ").append(host).append("\n");
        for (String field : fieldContentMap.keySet())
            builder.append(field).append(": ").append(fieldContentMap.get(field)).append("\n");
        builder.append("\n");
        builder.append(content);

        return builder.toString();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        builder.append("--------------------------------------\n");
        builder.append("HTTP REQUEST\n\n");
        builder.append("Method: ").append(method.method).append("\n");
        builder.append("Path: ").append(path).append("\n");
        builder.append("RemoteAddress: ").append(host).append("\n\n");
        for(String field : fieldContentMap.keySet())
            builder.append(field).append(": ").append(fieldContentMap.get(field)).append("\n");
        builder.append("\n");
        builder.append("Content:").append("\n");
        builder.append(content).append("\n");
        builder.append("--------------------------------------");

        return builder.toString();
    }

    public static class Builder
    {

        private Method requestMethod = Method.GET;

        private String requestHost;
        private String requestPath = "/";
        private String httpVersion = "HTTP/1.1";
        private Map<String, String> fieldContentMap = new HashMap<>();
        private String content;
        public Builder(String host)
        {
            this.requestHost = host;
        }

        public Builder setHttpVersion(String httpVersion)
        {
            this.httpVersion = httpVersion;
            return this;
        }

        public Builder addField(String field, String content)
        {
            fieldContentMap.put(field.toLowerCase(), content);
            return this;
        }

        public String getField(String field)
        {
            return fieldContentMap.get(field);
        }


        public Builder setContent(String content)
        {
            this.content = content;
            return this;
        }

        public Builder setFieldContentMap(Map<String, String> fieldContentMap)
        {
            this.fieldContentMap = fieldContentMap;
            return this;
        }

        public Builder setMethod(Method method)
        {
            this.requestMethod = method;
            return this;
        }

        public Builder setHost(String host)
        {
            this.requestHost = host;
            return this;
        }

        public Builder setRequestPath(String requestPath)
        {
            this.requestPath = requestPath;
            return this;
        }

        public HttpRequest build()
        {
            return new HttpRequest(httpVersion, requestMethod, requestPath, requestHost, fieldContentMap, content);
        }


    }
}
