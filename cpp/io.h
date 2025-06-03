#pragma once
#include <stdexcept>
#include <fstream>
#include <cstdint>

#include "math.h"

void writeBigIntToFile(bigint a, std::ofstream &f);

bigint readBigIntFromFile(std::ifstream &f);
