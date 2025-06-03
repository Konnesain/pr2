import java.math.BigInteger;
import java.util.ArrayList;
import static java.lang.Math.sqrt;

public class Math
{
    public static boolean isPrime(BigInteger a)
    {
        if (a.equals(BigInteger.TWO))
            return true;
        if (a.compareTo(BigInteger.ONE) <= 0 || a.mod(BigInteger.TWO).equals(BigInteger.ZERO))
            return false;
        if(a.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0)
            return a.isProbablePrime(25);
        double root = sqrt(a.intValue());
        int b = a.intValue();
        for (int i = 3; i <= root; i += 2)
        {
            if (b % i == 0)
                return false;
        }
        return true;
    }

    public static boolean fermaCheck(BigInteger a, BigInteger p)
    {
        return (!a.mod(p).equals(BigInteger.valueOf(0))) && isPrime(p);
    }

    public static BigInteger powerMod(BigInteger a, BigInteger x, BigInteger p) throws Exception
    {
        if (p.compareTo(BigInteger.valueOf(0)) <= 0)
            throw new Exception("Модуль не положительный");

        BigInteger res = BigInteger.ONE;
        for (BigInteger i = BigInteger.ZERO; i.compareTo(x) < 0; i = i.add(BigInteger.ONE))
        {
            res = res.multiply(a).mod(p);
        }
        return res;
    }

    public static BigInteger betterPowerMod(BigInteger a, BigInteger x, BigInteger p) throws Exception
    {
        if (p.compareTo(BigInteger.ZERO) <= 0)
            throw new Exception("Модуль не положительный");

        ArrayList<BigInteger> xi = new ArrayList<>();
        ArrayList<BigInteger> idk = new ArrayList<>(); //2^i
        xi.add(a.mod(p));
        idk.add(BigInteger.ONE);
        int index = 0;
        for (BigInteger i = BigInteger.TWO; i.compareTo(x) <= 0; i = i.multiply(BigInteger.TWO), index++)
        {
            xi.add(xi.get(index).multiply(xi.get(index)).mod(p));
            idk.add(i);
        }
        BigInteger res = BigInteger.ONE;
        for (int i = idk.size() - 1; i >= 0; i--)
        {
            if (idk.get(i).compareTo(x) > 0)
                continue;
            res = res.multiply(xi.get(i)).mod(p);
            x = x.subtract(idk.get(i));
        }
        return res;
    }

    public static BigInteger[] extendedGCD(BigInteger a, BigInteger b)
    {
        BigInteger u = BigInteger.ONE, v = BigInteger.ZERO;
        BigInteger u1 = BigInteger.ZERO, v1 = BigInteger.ONE;
        while (!b.equals(BigInteger.ZERO))
        {
            BigInteger q = a.divide(b);
            BigInteger r = a.mod(b);
            BigInteger tu = u1, tv = v1;
            u1 = u.subtract(q.multiply(u1));
            v1 = v.subtract(q.multiply(v1));
            u = tu;
            v = tv;
            a = b;
            b = r;
        }

        return new BigInteger[]{a, u, v};
    }

    public static BigInteger modInverse(BigInteger a, BigInteger m)
    {
        BigInteger[] g = extendedGCD(a, m);
        if (!g[0].equals(BigInteger.ONE))
        {
            return BigInteger.valueOf(-1);
        }
        else
        {
            return g[1].mod(m).add(m).mod(m);
        }
    }

    public static ArrayList<BigInteger> continuedFraction(BigInteger a, BigInteger b)
    {
        ArrayList<BigInteger> cf = new ArrayList<>();
        while (!b.equals(BigInteger.ZERO))
        {
            BigInteger q = a.divide(b);
            BigInteger r = a.mod(b);
            cf.add(q);
            a = b;
            b = r;
        }
        return cf;
    }
}
