package bsc;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Supporter {
static String configFilePath = "C:\\Users\\Public\\config.properties";
Properties config = loadConfig();
String SERVER_ADD = config.getProperty("server.url");
String LOCAL_ADD = config.getProperty("download.path");
static DatabaseService dbService = new DatabaseService();



public static boolean testNetworkPath(String path) {
    try {
        String normalizedPath = path.replace("/", "\\");
        File file = new File(normalizedPath);
        return file.exists();
    } catch (Exception e) {
        return false;
    }
}


public static boolean testServer(String address, int port) {
    try (Socket socket = new Socket()) {
        socket.connect(new InetSocketAddress(address, port), 2000);
        return true;
    } catch (IOException e) {
        return false;
    }
}

  
public static boolean saveConfig(String url, String username, String password, String serverURL, String downloadPath) {
    Properties properties = new Properties();
    File configFile = new File(configFilePath);

    try {
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
            }
        }

        if (url != null && !url.isEmpty()) {
            properties.setProperty("database.url", url);
        }
        if (username != null && !username.isEmpty()) {
            properties.setProperty("database.username", username);
        }
        if (password != null && !password.isEmpty()) {
            properties.setProperty("database.password", password);
        }
        if (serverURL != null && !serverURL.isEmpty()) {
            properties.setProperty("server.url", serverURL);
        }
        if (downloadPath != null && !downloadPath.isEmpty()) {
            properties.setProperty("download.path", downloadPath);
        }

        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            properties.store(fos, "Database and Server Configuration");
        }
        return true;

    } catch (IOException e) {
        return false;
    }
}


public static Properties loadConfig() {
    Properties properties = new Properties();
    File configFile = new File(configFilePath);

    if (!configFile.exists()) {
        return properties;
    }

    try (FileInputStream input = new FileInputStream(configFile)) {
        properties.load(input);
    } catch (IOException e) {
        e.printStackTrace();
    }

    return properties;
}


public static String getConfigValue(String key, String defaultValue) {
    Properties props = loadConfig();
    return props.getProperty(key, defaultValue);
}


public static String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] salt = new byte[16];
    SecureRandom random = new SecureRandom();
    random.nextBytes(salt);
    PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 100000, 256);
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    byte[] hash = factory.generateSecret(spec).getEncoded();
    byte[] combined = new byte[salt.length + hash.length];
    System.arraycopy(salt, 0, combined, 0, salt.length);
    System.arraycopy(hash, 0, combined, salt.length, hash.length);
    return Base64.getEncoder().encodeToString(combined);
}

public static boolean validatePassword(String inputPassword, String storedHash) throws NoSuchAlgorithmException, InvalidKeySpecException {
     byte[] combined = Base64.getDecoder().decode(storedHash);
     byte[] salt = new byte[16];
     System.arraycopy(combined, 0, salt, 0, salt.length);
     PBEKeySpec spec = new PBEKeySpec(inputPassword.toCharArray(), salt, 100000, 256);
     SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
     byte[] inputHash = factory.generateSecret(spec).getEncoded();
     for (int i = 0; i < inputHash.length; i++) {
        if (inputHash[i] != combined[salt.length + i]) {
            return false; 
            }
        }
        return true;
    }


public static void playNotificationSound() {
    try {
        InputStream audioStream = Supporter.class.getResourceAsStream("/audio/notification.wav");
            if (audioStream == null) {
                throw new FileNotFoundException("Audio file not found: /audio/notification.wav");
        }
        BufferedInputStream bufferedInputStream = new BufferedInputStream(audioStream);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);
        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.start();
            while (clip.isRunning()) {
                Thread.sleep(100);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}


public void uploadFiles(String folderPath) throws IOException {
    Path sourceFile = Paths.get(folderPath);
    Path targetDir = Paths.get(SERVER_ADD);
        if (!Files.exists(sourceFile) || !Files.isRegularFile(sourceFile)) {
            throw new IOException("Source file does not exist or is not a regular file: " + folderPath);
        }
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }
        Path targetFile = targetDir.resolve(sourceFile.getFileName());
        Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
    }


public boolean downloadANDopenPDF(String fileName) {
    try {
        Path source = Paths.get(SERVER_ADD, fileName);
        Path target = Paths.get(LOCAL_ADD, fileName);   
        Files.createDirectories(target.getParent());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        File pdfFile = new File(LOCAL_ADD+'/'+fileName);
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(pdfFile);
                    return true;
                } else {
                    System.err.println("Open action is not supported on this system.");
                    return false;
                }
            } else {
                System.err.println("Desktop is not supported on this system.");
                return false;
            }
    } catch (IOException e) {
        System.err.println("Error downloading file: " + e.getMessage());
        return false;
    }
}


public String addDigitalStamp(String fileName, String stampText, String doc_type) {
    try {
        Path source = Paths.get(SERVER_ADD, fileName);
        Path target = Paths.get(LOCAL_ADD, fileName);
        Files.createDirectories(target.getParent());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        File pdfFile = new File(LOCAL_ADD + "/" + fileName);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String dateTime = formatter.format(new Date());
        String FileNameDT = fileName.replace(".pdf", " (" + dateTime + ").pdf");
        File pdfFileOutput = new File(LOCAL_ADD + "/" + FileNameDT);
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(pdfFile), new PdfWriter(pdfFileOutput));
        Document document = new Document(pdfDoc);
        PdfCanvas canvas = new PdfCanvas(pdfDoc.getFirstPage());
        canvas.beginText()
              .setFontAndSize(PdfFontFactory.createFont(), 12) 
              .moveText(50, 750) 
              .showText(stampText)
              .endText();
        InputStream imageStream = getClass().getResourceAsStream("/images/free.png");
            if (imageStream != null) {
                ImageData imageData = ImageDataFactory.create(imageStream.readAllBytes());
                Image image = new Image(imageData);
                image.setFixedPosition(50, 700);
                image.scaleToFit(100, 50); 
                document.add(image);
                imageStream.close(); 
            } else {
                System.err.println("Image resource not found in JAR!");
        }
        document.close();
        uploadFiles(pdfFileOutput.toString());
        saveHashToDatabase(FileNameDT, calculateFileHash(pdfFileOutput.toString()), doc_type);
        return FileNameDT;
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}


public static String calculateFileHash(String filePath) {
    try (FileInputStream fis = new FileInputStream(new File(filePath))) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] byteArray = new byte[1024];
        int bytesRead;
            while ((bytesRead = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesRead);
                }
        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
                }
        return sb.toString();
    } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    

public static boolean saveHashToDatabase(String fileName, String hashValue, String doc_type) {
    String sql = "INSERT INTO hashes (file_type, file_name, file_hash) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE file_hash = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doc_type);
            pstmt.setString(2, fileName);
            pstmt.setString(3, hashValue);
            pstmt.setString(4, hashValue);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
       

public static boolean checkFileIntegrity(String filePath) {
    String currentHash = calculateFileHash(filePath);
    String sql = "SELECT file_hash FROM hashes";
        if (currentHash == null) {
            return false; 
        }
        try (Connection conn = dbService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
             while (rs.next()) {
                String savedHash = rs.getString("file_hash");
                if (Objects.equals(currentHash, savedHash)) {
                    return true; 
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false; 
        }
        return false;
    }

}
