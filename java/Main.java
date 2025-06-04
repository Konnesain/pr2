import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

class Person
{
    BigInteger publicKey;
    BigInteger privateKey;
    BigInteger sendKey;
}

enum InputType
{
    POWER_SLOW,
    POWER_FAST,
    MOD_INVERSE_WITH_D,
    MOD_INVERSE,
    GENERATE_PARAMETERS,
    GENERATE_PUB_KEY,
    ENCRYPT,
    DECRYPT,
    CONTINUED_FRACTION,
    ATTACK,
    EXIT;

    public static InputType fromInteger(int x) {
        return switch (x)
        {
            case 1 -> POWER_SLOW;
            case 2 -> POWER_FAST;
            case 3 -> MOD_INVERSE_WITH_D;
            case 4 -> MOD_INVERSE;
            case 5 -> GENERATE_PARAMETERS;
            case 6 -> GENERATE_PUB_KEY;
            case 7 -> ENCRYPT;
            case 8 -> DECRYPT;
            case 9 -> CONTINUED_FRACTION;
            case 10 -> ATTACK;
            default -> EXIT;
        };
    }
}

public class Main
{
    static void printContinuedFraction(ArrayList<BigInteger> cf)
    {
        System.out.print("[");
        int n = cf.size() - 1;
        for (int i = 0; i <= n; i++)
        {
            System.out.print(cf.get(i));
            if (i < n)
            {
                System.out.print(", ");
            }
        }
        System.out.println("]\n");
    }

    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        while (true)
        {
            System.out.println("1 - a^x mod p через свойства сравнений и теорему Ферма");
            System.out.println("2 - a^x mod p через разложение степени");
            System.out.println("3 - c*d = 1 mod m");
            System.out.println("4 - c^-1 mod m = d");
            System.out.println("5 - генерация параметров для шифрования Эль-Гамаля");
            System.out.println("6 - генерация открытого ключа для шифра Эль-Гамаля");
            System.out.println("7 - шифрование файла шифром Эль-Гамаля");
            System.out.println("8 - расшифрование файла шифром Эль-Гамаля");
            System.out.println("9 - уравнение 1256a+847b=119");
            System.out.println("10 - эмуляция атаки на базе шифрования Эль-Гамаля");
            System.out.println("Иначе - выход");
            InputType inputType = InputType.fromInteger(scanner.nextInt());
            switch (inputType)
            {
                case POWER_SLOW:
                {
                    System.out.println("Введите a");
                    BigInteger a = scanner.nextBigInteger();
                    System.out.println("Введите x");
                    BigInteger x = scanner.nextBigInteger();
                    System.out.println("Введите p");
                    BigInteger p = scanner.nextBigInteger();
                    if (Math.fermaCheck(a, p))
                    {
                        System.out.println("Теорема Ферма выполняется");
                        x = x.mod(p.subtract(BigInteger.ONE));
                        System.out.printf("Новая степень: %d\n",x);
                    }
                    else
                    {
                        System.out.println("Теорема ферма не выполняется");
                    }
                    BigInteger res;
                    try
                    {
                        res = Math.powerMod(a, x, p);
                    }
                    catch(Exception e)
                    {
                        System.out.println(e.getMessage());
                        break;
                    }
                    System.out.printf("%d^%d mod %d = %d\n",a,x,p,res);
                    break;
                }
                case POWER_FAST:
                {
                    System.out.println("Введите a");
                    BigInteger a = scanner.nextBigInteger();
                    System.out.println("Введите x");
                    BigInteger x = scanner.nextBigInteger();
                    System.out.println("Введите p");
                    BigInteger p = scanner.nextBigInteger();
                    BigInteger res;
                    try
                    {
                        res = Math.betterPowerMod(a, x, p);
                    }
                    catch(Exception e)
                    {
                        System.out.println(e.getMessage());
                        break;
                    }
                    System.out.printf("%d^%d mod %d = %d\n",a,x,p,res);
                }
                case MOD_INVERSE_WITH_D:
                {
                    System.out.println("Введите c");
                    BigInteger c = scanner.nextBigInteger();
                    System.out.println("Введите m");
                    BigInteger m = scanner.nextBigInteger();
                    BigInteger d = Math.modInverse(c, m);
                    if (!d.equals(BigInteger.valueOf(-1)))
                        System.out.printf("d = %d\n", d);
                    else
                        System.out.println("d не существует");
                    break;
                }
                case MOD_INVERSE:
                {
                    System.out.println("Введите c");
                    BigInteger c = scanner.nextBigInteger();
                    System.out.println("Введите m");
                    BigInteger m = scanner.nextBigInteger();
                    BigInteger d = Math.modInverse(c, m);
                    if (!d.equals(BigInteger.valueOf(-1)))
                        System.out.printf("%d^-1 по модулю %d = %d\n",c,m,d);
                    else
                        System.out.printf("%d^-1 по модулю %d не существует\n",c,m);
                    break;
                }
                case GENERATE_PARAMETERS:
                {
                    BigInteger[] params = Crypto.generateParameters();
                    System.out.printf("p = %d g = %d\n", params[0], params[1]);
                    break;
                }
                case GENERATE_PUB_KEY:
                {
                    System.out.println("Введите p и g");
                    BigInteger p = scanner.nextBigInteger(), g = scanner.nextBigInteger();
                    System.out.println("Введите закрытый ключ");
                    BigInteger privateKey = scanner.nextBigInteger();
                    BigInteger publicKey;
                    try
                    {
                        publicKey = Crypto.generatePublicKey(privateKey,g,p);
                    }
                    catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                        break;
                    }
                    System.out.printf("Открытый ключ = %d\n", publicKey);
                    break;
                }
                case ENCRYPT:
                {
                    System.out.println("Введите p и g");
                    BigInteger p = scanner.nextBigInteger(), g = scanner.nextBigInteger();
                    System.out.println("Введите файл для шифрования");
                    String filename = scanner.next();
                    System.out.println("Введите открытый ключ для шифрования");
                    BigInteger publicKey = scanner.nextBigInteger();
                    byte[] file_contents;
                    try
                    {
                        file_contents = Files.readAllBytes(Paths.get(filename));
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                    BigInteger[][] encrypted;
                    try
                    {
                        encrypted = Crypto.encrypt(file_contents, publicKey, g, p);
                    }
                    catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                        break;
                    }
                    try(DataOutputStream ofs = new DataOutputStream(new FileOutputStream(filename)))
                    {
                        ofs.writeInt(encrypted.length);
                        for(BigInteger[] s : encrypted)
                        {
                            byte[] bigInt = s[0].toByteArray();
                            ofs.writeInt(bigInt.length);
                            ofs.write(bigInt);
                            bigInt = s[1].toByteArray();
                            ofs.writeInt(bigInt.length);
                            ofs.write(bigInt);
                        }
                    }
                    catch (FileNotFoundException e)
                    {
                        System.out.println("Файл не существует");
                        break;
                    }
                    catch (IOException e)
                    {
                        System.out.println("Проблема с файлом");
                        break;
                    }
                    System.out.println("Зашифровано");
                    break;
                }
                case DECRYPT:
                {
                    System.out.println("Введите p и g");
                    BigInteger p = scanner.nextBigInteger(), g = scanner.nextBigInteger();
                    System.out.println("Введите файл для расшифрования");
                    String filename = scanner.next();
                    System.out.println("Введите закрытый ключ для расшифрования");
                    BigInteger privateKey = scanner.nextBigInteger();
                    BigInteger[][] encrypted;
                    try(DataInputStream ifs = new DataInputStream(new FileInputStream(filename)))
                    {
                        int size = ifs.readInt();
                        encrypted = new BigInteger[size][2];
                        for(int i = 0; i < size; i++)
                        {
                            int bigIntSize = ifs.readInt();
                            encrypted[i][0] = new BigInteger(ifs.readNBytes(bigIntSize));
                            bigIntSize = ifs.readInt();
                            if(bigIntSize < 0)
                                System.out.println(1);
                            encrypted[i][1] = new BigInteger(ifs.readNBytes(bigIntSize));
                        }
                    }
                    catch (FileNotFoundException e)
                    {
                        System.out.println("Файл не существует");
                        break;
                    }
                    catch (IOException e)
                    {
                        System.out.println("Проблема с файлом");
                        break;
                    }

                    PrintWriter ofs;
                    try
                    {
                        ofs = new PrintWriter(filename);
                    }
                    catch (FileNotFoundException e)
                    {
                        System.out.println("Файл не существует");
                        break;
                    }
                    String decrypted;
                    try
                    {
                        decrypted = Crypto.decrypt(encrypted, privateKey, g, p);
                    }
                    catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                        break;
                    }
                    ofs.print(decrypted);
                    ofs.close();
                    System.out.println("Расшифровано");
                    break;
                }
                case CONTINUED_FRACTION:
                {
                    System.out.println("Уравнение 1256*a + 847*b = 119");
                    BigInteger A = BigInteger.valueOf(1256), B = BigInteger.valueOf(847), C = BigInteger.valueOf(119);
                    ArrayList<BigInteger> cf = Math.continuedFraction(A,B);
                    System.out.printf("Цепная дробь %d/%d: ",A,B);
                    printContinuedFraction(cf);
                    ArrayList<BigInteger> P = new ArrayList<>();
                    P.add(BigInteger.ONE);
                    P.add(cf.get(0));
                    ArrayList<BigInteger> Q = new ArrayList<>();
                    Q.add(BigInteger.ZERO);
                    Q.add(BigInteger.ONE);
                    for(int i = 1; i < cf.size(); i++)
                    {
                        P.add(cf.get(i).multiply(P.get(i)).add(P.get(i-1)));
                        Q.add(cf.get(i).multiply(Q.get(i)).add(Q.get(i-1)));
                    }
                    System.out.println("По цепной дроби восстанавливаем подходящие дроби P/Q");
                    System.out.print("P: ");
                    for(int i = 1; i < P.size(); i++)
                        System.out.printf("%d ", P.get(i));
                    System.out.print("\nQ: ");
                    for(int i = 1; i < Q.size(); i++)
                        System.out.printf("%d ", Q.get(i));
                    System.out.println();
                    BigInteger a,b;
                    if(cf.size() % 2 == 0)
                    {
                        a = Q.get(Q.size()-2);
                        b = P.get(P.size()-2).negate();
                        System.out.printf("Так как длина цепной дроби четная: a = %d, b = %d\n",a,b);
                    }
                    else
                    {
                        a = Q.get(Q.size()-2).negate();
                        b = P.get(P.size()-2);
                        System.out.printf("Так как длина цепной дроби нечетная: a = %d, b = %d\n",a,b);
                    }
                    System.out.printf("После домножения на %d получаем решение: a = %d, b = %d\n",C,a.multiply(C),b.multiply(C));
                    break;
                }
                case ATTACK:
                {
                    BigInteger p = BigInteger.valueOf(401);
                    BigInteger g = BigInteger.valueOf(17);
                    System.out.printf("p: %d g: %d\n",p,g);
                    System.out.print("Алиса создает открытый ключ: ");
                    Person alice = new Person();
                    alice.privateKey = BigInteger.valueOf(56);
                    try
                    {
                        alice.publicKey = Crypto.generatePublicKey(alice.privateKey, g, p);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                    System.out.printf("%d и отправляет его Бобу\n", alice.publicKey);

                    System.out.println("Ева перехватывает ключ Алисы и отправляет Бобу свой ключ");
                    Person eve = new Person();
                    eve.privateKey = BigInteger.valueOf(40);
                    try
                    {
                        eve.publicKey = Crypto.generatePublicKey(eve.privateKey, g, p);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                    eve.sendKey = alice.publicKey;

                    System.out.printf("Боб получает открытый ключ: %d\n", eve.publicKey);
                    Person bob = new Person();
                    bob.sendKey = eve.publicKey;

                    System.out.println("Боб шифрует сообщение и отправляет Алисе");
                    String originalMessage = "привет";
                    BigInteger[][] originalEncryptedMessage;
                    try
                    {
                        originalEncryptedMessage = Crypto.encrypt(originalMessage.getBytes(StandardCharsets.UTF_8), bob.sendKey, g, p);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }

                    System.out.print("Ева перехватывает сообщение Боба: ");
                    String originalDecryptedMessage;
                    try
                    {
                        originalDecryptedMessage = Crypto.decrypt(originalEncryptedMessage, eve.privateKey, g, p);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                    System.out.println(originalDecryptedMessage);

                    System.out.println("Ева отправляет Алисе другое сообщение");
                    String newMessage = "пока";
                    BigInteger[][] newEncryptedMessage;
                    try
                    {
                        newEncryptedMessage = Crypto.encrypt(newMessage.getBytes(StandardCharsets.UTF_8), eve.sendKey, g, p);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }

                    System.out.print("Алиса получает сообщение: ");
                    String newDecryptedMessage;
                    try
                    {
                        newDecryptedMessage = Crypto.decrypt(newEncryptedMessage, alice.privateKey, g, p);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                    System.out.println(newDecryptedMessage);
                    break;
                }
                default:
                    return;
            }
        }
    }
}
