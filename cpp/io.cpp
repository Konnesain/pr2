#include "io.h"

void writeBigIntToFile(bigint a, std::ofstream &f)
{
    size_t num_bytes = (mpz_sizeinbase(a.get_mpz_t(), 2) + 7) / 8;
    f.write(reinterpret_cast<const char*>(&num_bytes), sizeof(num_bytes));
    int8_t buf[num_bytes];

    mpz_export(buf, nullptr, 1, 1, 0, 0, a.get_mpz_t());
    f.write(reinterpret_cast<const char*>(buf), num_bytes);
}

bigint readBigIntFromFile(std::ifstream &f)
{
    size_t num_bytes;
    if(!f.read(reinterpret_cast<char*>(&num_bytes), sizeof(num_bytes)))
        throw std::ios_base::failure("Проблема с файлом");
    int8_t buf[num_bytes];
    if(!f.read(reinterpret_cast<char*>(buf), num_bytes))
        throw std::ios_base::failure("Проблема с файлом");

    mpz_class num;
    mpz_import(num.get_mpz_t(), num_bytes, 1, 1, 0, 0, buf);
    return num;
}