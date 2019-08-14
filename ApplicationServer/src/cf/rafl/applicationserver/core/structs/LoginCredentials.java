package cf.rafl.applicationserver.core.structs;

public class LoginCredentials
{
    public final String username, password, ip;

    public LoginCredentials(String username, String password, String ip)
    {
        this.username = username;
        this.password = password;
        this.ip = ip;
    }



}
