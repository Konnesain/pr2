#include "crypto.h"

bigint generatePublicKey(bigint privateKey, bigint g, bigint p)
{
	if (!isPrime(p))
		throw std::invalid_argument("p не простое");
	if (1 >= privateKey || privateKey >= p - 1)
		throw std::invalid_argument("ключ не в диапазоне (1;p-1)");

	return betterPowerMod(g, privateKey, p);
}

std::pair<bigint, bigint> generateParameters()
{
	bigint p;
	gmp_randclass rng(gmp_randinit_default);
    rng.seed(time(0));

	do
	{
		p = rng.get_z_bits(256);
	} while(!isPrime(p));

	bigint g = rng.get_z_range(p-2) + 2;
	return {p, g};
}

std::vector<std::pair<bigint, bigint>> encrypt(std::string msg, bigint publicKey, bigint g, bigint p)
{
	if (!isPrime(p))
		throw std::invalid_argument("p не простое");
	if (0 >= publicKey || publicKey >= p)
		throw std::invalid_argument("ключ не в диапазоне (0;p)");

	gmp_randclass rng(gmp_randinit_default);
    rng.seed(time(0));
	
	std::vector<std::pair<bigint, bigint>> encrypted;
	for (uint8_t c : msg)
	{
		if (c >= p || p < 4)
			throw std::invalid_argument("p слишком маленькое");

		bigint k = rng.get_z_range(p) + 1;
		bigint a = betterPowerMod(g, k, p);
		bigint b = (betterPowerMod(publicKey, k, p) * c) % p;
		encrypted.push_back({ a,b });
	}
	return encrypted;
}

std::string decrypt(std::vector<std::pair<bigint, bigint>> &msg, bigint privateKey, bigint g, bigint p)
{
	if (!isPrime(p))
		throw std::invalid_argument("p не простое");
	if (1 >= privateKey || privateKey >= p - 1)
		throw std::invalid_argument("ключ не в диапазоне (1;p-1)");

	std::string decrypted = "";
	for (auto c : msg)
	{
		bigint t = ((c.second * modInverse(betterPowerMod(c.first, privateKey, p), p)) % p);
		decrypted += static_cast<char>(t.get_si());
	}
	return decrypted;
}

void encryptFile(std::ifstream &ifs, std::ofstream &ofs, bigint publicKey, bigint g, bigint p)
{
	if (!isPrime(p))
		throw std::invalid_argument("p не простое");
	if (0 >= publicKey || publicKey >= p)
		throw std::invalid_argument("ключ не в диапазоне (0;p)");

	gmp_randclass rng(gmp_randinit_default);
    rng.seed(time(0));
	if(p <= 255)
		throw std::invalid_argument("p слишком маленькое");
	
	size_t size = (mpz_sizeinbase(p.get_mpz_t(), 2) + 7) / 8-1; 
	char *buf = new char[size];
	while (1)
	{
		ifs.read(reinterpret_cast<char*>(buf), size);
		auto count = ifs.gcount();
		if(count == 0)
			break;
		if(count < size)
		{
			for(size_t i = count; i < size; i++)
				buf[i] = 0;
		}
		
		bigint c;
		mpz_import(c.get_mpz_t(), size, 1, 1, 1, 0, buf);
		bigint a;
		bigint b;
		if(c != 0)
		{
			bigint k = rng.get_z_range(p) + 1;
			a = betterPowerMod(g, k, p);
			b = (betterPowerMod(publicKey, k, p) * c) % p;
		}
		else
		{
			a = 1;
			b = 0;
		}
		std::cout << "i " << c << " a " << a << " b " << b << "\n";
		writeBigIntToFile(a, ofs);
		writeBigIntToFile(b, ofs);
	}
	delete[] buf;
}

void decryptFile(std::ifstream &ifs, std::ofstream &ofs, bigint privateKey, bigint g, bigint p)
{
	if (!isPrime(p))
		throw std::invalid_argument("p не простое");
	if (1 >= privateKey || privateKey >= p - 1)
		throw std::invalid_argument("ключ не в диапазоне (1;p-1)");
	if(p <= 255)
		throw std::invalid_argument("p слишком маленькое");
	
	size_t size = (mpz_sizeinbase(p.get_mpz_t(), 2) + 7) / 8-1; 

	char *buf = new char[size];
	bool exit = false;
	while(1)
	{
		try
		{
			bigint a = readBigIntFromFile(ifs);
			bigint b = readBigIntFromFile(ifs);
			bigint t = ((b * modInverse(betterPowerMod(a, privateKey, p), p)) % p);
			std::cout << "d " << t << " a " << a << " b " << b << "\n";
			size_t g;
    		mpz_export(buf, &g, 1, 1, 1, 0, t.get_mpz_t());
			if(g < size)
			{
				size_t zerosize = size-g;
				char zerobytes[zerosize];
				for(int i = 0; i < zerosize; i++)
				{
					zerobytes[i] = 0;
				}
				ofs.write(reinterpret_cast<char*>(&zerobytes), zerosize);
			}
			ofs.write(buf, g);
		}
		catch(std::ios_base::failure e)
		{
			exit = true;
		}
		if(exit)
		{
			break;
		}
	}
	delete[] buf;
}