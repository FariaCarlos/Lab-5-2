package securitylabs;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Class to decrypt an encrypted message from an Android APK
 * This class demonstrates the decryption of a Base64 encoded string
 */
public class DecodeAndroidKey {
    // Base64 encoded encrypted string from the APK
    private static final String ENCRYPTED_BASE64 = "5UJiFctbmgbDoLXmpL12mkno8HT4Lv8dlat8FxR2GOc=";
    
    // Hex string representation of the encryption key
    private static final String KEY_HEX = "8d127684cbc37c17616d806cf50473cc";
    
    // Encryption algorithm used
    private static final String ALGORITHM = "AES";

    public static void main(String[] args) {
        try {
            // Decode the Base64 encrypted string to bytes
            byte[] encryptedBytes = Base64.getDecoder().decode(ENCRYPTED_BASE64);
            
            // Convert hex key string to byte array
            byte[] keyBytes = hexStringToByteArray(KEY_HEX);
            
            // Create secret key specification
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            
            // Initialize cipher for decryption using <ALGORITHM>/<MODE>/<PADDING>
            // More information about algorithms names: https://docs.oracle.com/en/java/javase/21/docs/specs/security/standard-names.html 
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            // Perform decryption
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            // Convert decrypted bytes to string
            String decryptedText = new String(decryptedBytes);
            
            // Print the decrypted text
            System.out.println("Decrypted text: " + decryptedText);            
        } catch (Exception e) {
            System.err.println("Decryption failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Converts a hexadecimal string to byte array
     * @param hexString The hexadecimal string to convert
     * @return Byte array representation of the hex string
     */
    private static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            result[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return result;
    }
}