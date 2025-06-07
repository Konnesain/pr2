import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

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

    public static BigInteger generatePublicKey(BigInteger privateKey, BigInteger g, BigInteger p) throws IllegalArgumentException
    {
        if (!Math.isPrime(p))
            throw new IllegalArgumentException("p не простое");
        if(g.compareTo(BigInteger.ONE) <= 0 || g.compareTo(p.subtract(BigInteger.ONE)) >= 0)
            throw new IllegalArgumentException("g не в диапазоне (1; p-1)");
        if (privateKey.compareTo(BigInteger.ONE) <= 0 || privateKey.compareTo(p.subtract(BigInteger.ONE)) >= 0)
            throw new IllegalArgumentException("Ключ не находится на отрезке (1;p-1)");

        return Math.betterPowerMod(g, privateKey, p);
    }

    public static BigInteger[] generateParameters()
    {
        SecureRandom rand = new SecureRandom();
        BigInteger p = BigInteger.probablePrime(256, rand);
        BigInteger g = randomBigInteger(BigInteger.TWO, p, rand);
        return new BigInteger[]{p,g};
    }

    public static BigInteger[][] encrypt(byte[] msg, BigInteger publicKey, BigInteger g, BigInteger p)
    {
        BigInteger[][] encrypted = new BigInteger[msg.length][2];
        SecureRandom rand = new SecureRandom();
        for (int i = 0; i < msg.length; i++)
        {
            BigInteger k = randomBigInteger(BigInteger.ONE,p,rand);
            try
            {
                encrypted[i][0] = Math.betterPowerMod(g, k, p);
                encrypted[i][1] = Math.betterPowerMod(publicKey, k, p).multiply(BigInteger.valueOf(msg[i] & 0xff)).mod(p);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return encrypted;
    }

    public static String decrypt(BigInteger[][] msg, BigInteger privateKey, BigInteger g, BigInteger p)
    {
        byte[] bytes = new byte[msg.length];
        for (int i = 0; i < msg.length; i++)
        {
            try
            {
                bytes[i] = msg[i][1].multiply(Math.modInverse(Math.betterPowerMod(msg[i][0], privateKey, p), p)).mod(p).byteValue();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return new String(bytes);
    }

    public static void encryptFile(DataInputStream ifs, DataOutputStream ofs, BigInteger publicKey, BigInteger g, BigInteger p) throws IllegalArgumentException, IOException
    {
        if (!Math.isPrime(p))
            throw new IllegalArgumentException("p не простое");
        if (publicKey.compareTo(BigInteger.ZERO) <= 0 || publicKey.compareTo(p) >= 0)
            throw new IllegalArgumentException("Ключ не находится на отрезке (0;p)");
        if(g.compareTo(BigInteger.ONE) <= 0 || g.compareTo(p.subtract(BigInteger.ONE)) >= 0)
            throw new IllegalArgumentException("g не в диапазоне (1; p-1)");
        if(p.compareTo(BigInteger.valueOf(255)) <= 0)
            throw new IllegalArgumentException("p слишком маленькое");

        int blockSize = (p.bitLength() + 7) / 8 - 1;
        SecureRandom rand = new SecureRandom();
        byte[] buf = new byte[blockSize];
        while (true)
        {
            int readen = ifs.read(buf, 0, blockSize);
            if(readen == -1)
                break;
            if(readen < blockSize)
            {
                for(int i = readen; i < blockSize; i++)
                    buf[i] = 0;
            }

            BigInteger c = new BigInteger(1, buf);
            BigInteger k = randomBigInteger(BigInteger.ONE,p,rand);
            BigInteger a = Math.betterPowerMod(g,k,p);
            BigInteger b = Math.betterPowerMod(publicKey, k, p).multiply(c).mod(p);
            IO.writeBigIntToFile(a, ofs);
            IO.writeBigIntToFile(b, ofs);
        }
    }

    public static byte[] getMagnitudeBytes(BigInteger bigInteger) {
        byte[] byteArray = bigInteger.toByteArray();

        if (byteArray.length == 0) {
            return byteArray; // Handle zero case
        }

        // If the first byte is zero, it's a leading zero for a positive number, remove it
        if (byteArray[0] == 0) {
            return Arrays.copyOfRange(byteArray, 1, byteArray.length);
        }

        return byteArray; // Return as is if no leading byte or negative
    }

    public static void decryptFile(DataInputStream ifs, DataOutputStream ofs, BigInteger privateKey, BigInteger g, BigInteger p) throws IllegalArgumentException
    {
        if (!Math.isPrime(p))
            throw new IllegalArgumentException("p не простое");
        if (privateKey.compareTo(BigInteger.ONE) <= 0 || privateKey.compareTo(p.subtract(BigInteger.ONE)) >= 0)
            throw new IllegalArgumentException("Ключ не находится на отрезке (1;p-1)");
        if(g.compareTo(BigInteger.ONE) <= 0 || g.compareTo(p.subtract(BigInteger.ONE)) >= 0)
            throw new IllegalArgumentException("g не в диапазоне (1; p-1)");
        if(p.compareTo(BigInteger.valueOf(255)) <= 0)
            throw new IllegalArgumentException("p слишком маленькое");

        int blockSize = (p.bitLength() + 7) / 8 - 1;
        byte[] buf = new byte[blockSize];
        boolean exit = false;
        while(!exit)
        {
            try
            {
                BigInteger a = IO.readBigIntFromFile(ifs);
                BigInteger b = IO.readBigIntFromFile(ifs);
                BigInteger t = b.multiply(Math.modInverse(Math.betterPowerMod(a,privateKey,p),p)).mod(p);
                byte[] bi = getMagnitudeBytes(t);
                if(bi.length < blockSize)
                {
                    byte[] zerobytes = new byte[blockSize-bi.length];
                    Arrays.fill(zerobytes, (byte) 0);
                    ofs.write(zerobytes);
                }
                ofs.write(bi);
            }
            catch(IOException e)
            {
                exit = true;
            }
        }
    }
}