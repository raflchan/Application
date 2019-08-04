#include <Arduino.h>
#include <BluetoothSerial.h>
#include <EEPROM.h>
#include <HTTPClient.h>
#include <SPIFFS.h>
#include <WiFi.h>
#include <WiFiClientSecure.h>

#include <limits.h>

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

#define VERSION (0x6)

#define LED 2

#define MAX_CONNECTION_DURATION 5000
#define MAX_MESSAGE_SIZE 10

#define BT_MAX_CONNECTION_DURATION INT_MAX
#define BT_MAX_MESSAGE_SIZE 50
#define BT_WAIT_BETWEEN_WRITES 20

#define MAX_WAIT_FOR_WIFI 5000

#define TOKEN_SIZE 32

#define DEBUG

#ifdef DEBUG
    #define PRINT(s) (Serial.print(s))
    #define PRINTLN(s) (Serial.println(s))
#else
    #define PRINT(s) (0)
    #define PRINTLN(s) (0)
#endif

#define ADDRESS_VERSION         0
#define ADDRESS_IS_CONFIGURED   (ADDRESS_VERSION + 1)
#define ADDRESS_TOKEN_START     (ADDRESS_IS_CONFIGURED + 1)
/*       */
#define ADDRESS_TOKEN_END       (ADDRESS_TOKEN_START + TOKEN_SIZE - 1)
#define EEPROM_SIZE             ADDRESS_TOKEN_END + 1

const char serverAddress[] = "https://app.rafl.cf";
char *ssid;
char *password;

char* token;

char *cert;
WiFiServer server(69);

//  auth stuff
bool verifyUserToken(char* tokn);
void registerBoard(char* tokn);


//  BT stuff
bool readLine(BluetoothSerial &SerialBT, char* message, int maxSize, int* size  = nullptr);
bool writeLine(BluetoothSerial &SerialBT, char* message);
char* giveTokn(BluetoothSerial &SerialBT);
char* giveString(BluetoothSerial &SerialBT, char* request);
bool testWiFi();

void update();
void connect();
char* handleIncoming();

bool versionIsCurrent();
bool isConfigured();
void configFirstTime();
void userSetup();

void initializeEEPROM();
char* generateToken();

void initializeSPIFFS();
void loadWiFiCredentials();
void loadCert();

void initializeWiFi(); 

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

void configFirstTime()
{
    EEPROM.begin(EEPROM_SIZE);

    //  wipe eeprom if outdated version (set everything to 0)
    PRINTLN("WIPING EEPROM...");
    for(int i = 0; i < EEPROM_SIZE; i++)
        EEPROM.write(i, 0x0);
    EEPROM.write(ADDRESS_VERSION, VERSION);
    PRINTLN("EEPROM wiped!");

    token = generateToken();
    EEPROM.writeBytes(ADDRESS_TOKEN_START, token, TOKEN_SIZE);

    userSetup();

    EEPROM.write(ADDRESS_IS_CONFIGURED, 0x1);
    EEPROM.commit();
    EEPROM.end();
}

bool readLine(BluetoothSerial &SerialBT, char* message, int maxSize, int* size)
{
    int messageSize = 0;
    char c;
    while(SerialBT.available())
    {
        if(!SerialBT.hasClient())
        return false;
        if(messageSize >= maxSize)
        {
            PRINTLN("BT: stopped reading message, because too long");
            message[0] = '\0';
            return true;
        }
        c = (char) SerialBT.read();
        if(c == '\n' || c == '\r')
            break;
        message[messageSize++] = c;
    }
    
    if(size != nullptr)
        *size = messageSize++;
    message[messageSize] = '\0';

    PRINT("Received message: ");
    PRINTLN(message);

    return true;
}

bool writeLine(BluetoothSerial &SerialBT, char* message)
{
    if(!SerialBT.hasClient())
        return false;
    size_t size = strlen(message);
    SerialBT.write((uint8_t*) message, size);
    SerialBT.write('\n');

    return true;
}

char* giveString(BluetoothSerial &SerialBT, char* request)
{
    PRINT("serving: ");
    PRINTLN(request);


    SerialBT.flush();
    writeLine(SerialBT, request);
    while(!SerialBT.available())
    {
        if(!SerialBT.hasClient())
            return nullptr;
        delay(20);
    }
    char* receive = (char*) malloc(BT_MAX_MESSAGE_SIZE + 1);
    if(!readLine(SerialBT, receive, BT_MAX_MESSAGE_SIZE))
    {
        free(receive);
        return nullptr;
    }
    receive = (char*) realloc(receive, strlen(receive) + 1);

    return receive;
}

char* giveTokn(BluetoothSerial &SerialBT)
{
    bool success = false;
    char* tokn = nullptr;
    int size;
    while(!success)
    {
        if(tokn != nullptr)
            free(tokn);
        tokn = giveString(SerialBT, "GIVE TOKN");
        if(tokn == nullptr)
            return nullptr;
        
        //  verify tokn
        success = (strlen(tokn) == TOKEN_SIZE);
        if(!success)
            PRINTLN("BT: invalid token format, retrying");
    }
    return tokn;
}

bool testWiFi()
{
    PRINTLN("test wifi");

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
        if(WiFi.status() == WL_CONNECTED)
        {
            PRINT("\nConnected, IP address: ");
            PRINTLN(WiFi.localIP());
            // WiFi.disconnect();
            return true;
        }

    }
    return false;
}

bool verifyUserToken(char* tokn)
{
    PRINTLN("Initializing SPIFFS...");
    if(!SPIFFS.begin())
    {
        PRINTLN("Couldn't initialize SPIFFS!");
        fatal();
    }
    loadCert();
    SPIFFS.end();
    WiFiClientSecure *https = new WiFiClientSecure();;

    bool ok = false;

    if(https)
    {
        https->setCACert(cert);
        {
            HTTPClient client;
            
            if(client.begin(*https, "https://app.rafl.cf/verifyUserToken"))
            {
                client.addHeader("Cookie", token);
                int statusCode = client.POST((uint8_t*) tokn, strlen(tokn));

                if(statusCode == HTTP_CODE_OK)
                {
                    PRINTLN("Connected to server!\nContent:");
                    PRINTLN(client.getString());
                    ok = true;
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

    return ok;
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
        char* tokn = giveTokn(SerialBT);
        
        if(tokn == nullptr)
        {
            PRINTLN("BT: Device disconnected during setup, going back to start of userSetup...");
            goto startUserSetup;
        }
        PRINT("Received TOKN: ");
        PRINTLN(tokn);
        while(SerialBT.hasClient())
        {
            ssid = giveString(SerialBT, "GIVE SSID");
            password = giveString(SerialBT, "GIVE PASS");
            if(ssid == nullptr || password == nullptr)
            {
                PRINTLN("BT: Device disconnected during setup, going back to start of userSetup...");
                goto startUserSetup;
            }
            PRINT("Received SSID: ");
            PRINTLN(ssid);
            PRINT("Received PASS: ");
            PRINTLN(password);

            if(testWiFi())
                break;
            else
                writeLine(SerialBT, "INFO INVALID CRED");
        }

        if(!verifyUserToken(tokn))
        {
            writeLine(SerialBT, "INFO INVALID TOKN");
            WiFi.disconnect();
            goto startUserSetup;
        }
        registerBoard(tokn);

        writeLine(SerialBT, "INFO SUCCESSFULL SETUP");
        SerialBT.end();
        free(tokn);
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
    char loadedToken[TOKEN_SIZE + 1];
    //  load in the token 
    for(int i = 0; i < TOKEN_SIZE; i++)
        loadedToken[i] = (char) EEPROM.read(i + ADDRESS_TOKEN_START);
    token = loadedToken;
    EEPROM.end();

    PRINTLN("EEPROM initialized");

}

//  doesn't commit the change!
char* generateToken()
{
    PRINTLN("Generating new Token...");
    srand(esp_random());
    int tableLength;
    char table[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnoqrstuvwxyz0123456789+/";

    for(tableLength = 0; table[tableLength] != '\0'; tableLength++);

    char generatedToken[TOKEN_SIZE + 1] = {'\0'};

    for(int i = 0; i < TOKEN_SIZE; i++)
        generatedToken[i] = table[(rand() % tableLength)];
    PRINT("Generated new Token: ");
    PRINTLN(generatedToken);

    return generatedToken;
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

void initializeWiFi()
{
    for(int i = 0; i < 2; i++)
    {
        WiFi.begin(ssid, password);

        PRINT("Connecting to Wifi");
        long start = millis();
        while(WiFi.status() != WL_CONNECTED)
        {
            if(millis() >= (start + MAX_WAIT_FOR_WIFI))
            {
                PRINTLN("\nCouldn't connect to Wifi!");
                break;
            }
            delay(500);
            PRINT(".");
        }
        if(WiFi.status() == WL_CONNECTED)
        {
            PRINT("\nConnected, IP address: ");
            PRINTLN(WiFi.localIP());
        }
    }
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
