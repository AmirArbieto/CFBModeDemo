package pe.edu.vallegrande.cfbmodedemo.console;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;

public class CFBModeConsoleDemo {

    // Método para generar una clave secreta (AES)
    public static SecretKey generateSecretKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // Puedes usar 128, 192 o 256 bits si tu política de seguridad lo permite
        return keyGen.generateKey();
    }

    // Método para generar un IV (Vector de Inicialización)
    public static IvParameterSpec generateIV() {
        byte[] iv = new byte[16]; // Tamaño para AES
        new java.security.SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    // Método para cifrar el texto
    public static String encrypt(String plaintext, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // Método para descifrar el texto
    public static String decrypt(String ciphertext, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
        return new String(decrypted);
    }

    public static void main(String[] args) {
        try {
            // Generar clave y IV
            SecretKey key = generateSecretKey();
            IvParameterSpec iv = generateIV();

            // Texto a cifrar
            String plaintext = "Este es un mensaje secreto en modo CFB";

            // Cifrado
            String encryptedText = encrypt(plaintext, key, iv);
            System.out.println("Texto Cifrado: " + encryptedText);

            // Descifrado
            String decryptedText = decrypt(encryptedText, key, iv);
            System.out.println("Texto Descifrado: " + decryptedText);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
