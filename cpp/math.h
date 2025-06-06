#pragma once
#include <vector>
#include <stdexcept>
#include <gmpxx.h>
#include <climits>
#include <cmath>

typedef mpz_class bigint;

bool isPrime(bigint a);

bool fermaCheck(bigint a, bigint p);

std::vector<bigint> continuedFraction(bigint numerator, bigint denominator);

bigint powerMod(bigint a, bigint x, bigint p);

bigint betterPowerMod(bigint a, bigint x, bigint p);

bigint extendedGCD(bigint a, bigint b, bigint& u, bigint& v);

bigint modInverse(bigint a, bigint m);