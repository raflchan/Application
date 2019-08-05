#ifndef DEFINES_H
#define DEFINES_H

#define DEBUG

#define VERSION (0x6)

#define LED 2

#define MAX_CONNECTION_DURATION 5000
#define MAX_MESSAGE_SIZE 10

#define MAX_WAIT_FOR_WIFI 5000

#define TOKEN_LENGTH 32

#define ADDRESS_VERSION         0
#define ADDRESS_IS_CONFIGURED   (ADDRESS_VERSION + 1)
#define ADDRESS_TOKEN_START     (ADDRESS_IS_CONFIGURED + 1)
/*       */
#define ADDRESS_TOKEN_END       (ADDRESS_TOKEN_START + TOKEN_LENGTH - 1)
#define EEPROM_SIZE             ADDRESS_TOKEN_END + 1

#define MAX_SSID_LENGTH 31
#define MAX_PASSWORD_LENGHT 63

#endif
