package nz.govt.natlib.dashboard.common.core;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

public class PasswordManager {
    private static final String _AES_KEY = "$5do#@-hub&&(!(984";
    private static SecretKeySpec skey = null;

    static {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(_AES_KEY.getBytes());
            skey = new SecretKeySpec(b, "AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    public static String encrypt(String plainPassword) {
        byte[] iv = new byte[128 / 8];
        SecureRandom srandom = new SecureRandom();
//        srandom.nextBytes(iv);
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        try {
//                KeyGenerator kgen = KeyGenerator.getInstance("AES");
            Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
            ci.init(Cipher.ENCRYPT_MODE, skey, ivspec);

            byte[] encryptedPassword = ci.doFinal(plainPassword.getBytes());
            byte[] base64EncodedPassword = Base64.getEncoder().encode(encryptedPassword);
            return new String(base64EncodedPassword);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String decrypt(String base64EncodedPassword) {
        byte[] encryptedPassword = Base64.getDecoder().decode(base64EncodedPassword);

        byte[] iv = new byte[128 / 8];
        SecureRandom srandom = new SecureRandom();
//        srandom.nextBytes(iv);
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        Cipher ci = null;
        try {
            ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
            ci.init(Cipher.DECRYPT_MODE, skey, ivspec);
            byte[] plainPassword = ci.doFinal(encryptedPassword);
            return new String(plainPassword);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        return null;
    }
}