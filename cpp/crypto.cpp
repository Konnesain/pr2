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