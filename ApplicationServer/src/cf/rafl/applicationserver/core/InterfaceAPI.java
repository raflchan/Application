package cf.rafl.applicationserver.core;

import cf.rafl.applicationserver.core.exceptions.*;
import cf.rafl.http.core.HttpResponse;

import java.io.IOException;
import java.sql.SQLException;

public interface InterfaceAPI
{
    void GET() throws WrongMethodException;
    void POST() throws WrongMethodException;

    void verifySessionToken() throws InvalidSessionTokenException, SQLException, BadFormatException;

    String getUsername() throws SQLException, InvalidSessionTokenException, BadFormatException;

    void send(HttpResponse build)  throws IOException;

    void verifyVerificationToken() throws InvalidVerificationTokenException, BadFormatException, SQLException;
}