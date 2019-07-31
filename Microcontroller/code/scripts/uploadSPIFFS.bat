mkspiffs -c ../data -b 4096 -p 256 -s 0x2F000 ./spiffs.bin
esptool --chip esp32 --port COM4 --baud 512000 write_flash -z 0x3D1000 ./spiffs.bin