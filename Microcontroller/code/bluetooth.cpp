#include "bluetooth.h"

#include "defines.h"

bool readLine(BluetoothSerial &SerialBT, char* message, int maxSize, int* size)
{
    //  todo    timeouts
    int messageSize;
    char c;

    for(messageSize = 0; messageSize < maxSize && SerialBT.available(); messageSize++)
    {
        if(!SerialBT.hasClient())
            return false;
        // if(messageSize > maxSize)
        // {
        //     PRINTLN("BT: stopped reading message, because too long");
        //     message[0] = '\0';
        //     return true;
        // }
        c = (char) SerialBT.read();
        if(c == '\n' || c == '\r')
            break;
        message[messageSize] = c;
    }
    
    if(size != nullptr)
        *size = messageSize++;
    message[messageSize] = '\0';

    PRINT("Received message: ");
    PRINTLN(message);

    return true;
}

bool writeLine(BluetoothSerial &SerialBT, const char* message)
{
    if(!SerialBT.hasClient())
        return false;
    size_t size = strlen(message);
    SerialBT.write((uint8_t*) message, size);
    SerialBT.write('\n');
    delay(20);

    return true;
}

bool giveString(BluetoothSerial &SerialBT, const char* request, char* receive, size_t size)
{
    PRINT("serving: ");
    PRINTLN(request);

    SerialBT.flush();
    writeLine(SerialBT, request);
    while(!SerialBT.available())
    {
        if(!SerialBT.hasClient())
            return false;
        delay(20);
    }
    if(!readLine(SerialBT, receive, size))
        return false;

    return true;
}

bool giveTokn(BluetoothSerial &SerialBT, char* token)
{
    bool success = false;
    while(!success)
    {
        if(!giveString(SerialBT, "GIVE TOKN", token, TOKEN_LENGTH))
            return false;
        
        //  verify tokn
        success = (strlen(token) == TOKEN_LENGTH);
        if(!success)
            PRINTLN("BT: invalid token format, retrying");
    }
    return true;
}
