package cf.rafl.applicationserver.core.structs;

public final class Microcontroller
{
    public final String controllerID, name, description, owner, ip;

    public Microcontroller(String controllerID, String name, String description, String owner, String ip)
    {
        this.controllerID = controllerID;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.ip = ip;
    }
}
