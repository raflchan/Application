package cf.rafl.applicationserver.requesthandlers;

import cf.rafl.applicationserver.util.UtilDBRequest;
import cf.rafl.http.core.HttpResponse;
import com.google.gson.Gson;


public class API
{
    public static void getVerifyToken(UserAPI api) throws Exception
    {
        api.GET();
        api.verifySessionToken();
        String username = api.getUsername();

        String verificationToken = UtilDBRequest.createVerificationToken(username);

        api.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.OK)
                        .setContent(verificationToken)
                        .build()
        );

    }

    // TODO: 11.08.2019 documentation 
    public static void getOwnedDevices(UserAPI api) throws Exception
    {
        api.GET();
        api.verifySessionToken();
        String username = api.getUsername();

        String json = new Gson().toJson(UtilDBRequest.getUsersMicrocontrollers(username));

        api.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.OK)
                        .setContent(json)
                        .build()
        );

    }
}
