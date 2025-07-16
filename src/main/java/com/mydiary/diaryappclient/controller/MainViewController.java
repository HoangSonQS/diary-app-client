package com.mydiary.diaryappclient.controller;

import com.mydiary.diaryappclient.service.AuthService;
import com.mydiary.diaryappclient.service.CredentialManager;
import com.mydiary.diaryappclient.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.io.IOException;

public class MainViewController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private Button setPinButton;

    @FXML
    public void initialize() {
        // Lấy username từ AuthService và hiển thị lời chào
        String username = AuthService.getInstance().getUsername();
        if (username != null && !username.isEmpty()) {
            welcomeLabel.setText("Chào mừng, " + username + "!");
        }
    }

    @FXML
    void handleLogoutButtonAction(ActionEvent event) {
        try {
            // Xóa token trong session
            AuthService.getInstance().clearAuthToken();
            // Xóa file lưu người dùng cuối cùng
            CredentialManager.deleteLastUser();

            // Quay về màn hình đăng nhập
            SceneManager.switchScene("login-view.fxml", "Ultimate Diary - Đăng nhập");
        } catch (IOException e) {
            e.printStackTrace();
            // Có thể hiển thị một Alert lỗi ở đây
        }
    }
    @FXML
    void handleSetPinButtonAction(ActionEvent event) {
        try {
            // Chuyển đến màn hình tạo PIN đã có sẵn
            SceneManager.switchScene("create-pin-view.fxml", "Thiết lập mã PIN");
        } catch (IOException e) {
            e.printStackTrace();
            // Có thể hiển thị Alert lỗi
        }
    }
}