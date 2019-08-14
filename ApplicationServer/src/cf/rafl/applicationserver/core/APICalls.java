package cf.rafl.applicationserver.core;

import cf.rafl.applicationserver.core.exceptions.*;
import cf.rafl.applicationserver.core.security.Hasher;
import cf.rafl.applicationserver.core.structs.LoginCredentials;
import cf.rafl.applicationserver.util.UtilDBRequest;
import cf.rafl.http.core.HttpResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;


@SuppressWarnings("unused")
public class APICalls
{
    public static void getVerifyToken(APIHandlerInterface handler)
            throws WrongMethodException, InvalidSessionTokenException, SQLException, IOException, BadFormatException
    {
        handler.GET();
        handler.verifySessionToken();
        String username = handler.getUsername();

        String verificationToken = UtilDBRequest.createVerificationToken(username);

        handler.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.OK)
                        .setContent(verificationToken)
                        .build()
        );
    }

    public static void getOwnedDevices(APIHandlerInterface handler)
            throws WrongMethodException, InvalidSessionTokenException, SQLException, IOException, BadFormatException
    {
        handler.GET();
        handler.verifySessionToken();
        String username = handler.getUsername();

        String json = new Gson().toJson(UtilDBRequest.getUsersMicrocontrollers(username));

        handler.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.OK)
                        .setContent(json)
                        .build()
        );
    }

    public static void verifyVerificationToken(APIHandlerInterface handler)
            throws WrongMethodException, InvalidVerificationTokenException, IOException, SQLException, BadFormatException
    {
        handler.GET();
        handler.verifyVerificationToken();

        handler.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.OK)
                        .setContent("valid verification token")
                        .build()
        );
    }

    public static void login(APIHandlerInterface handler)
            throws WrongMethodException, BadFormatException, SQLException, InvalidCredentialsException, IOException
    {
        handler.POST();

        LoginCredentials login = handler.getLogin();

        if (!UtilDBRequest.userExists(login.username))
            throw new InvalidCredentialsException();

        long creationDate = UtilDBRequest.getCreationDate(login.username);
        String generatedHash = Hasher.generatePasswordHash(login, creationDate);
        String storedHash = UtilDBRequest.getPasswordHash(login.username);

        if(!storedHash.equals(generatedHash))
            throw new InvalidCredentialsException();

        String sessionToken = UtilDBRequest.createSessionToken(login);

        handler.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.OK)
                .setContent(sessionToken)
                .build()
        );
    }

    public static void register(APIHandlerInterface handler)
            throws IOException, SQLException, UsernameTakenException, WrongMethodException, BadFormatException
    {
        handler.POST();

        LoginCredentials register = handler.getLogin();

        if(UtilDBRequest.userExists(register.username))
            throw new UsernameTakenException();

        long created = (System.currentTimeMillis() / 1000) * 1000;
        String hashedPassword = Hasher.generatePasswordHash(register, created);
        register = new LoginCredentials(register.username, hashedPassword, register.ip);

        UtilDBRequest.putUser(register, new Timestamp(created));

        handler.send(
                new HttpResponse.Builder(HttpResponse.StatusCode.OK)
                        .setContent("account created")
                        .build()
        );

    }
}
