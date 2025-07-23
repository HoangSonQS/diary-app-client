package com.mydiary.diaryappclient.controller;

import com.mydiary.diaryappclient.model.dto.AuthResponse;
import com.mydiary.diaryappclient.service.ApiClient;
import com.mydiary.diaryappclient.service.AuthService;
import com.mydiary.diaryappclient.service.CredentialManager;
import com.mydiary.diaryappclient.util.SceneManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    // Các trường FXML còn lại sau khi đã dọn dẹp
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Hyperlink signUpLink;
    @FXML private Hyperlink forgotPasswordLink;

    public void initialize() {
        // Chỉ còn giữ lại logic cho việc hiện/ẩn mật khẩu
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

        // Chỉ còn tạo Task cho việc đăng nhập bằng mật khẩu
        Task<AuthResponse> loginTask = new Task<>() {
            @Override
            protected AuthResponse call() throws Exception {
                return ApiClient.getInstance().login(username, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            AuthResponse authResponse = loginTask.getValue();
            AuthService.getInstance().setAuthToken(authResponse.getToken());
            AuthService.getInstance().setUsername(username);

            // Logic lưu người dùng cuối cùng để lần sau mở app sẽ hỏi PIN
            try {
                CredentialManager.saveLastUser(username);
            } catch (IOException ioException) {
                Platform.runLater(() -> showError("Lỗi: Không thể lưu thông tin người dùng."));
                ioException.printStackTrace();
                return;
            }

            Platform.runLater(() -> {
                try {
                    // Theo luồng mới, sau khi đăng nhập bằng mật khẩu sẽ vào thẳng màn hình chính
                    SceneManager.switchScene("main-view.fxml", "Nhật ký của bạn");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Lỗi tải màn hình chính.");
                }
            });
        });

        loginTask.setOnFailed(e -> {
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

    @FXML
    private void handleForgotPasswordLink(ActionEvent event) {
        // TODO: Thêm logic xử lý quên mật khẩu ở đây
        System.out.println("Chức năng quên mật khẩu được nhấn.");
    }
}