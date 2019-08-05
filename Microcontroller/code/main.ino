#include <Arduino.h>
#include <BluetoothSerial.h>
#include <EEPROM.h>
#include <HTTPClient.h>
#include <SPIFFS.h>
#include <WiFi.h>
#include <WiFiClientSecure.h>

#include <limits.h>


#include "bluetooth.h"
#include "defines.h"


const char serverAddress[] = "app.rafl.cf";
char ssid[MAX_SSID_LENGTH + 1] = {'\0'};
char password[MAX_PASSWORD_LENGHT + 1] = {'\0'};

char token[TOKEN_LENGTH + 1] = {'\0'};

char *cert;

HTTPClient* httpsClient;
WiFiServer server(69);


//  auth stuff
bool verifyUserToken(const char* tokn);
void registerBoard(const char* tokn);




bool connectWiFi();

void update();
void connect();
char* handleIncoming();

bool versionIsCurrent();
bool isConfigured();
void configFirstTime();
void userSetup();

void initializeEEPROM();
void generateToken(char* token);

void initializeSPIFFS();
void loadWiFiCredentials();
void loadCert();

void initializeWiFi();

void initializeHttpsClient();
int POST(const char* payload);

void fatal();

void setup()
{
    Serial.begin(115200);
    PRINTLN("");

    userSetup();

    PRINTLN("RESTARTING");
    fatal();

    // if(!isConfigured())
    // {
    //     PRINTLN("Board is not configured\nConfiguring board...");
    //     configFirstTime();
    //     PRINTLN("Board configured");
    // }

    // initializeEEPROM();
    // initializeSPIFFS();
    // initializeWiFi();


    server.begin();
}

void loop()
{
    String message = handleIncoming();
    if(message.length() > 0)
    {
        PRINT("Received Message: ");
        PRINTLN(message);
        if(message == "CONNECT")
            connect();

    }
    updateSecure();
    delay(3600000);
}

void updateSecure()
{
    PRINTLN("Running updateSecure()...");
    WiFiClientSecure *https = new WiFiClientSecure();;

    if(https)
    {
        https->setCACert(cert);
        {
            HTTPClient client;
            if(client.begin(*https, serverAddress))
            {
                client.addHeader("Cookie", token);
                int statusCode = client.GET();

                if(statusCode == HTTP_CODE_OK)
                {
                    PRINTLN("Connected to server!\nContent:");
                    PRINTLN(client.getString());
                }
                else
                {
                    PRINT("Couldn't establish connection with server!\nStatus Code: ");
                    PRINT(statusCode);
                    PRINT(" (");
                    PRINT(client.errorToString(statusCode));
                    PRINTLN(")");
                }
            }
            else
                PRINTLN("Client couldn't establish the connection???");
        }
        delete https;
    }
    else
        PRINTLN("WiFiClientSecure not initialized!");
}

void connect()
{
    //  todo
    PRINTLN("This is where you connect to the Server with a GET");
}

char *handleIncoming()
{
    WiFiClient client = server.available();
    char message[MAX_MESSAGE_SIZE + 1] = {'\0'};

    if(client)
    {
        if(client.connected())
        {
            PRINT("Client connected, IP address: ");
            PRINTLN(client.remoteIP());
            short messageSize = 0;
            unsigned long connectionEstablished = millis();
            int in;

            while(client.connected())
            {
                //  check for time violation
                if(millis() - connectionEstablished >= MAX_CONNECTION_DURATION)
                {
                    PRINTLN("Disconnecting due to connection duration");
                    break;
                }

                //  check for message length violation
                if(messageSize = MAX_MESSAGE_SIZE)
                {
                    PRINTLN("Disconnecting due to message size");
                    break;
                }

                //  try to get the next byte
                if((in = client.read()) != -1)
                    message[messageSize++] = (char) in;
            }
        }
        client.stop();
        PRINTLN("Client disconnected");
    }
    return message;
}

bool versionIsCurrent()
{
    EEPROM.begin(EEPROM_SIZE);
    bool currentVersion = EEPROM.read(ADDRESS_VERSION) == VERSION;
    EEPROM.end();
    return currentVersion;
}

bool isConfigured()
{
    if(versionIsCurrent())
    {
        EEPROM.begin(EEPROM_SIZE);
    
        int value = EEPROM.read(ADDRESS_IS_CONFIGURED);
        if(value == 0x1)
        {
            EEPROM.end();
            return true;
        }
    }

    EEPROM.end();
    return false;
}

int POST(const char* payload)
{
    int status = httpsClient->POST(payload);
    httpsClient->end();
    return status;
}

void initializeHttpsClient()
{
    PRINTLN("Initializing HTTPSClient");

    static bool initialized = false;

    if(!initialized)
    {
        PRINTLN("Initializing SPIFFS...");
        if(!SPIFFS.begin())
        {
            PRINTLN("Couldn't initialize SPIFFS!");
            fatal();
        }
        loadCert();
        SPIFFS.end();
        httpsClient = new HTTPClient();
        initialized = true;
    }
    
    httpsClient->begin(serverAddress, 443, "/controllerapi", cert);
    if(!httpsClient)
    {
        PRINTLN("COULDN'T CONNECT TO SERVER");
        fatal();
    }

    PRINTLN("Connected to Server");


}

void configFirstTime()
{
    EEPROM.begin(EEPROM_SIZE);

    //  wipe eeprom if outdated version (set everything to 0)
    PRINTLN("WIPING EEPROM...");
    for(int i = 0; i < EEPROM_SIZE; i++)
        EEPROM.write(i, 0x0);
    EEPROM.write(ADDRESS_VERSION, VERSION);
    PRINTLN("EEPROM wiped!");

    generateToken(token);
    EEPROM.writeBytes(ADDRESS_TOKEN_START, token, TOKEN_LENGTH);

    userSetup();

    EEPROM.write(ADDRESS_IS_CONFIGURED, 0x1);
    EEPROM.commit();
    EEPROM.end();
}




bool connectWiFi()
{
    PRINTLN("connectWiFi:");

    for(int i = 0; i < 2; i++)
    {
        PRINT("Attempt ");
        PRINTLN(i + 1);
        WiFi.begin(ssid, password);

        long start = millis();
        PRINT("Connecting to Wifi");
        while(WiFi.status() != WL_CONNECTED)
        {
            if(millis() >= (start + MAX_WAIT_FOR_WIFI))
                break;
            delay(500);
            PRINT(".");
        }
        PRINTLN("");
        if(WiFi.status() == WL_CONNECTED)
        {
            PRINT("\nConnected, IP address: ");
            PRINTLN(WiFi.localIP());
            return true;
        }
    }
    WiFi.disconnect();
    return false;
}

bool verifyUserToken(char* tokn)
{
    initializeHttpsClient();
    httpsClient->addHeader("Type", "verifyUserToken");
    int statusCode = POST(tokn);

    if(statusCode != HTTP_CODE_OK)
    {
        PRINT("Couldn't establish connection with server!\nStatus Code: ");
        PRINT(statusCode);
        PRINT(" (");
        PRINT(httpsClient->errorToString(statusCode));
        PRINTLN(")");
        return false;
    }

    PRINTLN("Connected to server!\nContent:");
    PRINTLN(httpsClient->getString());

    return true;

}

void registerBoard(char* tokn)
{
    PRINTLN("this is where the board registers itself :)");
}

void userSetup()
{
    //  todo
    PRINTLN("This is where the user setup happens");
    initializeEEPROM();

    BluetoothSerial SerialBT;
    SerialBT.begin("ESP32");

    startUserSetup:

    PRINT("Waiting for BT connection to setup device");

    while(!SerialBT.hasClient())
    {
        delay(1000);
        PRINT(".");
    }
    PRINTLN("\nDevice connected!");

    bool complete = false;
    
    while(SerialBT.hasClient())
    {
        if(!giveTokn(SerialBT, token))
        {
            PRINTLN("BT: Device disconnected during setup, going back to start of userSetup...");
            goto startUserSetup;
        }
        PRINT("Received TOKN: ");
        PRINTLN(token);
        while(SerialBT.hasClient())
        {
            if(
                !giveString(SerialBT, "GIVE SSID", ssid, MAX_SSID_LENGTH) ||
                !giveString(SerialBT, "GIVE PASS", password, MAX_PASSWORD_LENGHT)
            )
            {
                PRINTLN("BT: Device disconnected during setup, going back to start of userSetup...");
                goto startUserSetup;
            }
            PRINT("Received SSID: ");
            PRINTLN(ssid);
            PRINT("Received PASS: ");
            PRINTLN(password);

            if(connectWiFi())
                break;
            else
            {
                writeLine(SerialBT, "INFO INVALID CRED");
                PRINTLN("INVALID CRED");
            }
        }

        while(!verifyUserToken(token))
        {
            writeLine(SerialBT, "INFO INVALID TOKN");
            PRINTLN("INVALID TOKN");
            if(!giveTokn(SerialBT, token))
            {
                PRINTLN("BT: Device disconnected during setup, going back to start of userSetup...");
                WiFi.disconnect();
                goto startUserSetup;
            }
            PRINT("Received TOKN: ");
            PRINTLN(token);
        }
        registerBoard(token);

        writeLine(SerialBT, "INFO SUCCESSFULL SETUP");
        SerialBT.end();
    }
    PRINTLN("Device disconnected!");


}

/*

GIVE
INFO
RECV

*/

void initializeEEPROM()
{
    PRINTLN("Initializing EEPROM...");

    EEPROM.begin(EEPROM_SIZE);
    //  load in the token 
    for(int i = 0; i < TOKEN_LENGTH; i++)
        token[i] = (char) EEPROM.read(i + ADDRESS_TOKEN_START);
    EEPROM.end();

    PRINTLN("EEPROM initialized");

}

//  doesn't commit the change!
void generateToken(char* token)
{
    PRINTLN("Generating new Token...");
    srand(esp_random());
    int tableLength;
    char table[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnoqrstuvwxyz0123456789+/";

    for(tableLength = 0; table[tableLength] != '\0'; tableLength++);

    for(int i = 0; i < TOKEN_LENGTH; i++)
        token[i] = table[(rand() % tableLength)];
    PRINT("Generated new Token: ");
    PRINTLN(token);
}

void initializeSPIFFS()
{
    PRINTLN("Initializing SPIFFS...");
    if(!SPIFFS.begin())
    {
        PRINTLN("Couldn't initialize SPIFFS!");
        fatal();
    }

    loadWiFiCredentials();
    loadCert();

    PRINTLN("SPIFFS initialized");
}

void loadCert()
{
    PRINTLN("Loading Cert...");
    static bool loaded = false;
    if(loaded)
    {
        PRINTLN("Cert already loaded!");
        return;
    }
    File file = SPIFFS.open("/cert.cer");
    if(!file)
    {
        PRINTLN("Couldn't open cert.cer");
        fatal();
    }
    
    unsigned long certSize = file.size();

    cert = (char*) malloc(certSize + 1);

    int i;
    for(i = 0; i < certSize; i++)
        *(cert + i) = (char) file.read();
    *(cert + i) = '\0';

    file.close();

    // PRINTLN("Certificate:");
    // PRINTLN(cert);
    PRINTLN("Loaded Cert");
    loaded = true;

}

void loadWiFiCredentials()
{
    PRINTLN("Loading WiFi Credentials...");
    File file = SPIFFS.open("/login.dat");
    if(!file)
    {
        PRINTLN("Couldn't open login.dat");
        fatal();
    }
    
    unsigned long loginSize = file.size();

    char* login = (char*) malloc(loginSize + 1);

    int i;
    char c;
    for(i = 0; file.available() && i < loginSize; i++)
    {
        c = (char) file.read();
        *(login + i) = c;
    }
    *(login + i) = '\0';

    for(i = 0; i < loginSize; i++)
    {
        c = *(login + i);
        if(c == '\n')
        {
            int j;
            long ssidSize = i - 1;
            long passwordSize = loginSize - ssidSize;
            for(j = 0; j < i - 1; j++)
                *(ssid + j) = *(login + j);
            *(ssid + ssidSize) = '\0';
            for(j = 0; i < loginSize; i++, j++)
                *(password + j) = *(login + i + 1);
            *(password + passwordSize) = '\0';
            free(login);
            break;
        }
    }
    file.close();

    PRINT("SSID: ");
    PRINTLN(ssid);
    PRINT("Pasword: ");
    PRINTLN(password);

    PRINTLN("Loaded WiFi Credentials");

}

void initializeWiFi()
{
    if(!connectWiFi)
        fatal();
}

void fatal()
{
    PRINT("A fatal error has occured, restarting in 10 seconds");
    for(int i = 0; i < 10; i++)
    {
        delay(1000);
        PRINT(".");
    }
    ESP.restart();
}
