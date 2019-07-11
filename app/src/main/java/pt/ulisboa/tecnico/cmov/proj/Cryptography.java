package pt.ulisboa.tecnico.cmov.proj;

import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

public class Cryptography {

    public static String cipher(PublicKey publicKey, String data) {
        return data;
        /*
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] input = data.getBytes();
            cipher.update(input);

            byte[] cipherText = cipher.doFinal();
            android.util.Log.d("debug", "Ciphered URL is: " + new String(cipherText));

            return new String(cipherText);
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.d("debug", "Could not cipher data");
            return null;
        }
        */
    }

    public static String decipher(PrivateKey privateKey, String cipheredData) {
        return cipheredData;
        /*
        try {
            Cipher decipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            decipher.init(Cipher.DECRYPT_MODE, privateKey);

            String decipheredText = new String(decipher.doFinal(cipheredData.getBytes()), Charset.forName("UTF-8"));

            android.util.Log.d("debug", "Deciphered URL is: " + new String(decipheredText));

            return decipheredText;
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.d("debug", "Could not decipher data");
            return null;
        }
        */
    }
}
