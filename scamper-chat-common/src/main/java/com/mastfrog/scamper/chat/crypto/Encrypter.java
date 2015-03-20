package com.mastfrog.scamper.chat.crypto;

import com.mastfrog.util.Exceptions;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Tim Boudreau
 */
public class Encrypter {

    Cipher enCipher;
    Cipher deCipher;
    private static final int ROUNDS = 13;
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String ALGORITHM = "Blowfish/ECB/PKCS5Padding";

    public Encrypter(String password) {
        try {
            byte[] key = password.getBytes(UTF8);
            int bits = (key.length * 8);
            if (bits > 448) {
                int amt = 448 / 8;
                byte[] nue = new byte[amt];
                System.arraycopy(key, 0, nue, 0, amt);
                int pos = amt;
                while (pos < key.length) {
                    for (int i = 0; i < nue.length && pos < key.length; i++) {
                        nue[i] ^= key[pos++];
                    }
                }
                key = nue;
            }
            SecretKeySpec KS = new SecretKeySpec(key, "Blowfish");
            enCipher = Cipher.getInstance(ALGORITHM);
            enCipher.init(Cipher.ENCRYPT_MODE, KS);
            deCipher = Cipher.getInstance(ALGORITHM);
            deCipher.init(Cipher.DECRYPT_MODE, KS);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException ex) {
            Exceptions.chuck(ex);
        }
    }

    public String encrypt(String cleartext) {
        try {
            byte[] encrypted = cleartext.getBytes(UTF8);
            for (int i = 0; i < ROUNDS; i++) {
                encrypted = enCipher.doFinal(encrypted);
            }
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            return Exceptions.chuck(ex);
        }
    }

    public String decrypt(String encrypted) {
        try {
            byte[] decrypted = Base64.getDecoder().decode(encrypted);
            for (int i = 0; i < ROUNDS; i++) {
                decrypted = deCipher.doFinal(decrypted);
            }
            return new String(decrypted, UTF8);
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            return Exceptions.chuck(ex);
        }
    }
}
