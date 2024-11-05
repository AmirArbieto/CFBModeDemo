package pe.edu.vallegrande.cfbmodedemo;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Base64;

public class CFBModeGUI extends JFrame {

    private JTextField inputField;
    private JTextArea outputArea;
    private SecretKey secretKey;
    private IvParameterSpec iv;

    public CFBModeGUI() {
        // Configuración básica de la ventana
        setTitle("Cifrado CFB Demo");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Generar clave y IV una sola vez para esta sesión
        try {
            secretKey = generateSecretKey();
            iv = generateIV();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Crear los componentes de la interfaz
        JLabel inputLabel = new JLabel("Ingrese el mensaje:");
        inputField = new JTextField(20);
        JButton encryptButton = new JButton("Cifrar");
        JButton decryptButton = new JButton("Descifrar");
        outputArea = new JTextArea(5, 20);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setEditable(false);

        // Panel para entrada y botones
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(inputLabel);
        panel.add(inputField);
        panel.add(encryptButton);
        panel.add(decryptButton);

        // Añadir componentes a la ventana
        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        // Acciones de los botones
        encryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String plaintext = inputField.getText();
                    String encryptedText = encrypt(plaintext, secretKey, iv);
                    outputArea.setText("Texto Cifrado: " + encryptedText);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        decryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String encryptedText = inputField.getText();
                    String decryptedText = decrypt(encryptedText, secretKey, iv);
                    outputArea.setText("Texto Descifrado: " + decryptedText);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    outputArea.setText("Error en el descifrado.");
                }
            }
        });
    }

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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CFBModeGUI().setVisible(true);
            }
        });
    }
}
