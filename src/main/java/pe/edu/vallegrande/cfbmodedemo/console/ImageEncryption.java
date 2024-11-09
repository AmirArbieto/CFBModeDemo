package pe.edu.vallegrande.cfbmodedemo.console;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.nio.file.Files;
import java.util.Base64;

public class ImageEncryption {

    public static SecretKey generateSecretKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // 128 bits
        return keyGen.generateKey();
    }

    public static IvParameterSpec generateIV() {
        byte[] iv = new byte[16]; // AES utiliza un tamaño de IV de 16 bytes
        new java.security.SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static byte[] encryptImage(byte[] imageBytes, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        return cipher.doFinal(imageBytes);
    }

    public static byte[] decryptImage(byte[] encryptedImageBytes, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        return cipher.doFinal(encryptedImageBytes);
    }

    public static void main(String[] args) {
        try {
            // Generar clave y IV
            SecretKey key = generateSecretKey();
            IvParameterSpec iv = generateIV();

            // Leer la imagen en bytes
            File imageFile = new File("path_to_your_image.jpg");
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());

            // Cifrar la imagen
            byte[] encryptedImageBytes = encryptImage(imageBytes, key, iv);
            System.out.println("Imagen cifrada: " + Base64.getEncoder().encodeToString(encryptedImageBytes));

            // Guardar la imagen cifrada en un archivo
            File encryptedImageFile = new File("encrypted_image.dat");
            try (FileOutputStream fos = new FileOutputStream(encryptedImageFile)) {
                fos.write(encryptedImageBytes);
            }

            // Leer la imagen cifrada
            byte[] encryptedImageBytesFromFile = Files.readAllBytes(encryptedImageFile.toPath());

            // Descifrar la imagen
            byte[] decryptedImageBytes = decryptImage(encryptedImageBytesFromFile, key, iv);

            // Guardar la imagen descifrada
            File decryptedImageFile = new File("decrypted_image.jpg");
            try (FileOutputStream fos = new FileOutputStream(decryptedImageFile)) {
                fos.write(decryptedImageBytes);
            }

            System.out.println("Imagen descifrada y guardada como decrypted_image.jpg");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
