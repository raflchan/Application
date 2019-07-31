package cf.rafl.http.core;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse extends HttpMessage
{

    public final int statusCode;


    private HttpResponse(int statusCode, String httpVersion, Map<String, String> fieldContentMap, String content)
    {
        super(httpVersion, fieldContentMap, content);
        this.statusCode = statusCode;
    }



    public static StatusCode codeToStatus(int statusCode)
    {
        for (StatusCode status : StatusCode.values())
        {
            if (statusCode == status.code)
                return status;
        }

        return null;
    }

    public int getStatusCode()
    {
        return statusCode;
    }


    @Override
    public String getMessage()
    {
        StringBuilder builder = new StringBuilder();

        builder.append(httpVersion).append(" ").append(statusCode).append("\n");
        for (String field : fieldContentMap.keySet())
            builder.append(field).append(": ").append(fieldContentMap.get(field)).append("\n");
        builder.append("\n");
        if(content != null)
            builder.append(content);

        return builder.toString();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        builder.append("--------------------------------------\n");
        builder.append("HTTP RESPONSE\n\n");
        builder.append("Http Version: ").append(httpVersion).append("\n");
        builder.append("Status Code:  ").append(statusCode);
        if (codeToStatus(statusCode) != null)
            builder.append(" ").append(codeToStatus(statusCode).status);
        builder.append("\n\n");

        for (String field : fieldContentMap.keySet())
            builder.append(field).append(": ").append(fieldContentMap.get(field)).append("\n");
        builder.append("\nContent:\n");
        builder.append(this.content).append("\n");
        builder.append("--------------------------------------\n");

        return builder.toString();
    }


    public static class Builder
    {

        private int statusCode;
        private String httpVersion = "HTTP/1.1";
        private Map<String, String> fieldContentMap = new HashMap<>();
        private String content;


        public Builder(int statusCode)
        {
            this.statusCode = statusCode;
        }

        public Builder(StatusCode statusCode)
        {
            this.statusCode = statusCode.code;
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

        public HttpResponse build()
        {
            fieldContentMap.remove("content-length");
            if(this.content != null)
                addField("content-length", Integer.toString(this.content.length()));

            return new HttpResponse(this.statusCode, this.httpVersion, this.fieldContentMap, this.content);
        }

        public Builder setStatusCode(StatusCode statusCode)
        {
            this.statusCode = statusCode.code;
            return this;
        }

        public Builder setStatusCode(int statusCode)
        {
            this.statusCode = statusCode;
            return this;
        }
    }

    public enum StatusCode
    {
        UNINITIALIZED(0, "UNINITIALIZED"),

        OK(200, "OK"),

        MovedPermanently(301, "Moved Permanently"),
        Found(302, "Found"),

        BadRequest(400, "Bad Request"),
        Unauthorized(401, "Unauthorized"),
        Forbidden(403, "Forbidden"),
        NotFound(404, "Not Found"),
        MethodNotAllowed(405, "Method Not Allowed"),
        UnprocessableEntity(422, "Unprocessable Entity"),
        InternalServerError(500,"Internal Server Error"),

        ;

        public final int code;
        public final String status;

        StatusCode(int code, String status)
        {
            this.code = code;
            this.status = status;
        }

    }
}
