import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class IO
{
    public static void writeBigIntToFile(BigInteger a, DataOutputStream ofs) throws IOException
    {
        byte[] bi = a.toByteArray();
        ofs.writeInt(bi.length);
        ofs.write(bi);
    }

    public static BigInteger readBigIntFromFile(DataInputStream ifs) throws IOException
    {
        int a = ifs.readInt();
        return new BigInteger(1,ifs.readNBytes(a));
    }
}