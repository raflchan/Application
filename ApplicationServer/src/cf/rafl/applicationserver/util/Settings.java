package cf.rafl.applicationserver.util;

import java.util.HashMap;
import java.util.Map;

public class Settings
{

    private Map<String, String> settings;

    public Settings(String content) throws BadContentException
    {
        settings = new HashMap<>();

        for(String line : content.split("\n"))
        {
            String[] split = line.split("=", 2);
            if(split.length != 2)
                throw new BadContentException();

            settings.put(split[0], split[1]);
        }
    }

    public String getSetting(String key) throws NoSuchSettingException
    {
        String setting = settings.get(key);
        if (setting == null)
            throw new NoSuchSettingException();

        return setting;
    }

    public static class NoSuchSettingException extends Exception
    {

    }

    public static class BadContentException extends Exception
    {

    }

}
