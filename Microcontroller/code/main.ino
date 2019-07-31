#include <Arduino.h>
#include <EEPROM.h>
#include <HTTPClient.h>
#include <SPIFFS.h>
#include <WiFi.h>
#include <WiFiClientSecure.h>

#define VERSION (0x6)

#define LED 2

#define MAX_CONNECTION_DURATION 5000
#define MAX_MESSAGE_SIZE 10

#define MAX_WAIT_FOR_WIFI 5000

#define TOKEN_LENGTH 32

#define DEBUG

#ifdef DEBUG
    #define PRINT(s) (Serial.print(s))
    #define PRINTLN(s) (Serial.println(s))
#else
    #define PRINT(s) (0)
    #define PRINTLN(s) (0)
#endif

#define ADDRESS_VERSION         0
#define ADDRESS_TOKEN_CREATED   (ADDRESS_VERSION + 1)
#define ADDRESS_TOKEN_START     (ADDRESS_TOKEN_CREATED + 1)
/*       */
#define ADDRESS_TOKEN_END       (ADDRESS_TOKEN_START + TOKEN_LENGTH - 1)
#define EEPROM_SIZE             ADDRESS_TOKEN_END + 1


const char serverAddress[] = "https://app.rafl.cf";
char *ssid;
char *password;

char token[TOKEN_LENGTH + 1] = {'\0'};

char *cert;
WiFiServer server(69);

void update();
void connect();
char* handleIncoming();

void initializeEEPROM();
void generateToken();

void initializeSPIFFS();
void loadWiFiCredentials();
void loadCert();

void fatal();

void setup()
{
    Serial.begin(115200);
    PRINTLN("");

    initializeEEPROM();
    initializeSPIFFS();

    WiFi.begin(ssid, password);

    PRINT("Connecting to Wifi");
    long start = millis();
    while (WiFi.status() != WL_CONNECTED)
    {
        if(millis() >= (start + MAX_WAIT_FOR_WIFI))
        {
            PRINTLN("\nCouldn't connect to Wifi!");
            fatal();
        }
        delay(500);
        PRINT(".");
    }
    Serial.println();

    PRINT("Connected, IP address: ");
    PRINTLN(WiFi.localIP());

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
            Serial.print("Client connected, IP address: ");
            Serial.println(client.remoteIP());
            short messageSize = 0;
            unsigned long connectionEstablished = millis();
            int in;

            while(client.connected())
            {
                //  check for time violation
                if(millis() - connectionEstablished >= MAX_CONNECTION_DURATION)
                {
                    Serial.println("Disconnecting due to connection duration");
                    break;
                }

                //  check for message length violation
                if(messageSize = MAX_MESSAGE_SIZE)
                {
                    Serial.println("Disconnecting due to message size");
                    break;
                }

                //  try to get the next byte
                if((in = client.read()) != -1)
                    message[messageSize++] = (char) in;
            }
        }
        client.stop();
        Serial.println("Client disconnected");
    }
    return message;
}

void initializeEEPROM()
{
    PRINTLN("Initializing EEPROM...");

    EEPROM.begin(EEPROM_SIZE);

    //  wipe eeprom if outdated version (set everything to 0)
    if(EEPROM.read(ADDRESS_VERSION) != VERSION)
    {
        PRINTLN("WIPING EEPROM...");
        for(int i = 0; i < EEPROM_SIZE; i++)
            EEPROM.write(i, 0x0);
        EEPROM.write(ADDRESS_VERSION, VERSION);
        EEPROM.commit();
        PRINTLN("EEPROM wiped!");
    }

    //  generate token if doesn't exist
    if(EEPROM.read(ADDRESS_TOKEN_CREATED) != 0x1)
    {
        generateToken();
        EEPROM.write(ADDRESS_TOKEN_CREATED, 0x1);
        EEPROM.commit();
    }

    //  load in the token 
    for(int i = 0; i < TOKEN_LENGTH; i++)
        token[i] = (char) EEPROM.read(i + ADDRESS_TOKEN_START);

    PRINTLN("EEPROM initialized");

}

//  doesn't commit the change!
void generateToken()
{
    PRINTLN("Generating new Token...");
    srand(millis());
    int tableLength;
    char table[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnoqrstuvwxyz0123456789+/";

    for(tableLength = 0; table[tableLength] != '\0'; tableLength++);

    for(int i = 0; i < TOKEN_LENGTH; i++)
    {
        token[i] = table[(rand() % tableLength)];
        EEPROM.write(i + ADDRESS_TOKEN_START, token[i]);
    }
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
            ssid = (char*) malloc(ssidSize + 1);
            password = (char*) malloc(passwordSize + 1);
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
