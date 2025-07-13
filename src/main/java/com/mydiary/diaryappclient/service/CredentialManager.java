package com.mydiary.diaryappclient.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class CredentialManager {
    private static final Path CONFIG_DIR = Paths.get(System.getProperty("user.home"), ".ultimate-diary");
    private static final Path CREDENTIALS_FILE = CONFIG_DIR.resolve("credentials.properties");

    // Record để chứa dữ liệu đọc được từ file
    public record StoredCredentials(String encryptedToken, String pinHash) {}

    // Lưu trữ thông tin
    public static void saveCredentials(String encryptedToken, String pinHash) throws IOException {
        if (!Files.exists(CONFIG_DIR)) {
            Files.createDirectories(CONFIG_DIR);
        }
        Properties props = new Properties();
        props.setProperty("token", encryptedToken);
        props.setProperty("pin_hash", pinHash);

        try (OutputStream output = new FileOutputStream(CREDENTIALS_FILE.toFile())) {
            props.store(output, "Ultimate Diary Credentials - DO NOT EDIT");
        }
    }

    // Tải thông tin
    public static StoredCredentials loadCredentials() {
        if (!Files.exists(CREDENTIALS_FILE)) {
            return null;
        }
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(CREDENTIALS_FILE.toFile())) {
            props.load(input);
            String token = props.getProperty("token");
            String pinHash = props.getProperty("pin_hash");
            return new StoredCredentials(token, pinHash);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Xóa thông tin (dùng khi đăng xuất)
    public static void deleteCredentials() throws IOException {
        if(Files.exists(CREDENTIALS_FILE)) {
            Files.delete(CREDENTIALS_FILE);
        }
    }
}