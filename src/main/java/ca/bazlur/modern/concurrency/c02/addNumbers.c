#include <unistd.h>

// Function definition
int addNumbers(int number1, int number2) {
    // Pause execution for 200,000 microseconds (200 milliseconds)
    usleep(200000);
  
    return number1 + number2;
}
