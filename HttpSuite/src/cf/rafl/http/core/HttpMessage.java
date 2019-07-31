package cf.rafl.http.core;

import java.util.Map;

public abstract class HttpMessage
{
    public final String httpVersion;
    public final Map<String, String> fieldContentMap;
    public final String content;

    public enum HeaderField
    {
        ContentType("content-type"),
        ContentLength("content-length"),
        Date("date"),
        ;
        public String field;

        HeaderField(String field)
        {
            this.field = field;
        }

    }

    protected HttpMessage(String httpVersion, Map<String, String> fieldContentMap, String content)
    {
        this.httpVersion = httpVersion;
        this.fieldContentMap = fieldContentMap;
        this.content = content;
    }

    public String getHttpVersion()
    {
        return httpVersion;
    }

    public String getField(String field)
    {
        return fieldContentMap.get(field);
    }

    public String getContent()
    {
        return content;
    }

    public long length()
    {
        return content.length();
    }

    public byte[] getBytes()
    {
        return content.getBytes();
    }

    public abstract String getMessage();

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        builder.append("--------------------------------------\n");
        builder.append("HTTP MESSAGE\n\n");
        builder.append("Http Version: ").append(httpVersion).append("\n");
        builder.append("\n");
        for (String field : fieldContentMap.keySet())
            builder.append(field).append(": ").append(fieldContentMap.get(field)).append("\n");
        builder.append("\n");
        builder.append("Content:\n");
        builder.append(this.content).append("\n");
        builder.append("--------------------------------------\n");

        return builder.toString();
    }
}
