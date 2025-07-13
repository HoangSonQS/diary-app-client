package com.mydiary.diaryappclient.controller;

import com.mydiary.diaryappclient.service.AuthService;
import com.mydiary.diaryappclient.service.CredentialManager;
import com.mydiary.diaryappclient.util.SceneManager;
import com.mydiary.diaryappclient.util.SecurityUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

import java.io.IOException;

public class CreatePinController {

    @FXML private PasswordField pinField;
    @FXML private PasswordField confirmPinField;
    @FXML private Label errorLabel;

    /**
     * Chữ ký phương thức phải có (ActionEvent event)
     */
    @FXML
    void handleCreatePinAction(ActionEvent event) {
        String pin = pinField.getText();
        String confirmPin = confirmPinField.getText();

        if (pin.isEmpty() || !pin.matches("\\d{6}")) {
            showError("Mã PIN phải là 6 chữ số.");
            return;
        }
        if (!pin.equals(confirmPin)) {
            showError("Mã PIN xác nhận không khớp.");
            return;
        }

        try {
            String token = AuthService.getInstance().getAuthToken();
            if (token == null) {
                showError("Lỗi: Không tìm thấy phiên đăng nhập.");
                return;
            }

            String encryptedToken = SecurityUtil.encrypt(token, pin);
            String pinHash = SecurityUtil.hash(pin);

            CredentialManager.saveCredentials(encryptedToken, pinHash);

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