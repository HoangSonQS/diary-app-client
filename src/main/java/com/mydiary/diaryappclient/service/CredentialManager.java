package com.mydiary.diaryappclient.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class CredentialManager {
    // Vẫn giữ nguyên đường dẫn
    private static final Path CONFIG_DIR = Paths.get(System.getProperty("user.home"), ".ultimate-diary");
    private static final Path CREDENTIALS_FILE = CONFIG_DIR.resolve("user.properties"); // Đổi tên file cho rõ nghĩa

    /**
     * Lưu lại username của người dùng cuối cùng
     * @param username Tên đăng nhập để lưu
     */
    public static void saveLastUser(String username) throws IOException {
        if (!Files.exists(CONFIG_DIR)) {
            Files.createDirectories(CONFIG_DIR);
        }
        Properties props = new Properties();
        props.setProperty("last_user", username);

        try (OutputStream output = new FileOutputStream(CREDENTIALS_FILE.toFile())) {
            props.store(output, "Ultimate Diary Last User");
        }
    }

    /**
     * Tải username của người dùng cuối cùng
     * @return Trả về username, hoặc null nếu không có file
     */
    public static String loadLastUser() {
        if (!Files.exists(CREDENTIALS_FILE)) {
            return null;
        }
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(CREDENTIALS_FILE.toFile())) {
            props.load(input);
            return props.getProperty("last_user");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Xóa thông tin người dùng đã lưu (dùng khi đăng xuất)
     */
    public static void deleteLastUser() throws IOException {
        if(Files.exists(CREDENTIALS_FILE)) {
            Files.delete(CREDENTIALS_FILE);
        }
    }
}