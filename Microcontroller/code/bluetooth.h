#ifndef BLUETOOTH_H
#define BLUETOOTH_H

 

#include <BluetoothSerial.h>

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

#define BT_MAX_CONNECTION_DURATION INT_MAX
#define BT_MAX_MESSAGE_SIZE 50
#define BT_WAIT_BETWEEN_WRITES 20

//  BT stuff
bool readLine(BluetoothSerial &SerialBT, char* message, int maxSize, int* size  = nullptr);
bool writeLine(BluetoothSerial &SerialBT, const char* message);
bool giveTokn(BluetoothSerial &SerialBT, char* token);
bool giveString(BluetoothSerial &SerialBT, const char* request, char* receive, size_t size);

#endif
