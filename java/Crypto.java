import java.math.BigInteger;
import java.security.SecureRandom;

public class Crypto
{
    public static BigInteger randomBigInteger(BigInteger min, BigInteger max, SecureRandom rand)
    {
        BigInteger a;
        do {
            a = new BigInteger(max.bitLength(), rand).add(min);
        } while (a.compareTo(max) >= 0);
        return a;
    }

    public static BigInteger generatePublicKey(BigInteger privateKey, BigInteger g, BigInteger p) throws Exception
    {
        if (!Math.isPrime(p))
            throw new Exception("p не простое");
        if (privateKey.compareTo(BigInteger.ONE) <= 0 || privateKey.compareTo(p.subtract(BigInteger.ONE)) >= 0)
            throw new Exception("Ключ не находится на отрезке (1;p-1)");

        return Math.betterPowerMod(g, privateKey, p);
    }

    public static BigInteger[] generateParameters()
    {
        SecureRandom rand = new SecureRandom();
        BigInteger p = BigInteger.probablePrime(256, rand);
        BigInteger g = randomBigInteger(BigInteger.TWO, p, rand);
        return new BigInteger[]{p,g};
    }

    public static BigInteger[][] encrypt(byte[] msg, BigInteger publicKey, BigInteger g, BigInteger p) throws Exception
    {
        if (!Math.isPrime(p))
            throw new Exception("p не простое");
        if (publicKey.compareTo(BigInteger.ZERO) <= 0 || publicKey.compareTo(p) >= 0)
            throw new Exception("Ключ не находится на отрезке (0;p)");

        BigInteger[][] encrypted = new BigInteger[msg.length][2];
        SecureRandom rand = new SecureRandom();
        for (int i = 0; i < msg.length; i++)
        {
            if (BigInteger.valueOf(msg[i]).compareTo(p) >= 0 || p.compareTo(BigInteger.valueOf(4)) < 0)
                throw new Exception("p слишком маленькое");

            BigInteger k = randomBigInteger(BigInteger.ONE,p,rand);
            encrypted[i][0] = Math.betterPowerMod(g, k, p);
            encrypted[i][1] = Math.betterPowerMod(publicKey, k, p).multiply(BigInteger.valueOf(msg[i] & 0xff)).mod(p);
        }
        return encrypted;
    }

    public static String decrypt(BigInteger[][] msg, BigInteger privateKey, BigInteger g, BigInteger p) throws Exception
    {
        if (!Math.isPrime(p))
            throw new Exception("p не простое");
        if (privateKey.compareTo(BigInteger.ONE) <= 0 || privateKey.compareTo(p.subtract(BigInteger.ONE)) >= 0)
            throw new Exception("Ключ не находится на отрезке (1;p-1)");

        byte[] bytes = new byte[msg.length];
        for (int i = 0; i < msg.length; i++)
        {
            bytes[i] = msg[i][1].multiply(Math.modInverse(Math.betterPowerMod(msg[i][0], privateKey, p), p)).mod(p).byteValue();
        }
        return new String(bytes);
    }
}