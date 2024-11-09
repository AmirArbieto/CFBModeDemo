package pe.edu.vallegrande.cfbmodedemo.gui;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;

public class ImageEncryptionGUI {
    private JFrame frame;
    private JButton cargarButton;
    private JButton cifrarButton;
    private JButton descifrarButton;
    private JButton guardarButton;
    private JLabel imageLabel;
    private File selectedImageFile;
    private BufferedImage image;
    private BufferedImage encryptedImage;
    private boolean isEncrypted = false;

    private static final String ALGORITHM = "AES";
    private static final String MODE = "CFB8";  // AES en modo CFB-8
    private static final String PADDING = "NoPadding";
    private SecretKey secretKey;
    private IvParameterSpec ivParameterSpec;
    private int imageCounter = 1;
    private static final String SAVE_PATH = "C:\\Users\\Ascen\\OneDrive\\Documentos\\ShirleyAscencio\\imagenes\\";  // Ruta donde se guardarán las imágenes

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                ImageEncryptionGUI window = new ImageEncryptionGUI();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public ImageEncryptionGUI() {
        try {
            secretKey = generateSecretKey();  // Generar una clave secreta para AES
            ivParameterSpec = generateIv();   // Generar un vector de inicialización para el modo CFB
        } catch (Exception e) {
            e.printStackTrace();
        }
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        frame.getContentPane().add(panel, BorderLayout.NORTH);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Panel para los botones
        JPanel buttonPanel = new JPanel();
        panel.add(buttonPanel);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        cargarButton = new JButton("Cargar Imagen");
        cargarButton.addActionListener(this::loadImage);
        buttonPanel.add(cargarButton);

        cifrarButton = new JButton("Cifrar Imagen");
        cifrarButton.addActionListener(this::encryptImage);
        buttonPanel.add(cifrarButton);

        descifrarButton = new JButton("Descifrar Imagen");
        descifrarButton.addActionListener(this::decryptImage);
        buttonPanel.add(descifrarButton);

        guardarButton = new JButton("Guardar Imagen");
        guardarButton.addActionListener(this::saveImage);
        buttonPanel.add(guardarButton);

        imageLabel = new JLabel();
        frame.getContentPane().add(imageLabel, BorderLayout.CENTER);

        selectedImageFile = null;
        image = null;
        encryptedImage = null;
    }

    private void loadImage(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos de Imagen", "jpg", "jpeg", "png"));
        int result = fileChooser.showOpenDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();

            try {
                image = ImageIO.read(selectedImageFile);
                if (image != null) {
                    ImageIcon icon = new ImageIcon(image);
                    imageLabel.setIcon(icon);
                    frame.repaint();
                    encryptedImage = null;
                    isEncrypted = false;
                } else {
                    JOptionPane.showMessageDialog(frame, "No se pudo cargar la imagen", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error al leer el archivo", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void encryptImage(ActionEvent e) {
        if (image == null) {
            JOptionPane.showMessageDialog(frame, "Por favor, cargue una imagen primero", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (isEncrypted) {
            JOptionPane.showMessageDialog(frame, "La imagen ya está cifrada. Solo se puede descifrar.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Cifrar la imagen
            encryptedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Cipher cipher = Cipher.getInstance(ALGORITHM + "/" + MODE + "/" + PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    Color color = new Color(image.getRGB(x, y));
                    byte[] pixelData = new byte[3];
                    pixelData[0] = (byte) color.getRed();
                    pixelData[1] = (byte) color.getGreen();
                    pixelData[2] = (byte) color.getBlue();

                    // Cifrar los datos del pixel
                    byte[] encryptedPixel = cipher.doFinal(pixelData);

                    // Establecer el color cifrado del pixel
                    int red = encryptedPixel[0] & 0xFF;
                    int green = encryptedPixel[1] & 0xFF;
                    int blue = encryptedPixel[2] & 0xFF;
                    encryptedImage.setRGB(x, y, new Color(red, green, blue).getRGB());
                }
            }

            // Mostrar la imagen cifrada
            ImageIcon icon = new ImageIcon(encryptedImage);
            imageLabel.setIcon(icon);
            frame.repaint();

            // Limpiar la interfaz para mostrar la imagen cifrada
            isEncrypted = true;  // La imagen está cifrada
            JOptionPane.showMessageDialog(frame, "¡Imagen cifrada!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Cifrado fallido", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void decryptImage(ActionEvent e) {
        if (encryptedImage == null) {
            JOptionPane.showMessageDialog(frame, "No hay imagen cifrada para descifrar", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!isEncrypted) {
            JOptionPane.showMessageDialog(frame, "La imagen ya está descifrada. Solo se puede cifrar.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Descifrar la imagen
            BufferedImage decryptedImage = new BufferedImage(encryptedImage.getWidth(), encryptedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            Cipher cipher = Cipher.getInstance(ALGORITHM + "/" + MODE + "/" + PADDING);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

            for (int x = 0; x < encryptedImage.getWidth(); x++) {
                for (int y = 0; y < encryptedImage.getHeight(); y++) {
                    Color color = new Color(encryptedImage.getRGB(x, y));
                    byte[] pixelData = new byte[3];
                    pixelData[0] = (byte) color.getRed();
                    pixelData[1] = (byte) color.getGreen();
                    pixelData[2] = (byte) color.getBlue();

                    // Descifrar los datos del pixel
                    byte[] decryptedPixel = cipher.doFinal(pixelData);

                    // Establecer el color descifrado del pixel
                    int red = decryptedPixel[0] & 0xFF;
                    int green = decryptedPixel[1] & 0xFF;
                    int blue = decryptedPixel[2] & 0xFF;
                    decryptedImage.setRGB(x, y, new Color(red, green, blue).getRGB());
                }
            }

            // Mostrar la imagen descifrada
            ImageIcon icon = new ImageIcon(decryptedImage);
            imageLabel.setIcon(icon);
            frame.repaint();

            // Limpiar la interfaz para mostrar la imagen descifrada
            isEncrypted = false;  // La imagen ahora está descifrada
            JOptionPane.showMessageDialog(frame, "¡Imagen descifrada!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Descifrado fallido", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveImage(ActionEvent e) {
        if (image == null && encryptedImage == null) {
            JOptionPane.showMessageDialog(frame, "Por favor, cargue una imagen primero", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Ruta donde se guardarán las imágenes
            String fileName = (isEncrypted ? "imagen_cifrada_" : "imagen_descifrada_") + imageCounter++ + ".jpg";
            File outputFile = new File(SAVE_PATH + fileName);

            // Verificar si la carpeta existe, si no, crearla
            File folder = new File(SAVE_PATH);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // Guardar la imagen cifrada o descifrada
            BufferedImage imageToSave = (isEncrypted) ? encryptedImage : image;
            ImageIO.write(imageToSave, "jpg", outputFile);
            JOptionPane.showMessageDialog(frame, "Imagen guardada como: " + fileName, "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error al guardar la imagen", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para generar clave secreta (AES)
    private SecretKey generateSecretKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        return keyGen.generateKey();
    }

    // Método para generar IV (Vector de Inicialización)
    private IvParameterSpec generateIv() {
        byte[] iv = new byte[16]; // Tamaño adecuado para AES
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }
}
