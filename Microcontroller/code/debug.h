#ifndef DEBUG_H
#define DEBUG_H

#include "defines.h"

#ifdef DEBUG
    #define PRINT(s) (Serial.print(s))
    #define PRINTLN(s) (Serial.println(s))
#else
    #define PRINT(s) (0)
    #define PRINTLN(s) (0)
#endif

#endif
