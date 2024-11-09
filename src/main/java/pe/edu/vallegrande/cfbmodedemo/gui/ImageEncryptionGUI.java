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
import java.util.Base64;

public class ImageEncryptionGUI {
    private JFrame frame;
    private JButton cargarButton;
    private JButton cifrarButton;
    private JButton descifrarButton;
    private JButton guardarButton;
    private JButton mostrarHexButton;
    private JButton mostrarBase64Button;
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
    private static final String SAVE_PATH = "C:\\Users\\MiguelCuadros\\Pictures\\prueba\\";  // Ruta donde se guardarán las imágenes

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

        // Botón para mostrar en hexadecimal
        mostrarHexButton = new JButton("Mostrar Hexadecimal");
        mostrarHexButton.addActionListener(this::mostrarHexadecimal);
        buttonPanel.add(mostrarHexButton);

        // Botón para mostrar en Base64
        mostrarBase64Button = new JButton("Mostrar Base64");
        mostrarBase64Button.addActionListener(this::mostrarBase64);
        buttonPanel.add(mostrarBase64Button);

        imageLabel = new JLabel();
        frame.getContentPane().add(imageLabel, BorderLayout.CENTER);

        selectedImageFile = null;
        image = null;
        encryptedImage = null;
    }

    private void mostrarHexadecimal(ActionEvent e) {
        BufferedImage img = isEncrypted ? encryptedImage : image;
        if (img == null) {
            JOptionPane.showMessageDialog(frame, "No hay imagen disponible para mostrar en hexadecimal.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String hex = convertImageToHex(img);
        mostrarEnDialogoScrollable("Código Hexadecimal", hex);
    }

    private void mostrarBase64(ActionEvent e) {
        BufferedImage img = isEncrypted ? encryptedImage : image;
        if (img == null) {
            JOptionPane.showMessageDialog(frame, "No hay imagen disponible para mostrar en Base64.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String base64 = convertImageToBase64(img);
        mostrarEnDialogoScrollable("Código Base64", base64);
    }

    private void mostrarEnDialogoScrollable(String titulo, String contenido) {
        // Crear un área de texto con el contenido y hacerla no editable
        JTextArea textArea = new JTextArea(contenido);
        textArea.setEditable(false);

        // Crear un JScrollPane para el JTextArea
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        // Crear un cuadro de diálogo con el JScrollPane
        JOptionPane.showMessageDialog(frame, scrollPane, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    private String convertImageToHex(BufferedImage img) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            byte[] bytes = baos.toByteArray();

            StringBuilder hexBuilder = new StringBuilder();
            int lineLength = 64; // Cambia este valor para ajustar la longitud de cada línea

            for (int i = 0; i < bytes.length; i++) {
                hexBuilder.append(String.format("%02X", bytes[i]));
                if ((i + 1) % (lineLength / 2) == 0) {  // Cada 16 bytes, añade un salto de línea (32 caracteres)
                    hexBuilder.append("\n");
                }
            }
            return hexBuilder.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
            return "Error al convertir la imagen a hexadecimal.";
        }
    }

    private String convertImageToBase64(BufferedImage img) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            byte[] bytes = baos.toByteArray();
            String base64String = Base64.getEncoder().encodeToString(bytes);

            StringBuilder formattedBase64 = new StringBuilder();
            int lineLength = 64; // Cambia este valor para ajustar la longitud de cada línea

            for (int i = 0; i < base64String.length(); i += lineLength) {
                int end = Math.min(i + lineLength, base64String.length());
                formattedBase64.append(base64String, i, end).append("\n");
            }
            return formattedBase64.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
            return "Error al convertir la imagen a Base64.";
        }
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
            // Cifrar la imagen completa en bloques de línea en lugar de píxeles individuales
            encryptedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Cipher cipher = Cipher.getInstance(ALGORITHM + "/" + MODE + "/" + PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

            for (int y = 0; y < image.getHeight(); y++) {
                byte[] lineData = new byte[image.getWidth() * 3];
                for (int x = 0; x < image.getWidth(); x++) {
                    Color color = new Color(image.getRGB(x, y));
                    int offset = x * 3;
                    lineData[offset] = (byte) color.getRed();
                    lineData[offset + 1] = (byte) color.getGreen();
                    lineData[offset + 2] = (byte) color.getBlue();
                }

                // Cifrar la línea de píxeles completa
                byte[] encryptedLine = cipher.update(lineData);

                // Establecer el color cifrado para cada píxel en la línea
                for (int x = 0; x < image.getWidth(); x++) {
                    int offset = x * 3;
                    int red = encryptedLine[offset] & 0xFF;
                    int green = encryptedLine[offset + 1] & 0xFF;
                    int blue = encryptedLine[offset + 2] & 0xFF;
                    encryptedImage.setRGB(x, y, new Color(red, green, blue).getRGB());
                }
            }

            // Mostrar la imagen cifrada
            ImageIcon icon = new ImageIcon(encryptedImage);
            imageLabel.setIcon(icon);
            frame.repaint();

            isEncrypted = true;
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
            BufferedImage decryptedImage = new BufferedImage(encryptedImage.getWidth(), encryptedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            Cipher cipher = Cipher.getInstance(ALGORITHM + "/" + MODE + "/" + PADDING);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

            for (int y = 0; y < encryptedImage.getHeight(); y++) {
                byte[] encryptedLine = new byte[encryptedImage.getWidth() * 3];
                for (int x = 0; x < encryptedImage.getWidth(); x++) {
                    Color color = new Color(encryptedImage.getRGB(x, y));
                    int offset = x * 3;
                    encryptedLine[offset] = (byte) color.getRed();
                    encryptedLine[offset + 1] = (byte) color.getGreen();
                    encryptedLine[offset + 2] = (byte) color.getBlue();
                }

                byte[] decryptedLine = cipher.update(encryptedLine);

                for (int x = 0; x < encryptedImage.getWidth(); x++) {
                    int offset = x * 3;
                    int red = decryptedLine[offset] & 0xFF;
                    int green = decryptedLine[offset + 1] & 0xFF;
                    int blue = decryptedLine[offset + 2] & 0xFF;
                    decryptedImage.setRGB(x, y, new Color(red, green, blue).getRGB());
                }
            }

            ImageIcon icon = new ImageIcon(decryptedImage);
            imageLabel.setIcon(icon);
            frame.repaint();

            isEncrypted = false;
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
