package com.mydiary.diaryappclient.controller;

import com.mydiary.diaryappclient.service.AuthService;
import com.mydiary.diaryappclient.service.CredentialManager;
import com.mydiary.diaryappclient.util.SceneManager;
import com.mydiary.diaryappclient.util.SecurityUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

import java.io.IOException;

public class CreatePinController {

    @FXML private PasswordField pinField;
    @FXML private PasswordField confirmPinField;
    @FXML private Label errorLabel;
    @FXML private Button createPinButton;

    @FXML
    void handleCreatePinAction(ActionEvent event) {
        String pin = pinField.getText();
        String confirmPin = confirmPinField.getText();

        // --- Validation ---
        if (pin.isEmpty() || !pin.matches("\\d{6}")) {
            showError("Mã PIN phải là 6 chữ số.");
            return;
        }
        if (!pin.equals(confirmPin)) {
            showError("Mã PIN xác nhận không khớp.");
            return;
        }

        try {
            // Lấy token từ AuthService (đã được lưu sau khi đăng nhập)
            String token = AuthService.getInstance().getAuthToken();
            if (token == null) {
                showError("Lỗi: Không tìm thấy phiên đăng nhập. Vui lòng đăng nhập lại.");
                // Có thể chuyển về màn hình login ở đây
                return;
            }

            // Mã hóa token bằng PIN
            String encryptedToken = SecurityUtil.encrypt(token, pin);
            // Băm mã PIN để lưu trữ
            String pinHash = SecurityUtil.hash(pin);

            // Lưu vào file
            CredentialManager.saveCredentials(encryptedToken, pinHash);

            // Chuyển sang màn hình chính
            SceneManager.switchScene("main-view.fxml", "Nhật ký của bạn");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi: Không thể lưu thông tin đăng nhập.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}