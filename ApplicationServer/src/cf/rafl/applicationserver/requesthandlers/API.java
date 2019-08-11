package cf.rafl.applicationserver.requesthandlers;

import cf.rafl.applicationserver.core.exceptions.*;
import cf.rafl.applicationserver.core.InterfaceAPI;
import cf.rafl.applicationserver.util.UtilDBRequest;
import cf.rafl.http.core.HttpResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.sql.SQLException;


@SuppressWarnings("unused")
public class API
{
    public static void getVerifyToken(InterfaceAPI api)
            throws WrongMethodException, InvalidSessionTokenException, SQLException, IOException, BadFormatException
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

    public static void getOwnedDevices(InterfaceAPI api)
            throws WrongMethodException, InvalidSessionTokenException, SQLException, IOException, BadFormatException
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

    public void verifyVerificationToken(InterfaceAPI api)
            throws WrongMethodException, InvalidVerificationTokenException, IOException, SQLException, BadFormatException
    {
        api.GET();
        api.verifyVerificationToken();

        api.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.OK)
                        .setContent("valid verification token")
                        .build()
        );
    }

    public void login(InterfaceAPI api)
            throws WrongMethodException
    {
        api.POST();
        // TODO: 12.08.2019 PRIORITY
    }
}
