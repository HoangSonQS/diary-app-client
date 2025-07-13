package com.mydiary.diaryappclient.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class SecurityUtil {

    private static final String AES_ALGORITHM = "AES";
    private static final String HASH_ALGORITHM = "SHA-256";

    // Tạo key AES 128-bit từ mã PIN
    private static SecretKeySpec createKey(String pin) throws Exception {
        byte[] key = pin.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance(HASH_ALGORITHM);
        key = sha.digest(key);
        byte[] key16Bytes = new byte[16];
        System.arraycopy(key, 0, key16Bytes, 0, key16Bytes.length);
        return new SecretKeySpec(key16Bytes, AES_ALGORITHM);
    }

    // Mã hóa một chuỗi (ví dụ: JWT Token)
    public static String encrypt(String dataToEncrypt, String pin) {
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, createKey(pin));
            return Base64.getEncoder().encodeToString(cipher.doFinal(dataToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.err.println("Lỗi mã hóa: " + e.getMessage());
            return null;
        }
    }

    // Giải mã một chuỗi
    public static String decrypt(String dataToDecrypt, String pin) {
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, createKey(pin));
            return new String(cipher.doFinal(Base64.getDecoder().decode(dataToDecrypt)));
        } catch (Exception e) {
            // Lỗi sai PIN sẽ nhảy vào đây
            System.err.println("Lỗi giải mã (có thể do sai PIN): " + e.getMessage());
            return null;
        }
    }

    // Băm một chuỗi (mã PIN)
    public static String hash(String dataToHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] encodedhash = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}