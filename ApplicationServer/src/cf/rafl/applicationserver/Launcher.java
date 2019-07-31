package cf.rafl.applicationserver;

public class Launcher
{


    public static void main(String[] args)
    {
        ApplicationServer server = new ApplicationServer();

        server.start();
        System.out.println("Server running...");

    }
}
