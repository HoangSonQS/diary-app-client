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

public class EnterPinController {

    @FXML private PasswordField pinField;
    @FXML private Label errorLabel;
    @FXML private Button unlockButton;

    @FXML
    void handleUnlockAction(ActionEvent event) {
        String pin = pinField.getText();
        if (pin.isEmpty()) {
            showError("Vui lòng nhập mã PIN.");
            return;
        }

        // Tải thông tin đã lưu
        CredentialManager.StoredCredentials credentials = CredentialManager.loadCredentials();
        if (credentials == null) {
            showError("Lỗi: Không tìm thấy thông tin đăng nhập. Vui lòng đăng nhập lại.");
            return;
        }

        // Băm mã PIN người dùng vừa nhập
        String enteredPinHash = SecurityUtil.hash(pin);

        // So sánh với mã PIN hash đã lưu
        if (!enteredPinHash.equals(credentials.pinHash())) {
            showError("Mã PIN không chính xác.");
            return;
        }

        // Nếu khớp, giải mã token
        String token = SecurityUtil.decrypt(credentials.encryptedToken(), pin);
        if (token == null) {
            showError("Lỗi giải mã. Vui lòng đăng nhập lại.");
            return;
        }

        // Lưu token vào AuthService và chuyển màn hình
        AuthService.getInstance().setAuthToken(token);
        try {
            SceneManager.switchScene("main-view.fxml", "Nhật ký của bạn");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi không thể tải màn hình chính.");
        }
    }

    @FXML
    void handleForgotPasswordLink(ActionEvent event) {
        // Nếu quên PIN, cho phép đăng nhập lại từ đầu
        try {
            // Xóa thông tin cũ để bắt đầu lại luồng đăng nhập
            CredentialManager.deleteCredentials();
            SceneManager.switchScene("login-view.fxml", "Ultimate Diary - Đăng nhập");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi khi chuyển màn hình.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}