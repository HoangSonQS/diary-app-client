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

public class EnterPinController {

    @FXML private PasswordField pinField;
    @FXML private Label errorLabel;

    /**
     * Chữ ký phương thức phải có (ActionEvent event)
     */
    @FXML
    void handleUnlockAction(ActionEvent event) {
        String pin = pinField.getText();
        if (pin.isEmpty()) {
            showError("Vui lòng nhập mã PIN.");
            return;
        }

        CredentialManager.StoredCredentials credentials = CredentialManager.loadCredentials();
        if (credentials == null) {
            showError("Lỗi: Không tìm thấy thông tin đăng nhập.");
            return;
        }

        String enteredPinHash = SecurityUtil.hash(pin);

        if (!enteredPinHash.equals(credentials.pinHash())) {
            showError("Mã PIN không chính xác.");
            return;
        }

        String token = SecurityUtil.decrypt(credentials.encryptedToken(), pin);
        if (token == null) {
            showError("Lỗi giải mã. Vui lòng đăng nhập lại.");
            return;
        }

        AuthService.getInstance().setAuthToken(token);
        try {
            SceneManager.switchScene("main-view.fxml", "Nhật ký của bạn");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi không thể tải màn hình chính.");
        }
    }

    /**
     * Chữ ký phương thức phải có (ActionEvent event)
     */
    @FXML
    void handleForgotPasswordLink(ActionEvent event) {
        try {
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