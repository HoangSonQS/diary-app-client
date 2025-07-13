package com.mydiary.diaryappclient;

import com.mydiary.diaryappclient.service.CredentialManager;
import com.mydiary.diaryappclient.util.SceneManager; // 1. Import lớp SceneManager
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.setPrimaryStage(stage);

        // Kiểm tra xem có thông tin đăng nhập đã lưu hay không
        if (CredentialManager.loadCredentials() != null) {
            // Nếu có, hiển thị màn hình nhập PIN
            SceneManager.switchScene("enter-pin-view.fxml", "Mở khóa");
        } else {
            // Nếu không, bắt đầu từ màn hình đăng nhập
            SceneManager.switchScene("login-view.fxml", "Ultimate Diary - Đăng nhập");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}