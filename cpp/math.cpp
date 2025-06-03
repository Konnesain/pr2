#include "math.h"

bool isPrime(mpz_class a)
{
    if (a == 2)
        return true;
    if (a <= 1 || a % 2 == 0)
        return false;
    if(a > INT_MAX)
        return mpz_probab_prime_p(a.get_mpz_t(), 25);
    int root = sqrt(a.get_si());
    int b = a.get_si();
    for (int i = 3; i <= root; i += 2)
    {
        if (b % i == 0)
            return false;
    }
    return true;
}

bool fermaCheck(bigint a, bigint p)
{
    return isPrime(p) && (a % p != 0);
}

std::vector<bigint> continuedFraction(bigint numerator, bigint denominator)
{
    if (denominator == 0)
        return { 0 };
    std::vector<bigint> cf;
    while (denominator != 0)
    {
        bigint q = numerator / denominator;
        bigint r = numerator % denominator;
        cf.push_back(q);
        numerator = denominator;
        denominator = r;
    }
    return cf;
}

bigint powerMod(bigint a, bigint x, bigint p)
{
    if (p <= 0)
        throw std::invalid_argument("p не положительное");

    bigint res = 1;
    for (bigint i = 0; i < x; i++)
    {
        res = (res * a) % p;
    }
    return res;
}

bigint betterPowerMod(bigint a, bigint x, bigint p)
{
    if (p <= 0)
        throw std::invalid_argument("p не положительное");

    std::vector<bigint> xi;
    std::vector<bigint> idk;//2^i
    xi.push_back(a % p);
    idk.push_back(1);
    int index = 0;
    for (bigint i = 2; i <= x; i *= 2, index++)
    {
        xi.push_back((xi[index] * xi[index]) % p);
        idk.push_back(i);
    }
    bigint res = 1;
    for (auto i = idk.size(); i-- > 0;)//starts at size-1
    {
        if (idk[i] > x)
            continue;
        res = (res * xi[i]) % p;
        x -= idk[i];
    }
    return res;
}

bigint extendedGCD(bigint a, bigint b, bigint& u, bigint& v)
{
    u = 1; v = 0;
    bigint u1 = 0, v1 = 1;
    while (b != 0)
    {
        bigint q = a / b;
        bigint r = a % b;
        bigint tu = u1, tv = v1;
        u1 = u - q * u1;
        v1 = v - q * v1;
        u = tu;
        v = tv;
        a = b;
        b = r;
    }

    return a;
}

bigint modInverse(bigint a, bigint m)
{
    bigint u, v;
    bigint g = extendedGCD(a, m, u, v);

    if (g != 1)
        return -1;
    else
        return (u % m + m) % m;
}