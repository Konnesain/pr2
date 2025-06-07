#include "io.h"

void writeBigIntToFile(bigint a, std::ofstream &f)
{
    size_t size = (mpz_sizeinbase(a.get_mpz_t(), 2) + 7) / 8;//wrong
    char buf[size];
    mpz_export(buf, &size, 1, 1, 0, 0, a.get_mpz_t());
    f.write(reinterpret_cast<const char*>(&size), sizeof(size));
    f.write(reinterpret_cast<const char*>(&buf), size);
}

bigint readBigIntFromFile(std::ifstream &f)
{
    size_t size;
    if(!f.read(reinterpret_cast<char*>(&size), sizeof(size)))
        throw std::ios_base::failure("Проблема с файлом");
    char buf[size];
    if(!f.read(reinterpret_cast<char*>(&buf), size))
        throw std::ios_base::failure("Проблема с файлом");

    bigint num;
    mpz_import(num.get_mpz_t(), size, 1, 1, 0, 0, buf);
    return num;
}