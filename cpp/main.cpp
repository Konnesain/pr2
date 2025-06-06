#include <iostream>
#include <fstream>
#include "crypto.h"
#include "math.h"
#include "io.h"
#ifdef _WIN32
#include <Windows.h>
#endif
using namespace std;

ostream& operator<<(ostream& os, const vector<bigint>& cf)
{
    os << "[";
    auto n = cf.size() - 1;
    for (auto i = 0; i <= n; i++)
    {
        os << cf[i];
        if (i < n)
        {
            os << ", ";
        }
    }
    os << "]\n";
    return os;
}


struct Person
{
    bigint publicKey;
    bigint privateKey;
    bigint sendKey;
};

enum class InputType
{
    POWER_SLOW = 1,
    POWER_FAST,
    MOD_INVERSE_WITH_D,
    MOD_INVERSE,
    GENERATE_PARAMETERS,
    GENERATE_PUB_KEY,
    ENCRYPT,
    DECRYPT,
    CONTINUED_FRACTION,
    ATTACK
};

int main()
{
    setlocale(LC_ALL, "");
	#ifdef _WIN32
    SetConsoleCP(1251);
    SetConsoleOutputCP(1251);
    #endif
    while (1)
    {
        cout << "1 - a^x mod p через свойства сравнений и теорему Ферма\n";
        cout << "2 - a^x mod p через разложение степени\n";
        cout << "3 - c*d = 1 mod m\n";
        cout << "4 - c^-1 mod m = d\n";
        cout << "5 - генерация параметров для шифрования Эль-Гамаля\n";
        cout << "6 - генерация открытого ключа для шифра Эль-Гамаля\n";
        cout << "7 - шифрование файла шифром Эль-Гамаля\n";
        cout << "8 - расшифрование файла шифром Эль-Гамаля\n";
        cout << "9 - уравнение 1256a+847b=119\n";
        cout << "10 - эмуляция атаки на базе шифрования Эль-Гамаля\n";
        cout << "Иначе - выход\n";
        int inputCode;
        cin >> inputCode;
        InputType type = static_cast<InputType>(inputCode);
        switch (type)
        {
        case InputType::POWER_SLOW:
        {
            cout << "Введите a\n";
            bigint a;
            cin >> a;
            cout << "Введите x\n";
            bigint x;
            cin >> x;
            cout << "Введите p\n";
            bigint p;
            cin >> p;
            if (fermaCheck(a, p))
            {
                cout << "Теорема Ферма выполняется\n";
                x = x % (p - 1);
                cout << "Новая степень: " << x << "\n";
            }
            else
            {
                cout << "Теорема ферма не выполняется\n";
            }
            bigint res;
            try
            {
                res = powerMod(a, x, p);
            }
            catch(invalid_argument e)
            {
                cout << e.what() << "\n";
                break;
            }
            cout << a << "^" << x << " mod " << p << " = " << res << "\n";
            break;
        }
        case InputType::POWER_FAST:
        {
            cout << "Введите a\n";
            bigint a;
            cin >> a;
            cout << "Введите x\n";
            bigint x;
            cin >> x;
            cout << "Введите p\n";
            bigint p;
            cin >> p;
            bigint res;
            try
            {
                res = betterPowerMod(a, x, p);
            }
            catch(invalid_argument e)
            {
                cout << e.what() << "\n";
                break;
            }
            cout << a << "^" << x << " mod " << p << " = " << res << "\n";
            break;
        }
        case InputType::MOD_INVERSE_WITH_D:
        {
            cout << "Введите c\n";
            bigint c;
            cin >> c;
            cout << "Введите m\n";
            bigint m;
            cin >> m;
            bigint d = modInverse(c, m);
            if (d != -1)
                cout << "d = " << d << "\n";
            else
                cout << "d не существует\n";
            break;
        }
        case InputType::MOD_INVERSE:
        {
            cout << "Введите c\n";
            bigint c;
            cin >> c;
            cout << "Введите m\n";
            bigint m;
            cin >> m;
            bigint d = modInverse(c, m);
            if (d != -1)
                cout << c << "^-1 по модулю " << m << " = " << d << "\n";
            else
                cout << c << "^-1 по модулю " << m << " не существует\n";
            break;
        }
        case InputType::GENERATE_PARAMETERS:
        {
            pair<bigint,bigint> params = generateParameters();
            cout << "p = " << params.first << " g = " << params.second << "\n";
            break;
        }
        case InputType::GENERATE_PUB_KEY:
        {
            cout << "Введите p и g\n";
            bigint p, g;
            cin >> p >> g;
            cout << "Введите закрытый ключ\n";
            bigint privateKey;
            cin >> privateKey;
            bigint publicKey;
            try
            {
                publicKey = generatePublicKey(privateKey, g, p);
            }
            catch(invalid_argument e)
            {
                cout << e.what() << "\n";
                break;
            }
            cout << "Открытый ключ = " << publicKey << "\n";
            break;
        }
        case InputType::ENCRYPT:
        {
            cout << "Введите p и g\n";
            bigint p, g;
            cin >> p >> g;
            cout << "Введите файл для шифрования\n";
            string filename;
            cin >> filename;
            cout << "Введите файл зашифрованного текста\n";
            string encFilename;
            cin >> encFilename;
            cout << "Введите открытый ключ для шифрования\n";
            bigint publicKey;
            cin >> publicKey;
            ifstream ifs(filename, ifstream::binary);
            if (!ifs.is_open())
            {
                cout << "Файл для шифрования не существует\n";
                break;
            }
            ofstream ofs(encFilename, ofstream::trunc | ofstream::binary);
            if (!ofs.is_open())
            {
                cout << "Файл для зашифрованного текста не существует\n";
                break;
            }
            try
            {
                encryptFile(ifs, ofs, publicKey, g, p);
            }
            catch (invalid_argument e)
            {
                cout << e.what() << "\n";
                break;
            }
            ifs.close();
            ofs.close();
            cout << "Зашифровано\n";
            break;
        }
        case InputType::DECRYPT:
        {
            cout << "Введите p и g\n";
            bigint p, g;
            cin >> p >> g;
            cout << "Введите файл для расфрования\n";
            string filename;
            cin >> filename;
            cout << "Введите файл для расшифрованного текста\n";
            string decFilename;
            cin >> decFilename;
            cout << "Введите закрытый ключ для расшифрования\n";
            bigint privateKey;
            cin >> privateKey;
            ifstream ifs(filename, ifstream::binary);
            if (!ifs.is_open())
            {
                cout << "Файл для расшифровнаия существует\n";
                break;
            }
            ofstream ofs(decFilename, ofstream::trunc | ofstream::binary);
            if (!ofs.is_open())
            {
                cout << "Файл для расшифрованного текста не существует\n";
                break;
            }
            try
            {
                decryptFile(ifs,ofs, privateKey, g, p);
            }
            catch (invalid_argument e)
            {
                cout << e.what() << "\n";
                break;
            }
            ifs.close();
            ofs.close();
            cout << "Расшифровано\n";
            break;
        }
        case InputType::CONTINUED_FRACTION:
        {            
            cout << "Уравнение 1256*a + 847*b = 119\n";
            bigint A = 1256, B = 847, C = 119;
            auto cf = continuedFraction(A,B);
            cout << "Цепная дробь " << A << "/" << B << ": " << cf;
            vector<bigint> P{1, cf[0]};
            vector<bigint> Q{0, 1};
            for(int i = 1; i < cf.size(); i++)
            {
                P.push_back(cf[i]*P[i] + P[i-1]);
                Q.push_back(cf[i]*Q[i] + Q[i-1]);
            }
            cout << "По цепной дроби восстанавливаем подходящие дроби P/Q\n";
            cout << "P: ";
            for(int i = 1; i < P.size(); i++)
                cout << P[i] << " ";
            cout << "\nQ: ";
            for(int i = 1; i < Q.size(); i++)
                cout << Q[i] << " ";
            cout << "\n";
            bigint a,b;
            if(cf.size() % 2 == 0)
            {
                a = Q[Q.size()-2];
                b = -P[P.size()-2];
                cout << "Так как длина цепной дроби четная: a = " << a << ", b = " << b << "\n";
            }
            else
            {
                a = -Q[Q.size()-2];
                b = P[P.size()-2];
                cout << "Так как длина цепной дроби нечетная: a = " << a << ", b = " << b << "\n";
            }
            cout << "После домножения на " << C << " получаем решение: a = " << a*C << ", b = " << b*C << "\n";
            break;
        }
        case InputType::ATTACK:
        {
            bigint p = 401;
            bigint g = 17;
            cout << "p: " << p << " g: " << g << "\n";
            cout << "Алиса создает открытый ключ: ";
            Person alice;
            alice.privateKey = 56;
            alice.publicKey = generatePublicKey(alice.privateKey, g, p);
            cout << alice.publicKey << " и отправляет его Бобу\n";

            cout << "Ева перехватывает ключ Алисы и отправляет Бобу свой ключ\n";
            Person eve;
            eve.privateKey = 40;
            eve.publicKey = generatePublicKey(eve.privateKey, g, p);
            eve.sendKey = alice.publicKey;

            cout << "Боб получает открытый ключ: " << eve.publicKey << "\n";
            Person bob;
            bob.sendKey = eve.publicKey;

            cout << "Боб шифрует сообщение и отправляет Алисе\n";
            string originalMessage = "привет";
            auto originalEncryptedMessage = encrypt(originalMessage, bob.sendKey, g, p);

            cout << "Ева перехватывает сообщение Боба: ";
            string originalDecryptedMessage = decrypt(originalEncryptedMessage, eve.privateKey, g, p);
            cout << originalDecryptedMessage << "\n";
            
            cout << "Ева отправляет Алисе другое сообщение\n";
            string newMessage = "пока";
            auto newEncryptedMessage = encrypt(newMessage, eve.sendKey, g, p);
            
            cout << "Алиса получает сообщение: ";
            string newDecryptedMessage = decrypt(newEncryptedMessage, alice.privateKey, g, p);
            cout << newDecryptedMessage << "\n";
            break;
        }
        default:
            return 0;
        }
    }
}