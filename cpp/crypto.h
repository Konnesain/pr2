#pragma once
#include <utility>
#include <string>
#include <vector>
#include <stdexcept>
#include <random>
#include <ctime>

#include <iostream>

#include "math.h"
#include "io.h"

bigint generatePublicKey(bigint privateKey, bigint g, bigint p);

std::pair<bigint, bigint> generateParameters();

std::vector<std::pair<bigint, bigint>> encrypt(std::string msg, bigint publicKey, bigint g, bigint p);

std::string decrypt(std::vector<std::pair<bigint, bigint>> &msg, bigint privateKey, bigint g, bigint p);

void encryptFile(std::ifstream &ifs, std::ofstream &ofs, bigint publicKey, bigint g, bigint p);

void decryptFile(std::ifstream &ifs, std::ofstream &ofs, bigint privateKey, bigint g, bigint p);