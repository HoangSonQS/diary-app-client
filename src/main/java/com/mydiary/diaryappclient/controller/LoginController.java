package com.mydiary.diaryappclient.controller;

import com.mydiary.diaryappclient.model.AuthResponse;
import com.mydiary.diaryappclient.service.ApiClient;
import com.mydiary.diaryappclient.service.AuthService;
import com.mydiary.diaryappclient.util.SceneManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Hyperlink signUpLink;
    @FXML private Hyperlink forgotPasswordLink;

    public void initialize() {
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        showPasswordCheckBox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            passwordField.setVisible(!isNowSelected);
            passwordField.setManaged(!isNowSelected);
            visiblePasswordField.setVisible(isNowSelected);
            visiblePasswordField.setManaged(isNowSelected);
        });
    }

    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Tên đăng nhập và mật khẩu không được để trống.");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Đang xử lý...");
        errorLabel.setVisible(false);

        System.out.println("DEBUG: Nút đăng nhập đã được nhấn. Chuẩn bị tạo Task.");

        Task<AuthResponse> loginTask = new Task<>() {
            @Override
            protected AuthResponse call() throws Exception {
                System.out.println("DEBUG: Task đang chạy trên luồng nền. Bắt đầu gọi API...");
                AuthResponse response = ApiClient.getInstance().login(username, password);
                System.out.println("DEBUG: Đã gọi API xong. Nhận được phản hồi.");
                return response;
            }
        };

        loginTask.setOnSucceeded(e -> {
            System.out.println("DEBUG: Task BÁO THÀNH CÔNG (setOnSucceeded).");
            AuthResponse authResponse = loginTask.getValue();
            AuthService.getInstance().setAuthToken(authResponse.getToken());

            Platform.runLater(() -> {
                try {
                    SceneManager.switchScene("create-pin-view.fxml", "Tạo mã PIN");
                } catch (Exception  ex) {
                    ex.printStackTrace();
                    showError("Lỗi nghiêm trọng: Không thể tải màn hình tạo PIN.");
                    loginButton.setDisable(false);
                    loginButton.setText("Đăng nhập");
                }
            });
        });

        loginTask.setOnFailed(e -> {
            System.out.println("DEBUG: Task BÁO KHÔNG THÀNH CÔNG (setOnSucceeded).");
            Throwable exception = loginTask.getException();
            Platform.runLater(() -> {
                showError(exception.getMessage());
                loginButton.setDisable(false);
                loginButton.setText("Đăng nhập");
            });
        });

        new Thread(loginTask).start();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @FXML
    private void handleSignUpLinkAction(ActionEvent event) {
        try {
            SceneManager.switchScene("register-view.fxml", "Tạo tài khoản mới");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}